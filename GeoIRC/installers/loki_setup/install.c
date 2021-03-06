/* $Id: install.c,v 1.1 2003/10/14 17:00:24 mynameisneo Exp $ */

/* Modifications by Borland/Inprise Corp.:
    04/10/2000: Added code to expand ~ in a default path immediately after 
                XML is loaded 
 
   04/12/2000: Modifed run_script function to put the full pathname of the
               script into the temp script file. In some cases, setup was
			   having trouble finding the script.

   04/21/2000: Setup could not launch the game if the bin dir isn't in the
               user's path. Changed launch_game so it references the full
			   pathname for the symlink.

   05/17/2000: Modified create_install function to allow two new parameters:
               install_path and binary_path. These may have been passed in
			   using the -i and -b command line parameters. If so, then the
			   info->install_path or info->symlinks_path will be set using
			   the values from the command line.

   05/20/2000: Modified generate_uninstall so that it will add "rpm -e ..." 
               to the uninstall script for any RPM files that have their
			   autoremove flag set. Modified the rpm_elem structure to include
			   an new element for the autoremove flag. This is set on the
			   "files" tag, similar to the "relocate" flag. See copy.c for
			   details.

   05/24/2000: Modified generate_uninstall and uninstall functions to support
               two new options on the <install> tag:
			   <install preuninstall="script_filename"
			   postuninstall="script_filename" ... >
			   This allows extra cleanup to be done before and after the files
			   are removed. The contents of the script specified by the 
			   preuninstall option is added to the beginning of the uninstall 
			   script, before the file list and before any RPM uninstall 
			   scripts. The contents of the script specified by the
			   postuninstall option is added to the end of the uninstall script
			   after any RPM uninstall scripts. Note that the pre- and post-
			   uninstall scripts are not installed. Their contents are streamed
			   into the uninstall script. Also note that these scripts should
			   be executable as stand-alone scripts (they should have the
			   #!/bin/sh at the beginning) because in the event the install is 
			   aborted, these scripts will be run individually at the beginning
			   and end of the cleanup process.

			   Modified generate_uninstall to add environment settings to the
			   very beginning of the uninstall script. This allows the
			   uninstall script to use these variables: $SETUP_PRODUCTNAME, 
			   $SETUP_PRODUCTVER, $SETUP_INSTALLPATH, $SETUP_SYMLINKSPATH,
			   and $SETUP_CDROMPATH
*/

#include "config.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#ifdef HAVE_STRINGS_H
#  include <strings.h>
#endif
#include <pwd.h>
#include <unistd.h>
#include <signal.h>
#include <stdio.h>
#include <errno.h>
#ifdef HAVE_LIBGEN_H
# include <libgen.h>
#endif

#include "install.h"
#include "install_log.h"
#include "install_ui.h"
#include "detect.h"
#include "arch.h"
#include "log.h"
#include "copy.h"
#include "file.h"
#include "network.h"
#include "loki_launchurl.h"

extern char *rpm_root;
extern struct component_elem *current_component;

/* Global variables */
Install_UI UI;
int disable_install_path = 0;
int disable_binary_path = 0;

static int install_updatemenus_script = 0;
static int uninstall_generated = 0;

/* Functions to retrieve attribution information from the XML tree */
const char *GetProductName(install_info *info)
{
    const char *name;

    name = xmlGetProp(info->config->root, "product");
    if ( name == NULL ) {
        name = "";
    }
    return name;
}
const char *GetProductDesc(install_info *info)
{
    const char *desc;

    desc = xmlGetProp(info->config->root, "desc");
    if ( desc == NULL ) {
        desc = "";
    }
    return desc;
}

const char *GetProductComponent(install_info *info)
{
    return xmlGetProp(info->config->root, "component");
}

const char *GetProductUninstall(install_info *info)
{
    const char *desc;

    desc = xmlGetProp(info->config->root, "uninstall");
    if ( desc == NULL ) {
        desc = "uninstall";
    }
    return desc;
}
const char *GetProductSplash(install_info *info)
{
    const char *desc;

    desc = xmlGetProp(info->config->root, "splash");
    if ( desc == NULL ) {
        desc = "splash.xpm";
    }
    return desc;
}
const char *GetProductVersion(install_info *info)
{
    return xmlGetProp(info->config->root, "version");
}
const char *GetProductCategory(install_info *info)
{
    const char *cat;

    cat = xmlGetProp(info->config->root, "category");
    if ( cat == NULL ) {
        cat = "Games";
    }
    return cat;
}

const char *GetProductDefaultBinaryPath(install_info *info)
{
    return xmlGetProp(info->config->root, "binarypath");
}
int GetProductCDROMRequired(install_info *info)
{
    const char *str = xmlGetProp(info->config->root, "cdrom");
    if ( str && !strcasecmp(str, "required") ) {
        return 1;
    }
    return 0;
}
int GetProductIsMeta(install_info *info)
{
    const char *str = xmlGetProp(info->config->root, "meta");
    if ( str && !strcasecmp(str, "yes") ) {
        return 1;
    }
    return 0;
}
int GetProductInstallOnce(install_info *info)
{
    const char *str = xmlGetProp(info->config->root, "once");
    if ( str && !strcasecmp(str, "yes") ) {
        return 1;
    }
    return 0;
}
int GetProductRequireRoot(install_info *info)
{
    const char *str = xmlGetProp(info->config->root, "superuser");
    if ( str && !strcasecmp(str, "yes") ) {
        return 1;
    }
    return 0;
}

int GetProductAllowsExpress(install_info *info)
{
    const char *str = xmlGetProp(info->config->root, "express");
    if ( str && !strcasecmp(str, "yes") ) {
        return 1;
    }
    return 0;
}

int GetProductHasNoBinaries(install_info *info)
{
	return xmlGetProp(info->config->root, "nobinaries") || !has_binaries(info, info->config->root->childs);
}

int GetProductHasPromptBinaries(install_info *info)
{
    char *p = xmlGetProp(info->config->root, "promptbinaries");
    if (p && strstr(p, "yes")) {
		return 1;
	}
    return 0;
}

int GetProductIsAppBundle(install_info *info)
{
    const char *str = xmlGetProp(info->config->root, "appbundle");
    if ( str && !strcasecmp(str, "yes") ) {
        return 1;
    }
    return 0;
}

int GetProductSplashPosition(install_info *info)
{
    const char *str = xmlGetProp(info->config->root, "splashpos");
    if ( str && !strcasecmp(str, "top") ) {
        return 0;
    }
    return 1;
}

const char *GetProductCDKey(install_info *info)
{
    return xmlGetProp(info->config->root, "cdkey");
}

/* returns true if any deviant paths are not writable */
char check_deviant_paths(xmlNodePtr node, install_info *info)
{
    char path_up[PATH_MAX];

    while ( node ) {
        const char *wanted;
        const char *dpath;
        char deviant_path[PATH_MAX];

        wanted = xmlGetProp(node, "install");
        if ( wanted  && (strcmp(wanted, "true") == 0) ) {
            xmlNodePtr elements = node->childs;
            while ( elements ) {
                dpath = xmlGetProp(elements, "path");
                if ( dpath ) {
					parse_line(&dpath, deviant_path, PATH_MAX);
                    topmost_valid_path(path_up, deviant_path);
					if ( path_up[0] != '/' ) { /* Not an absolute path */
						char buf[PATH_MAX];
						snprintf(buf, PATH_MAX, "%s/%s", info->install_path, path_up);
						return ! dir_is_accessible(buf);
					} else if ( ! dir_is_accessible(path_up) )
                        return 1;
                }
                elements = elements->next;
            }
            if (check_deviant_paths(node->childs, info))
                return 1;
        }
        node = node->next;
    }
    return 0;
}

const char *IsReadyToInstall(install_info *info)
{
	const char *message = NULL;
	char path_up[PATH_MAX];
    struct stat st;
	
    /* Get the topmost valid path */
    topmost_valid_path(path_up, info->install_path);
  
    /* See if we can install yet */
	if ( ! *info->install_path ) {
        message = _("No destination directory selected");
    } else if ( info->install_size <= 0 ) {
		if ( !GetProductIsMeta(info) ) {
			message = _("Please select at least one option");
		}
    } else if ( BYTES2MB(info->install_size) > detect_diskspace(info->install_path) ) {
        message = _("Not enough free space for the selected options");
    } else if ( (stat(path_up, &st) == 0) && !S_ISDIR(st.st_mode) ) {
        message = _("Install path is not a directory");
    } else if ( access(path_up, W_OK) < 0 ) {
        message = _("No write permissions on the install directory");
	} else if (strcmp(info->symlinks_path, info->install_path) == 0) {
		message = _("Binary path and install path must be different");
	} else if ( check_deviant_paths(info->config->root->childs, info) ) {
        message = _("No write permissions to install a selected package");
    } else if ( info->symlinks_path[0] &&
				(access(info->symlinks_path, W_OK) < 0) ) {
        message = _("No write permissions on the binary directory");
    }

	return message;
}

const char *GetProductCDROMFile(install_info *info)
{
    return xmlGetProp(info->config->root, "cdromfile");
}
const char *GetDefaultPath(install_info *info)
{
    const char *path;

    path = xmlGetProp(info->config->root, "path");
    if ( path == NULL ) {
        path = DEFAULT_PATH;
    }
    return path;
}

const char *GetProductEULA(install_info *info, int *keepdirs)
{
	return GetProductEULANode(info, info->config->root, keepdirs);
}

const char *GetProductEULANode(install_info *info, xmlNodePtr node, int *keepdirs)
{
	const char *text;
	static char name[BUFSIZ], matched_name[BUFSIZ];
	int found = 0;

    text = xmlGetProp(node, "eula");
	if (text) {
		strncpy(matched_name, text, BUFSIZ);
		found = 1;
		log_warning("The 'eula' attribute is deprecated, please use the 'eula' element from now on.");
	}
	/* Look for EULA elements */
	node = node->childs;
	while(node) {
		if(! strcmp(node->name, "eula") ) {
			const char *prop = xmlGetProp(node, "lang");
			if ( match_locale(prop) ) {
				if (found == 1)
					log_warning("Duplicate matching EULA entries in XML file!");
				if ( keepdirs )
					*keepdirs = ( xmlGetProp(node, "keepdirs") != NULL);
				text = xmlNodeListGetString(info->config, node->childs, 1);
				if(text) {
					*matched_name = '\0';
					while ( (*matched_name == 0) && parse_line(&text, matched_name, sizeof(matched_name)) )
						;
					found = 2;
				}
			}
		}
		node = node->next;
	}
    if ( found ) {
		snprintf(name, sizeof(name), "%s/%s", info->setup_path, matched_name);
		if ( !access(name, R_OK) ) {
			return name;
		}
    }
	return NULL;
}

const char *GetProductREADME(install_info *info, int *keepdirs)
{
    const char *ret = xmlGetProp(info->config->root, "readme");
	const char *text;
	static char name[BUFSIZ], matched_name[BUFSIZ];
	xmlNodePtr node;
	int found = 0;

    if ( ! ret ) {
        strcpy(matched_name, "README");
    } else {
		strncpy(matched_name, ret, BUFSIZ);
		found = 1;
		log_warning("The 'readme' attribute is deprecated, please use the 'readme' element from now on.");
	}
	/* Try to find a README that matches the locale */
	node = info->config->root->childs;
	while(node) {
		if(! strcmp(node->name, "readme") ) {
			const char *prop = xmlGetProp(node, "lang");
			if ( match_locale(prop) ) {
				if (found == 1) {
					log_warning("Duplicate matching README entries in XML file!");
				}
				if ( keepdirs )
					*keepdirs = ( xmlGetProp(node, "keepdirs") != NULL);
				text = xmlNodeListGetString(info->config, node->childs, 1);
				if (text) {
					*matched_name = '\0';
					while ( (*matched_name == 0) && parse_line(&text, matched_name, sizeof(matched_name)) )
						;
					found = 2;
				}
			}
		}
		node = node->next;
	}
    if ( found  ) {
		snprintf(name, sizeof(name), "%s/%s", info->setup_path, matched_name);
		if ( !access(name, R_OK) ) {
			return name;
		}
    }
	return NULL;
}

const char *GetProductPostInstallMsg(install_info *info)
{
	xmlNodePtr node;
	const char *text;

	for(node = info->config->root->childs; node; node = node->next) {
		if(! strcmp(node->name, "post_install_msg") ) {
			const char *prop;
			if ( UI.is_gui ) {
				prop = xmlGetProp(node, "nogui");
				if ( prop && !strcmp(prop, "true") ){
					continue;
				}
			}
			prop = xmlGetProp(node, "command");
			if ( prop ) { /* Run the command */
				if ( run_script(info, prop, 0, 1) != 0 ) /* Failed, skip */
					continue;
			}
			prop = xmlGetProp(node, "lang");
			if ( match_locale(prop) ) {
				static char line[BUFSIZ], buf[BUFSIZ];

				text = xmlNodeListGetString(info->config, node->childs, 1);
				if (text) {
					*buf = '\0';
					while ( *text ) {
						parse_line(&text, line, sizeof(line));
						strcat(buf, line);
						strcat(buf, "\n");
					}
					return buf;
				}
			}
		}
	}
	return NULL;
}

int GetProductNumComponents(install_info *info)
{
    int count = 0;
	xmlNodePtr node;

	node = info->config->root->childs;
	while(node) {
        if ( !strcmp(node->name, "component") ) {
            count ++;
        }
        node = node->next;
    }
    return count;
}

int GetProductCDROMDescriptions(install_info *info)
{
    int count = 0;
	xmlNodePtr node;
    const char *text;
    char name[BUFSIZ];
    struct cdrom_elem *entry;

	node = info->config->root->childs;
	while(node) {
        if ( !strcmp(node->name, "cdrom") ) {
            text = xmlNodeListGetString(info->config, node->childs, 1);
            if (text) {
                *name = '\0';
                while ( (*name == 0) && parse_line(&text, name, sizeof(name)) )
						;
            }
            entry = add_cdrom_entry(info, xmlGetProp(node, "id"), xmlGetProp(node, "name"), name);
            if ( entry ) {
                
                count ++;
            }
        }
        node = node->next;
    }
    return count;
}

const char *GetWebsiteText(install_info *info)
{
    return xmlGetProp(info->config->root, "website_text");
}
const char *GetProductURL(install_info *info)
{
    return xmlGetProp(info->config->root, "url");
}

int GetProductReinstall(install_info *info)
{
	const char *str = xmlGetProp(info->config->root, "reinstall");
	return str && (*str=='t' || *str=='y');
}

int GetReinstallNode(install_info *info, xmlNodePtr node)
{
	const char *str = xmlGetProp(node, "reinstall");
	if ( str ) {
		return (*str=='t' || *str=='y');
	}
	return 1; /* Default to yes */
}

const char *GetLocalURL(install_info *info)
{
    const char *file;

    file = xmlGetProp(info->config->root, "localurl");
    if ( file ) {
        /* Warning, memory leak */
        char *path;

        path = (char *)malloc(PATH_MAX);
        strcpy(path, "file://");
        if ( getcwd(path+strlen(path), PATH_MAX-strlen(path)) == NULL ) {
			perror("GetLocalURL: getcwd");
		}
        strcat(path, "/");
        strncat(path, file, PATH_MAX-strlen(path));
        file = path;
    }
    return file;
}
const char *GetAutoLaunchURL(install_info *info)
{
    const char *auto_url;

    auto_url = xmlGetProp(info->config->root, "auto_url");
    if ( auto_url == NULL ) {
        auto_url = "false";
    }
    return auto_url;
}
const char *GetProductUpdateURL(install_info *info)
{
    const char *url;

    url = xmlGetProp(info->config->root, "update_url");
    if ( url == NULL ) {
        url = "http://icculus.org/";
    }
    return url;
}
const char *GetPreInstall(install_info *info)
{
    return xmlGetProp(info->config->root, "preinstall");
}
const char *GetPreUnInstall(install_info *info)
{
    return xmlGetProp(info->config->root, "preuninstall");
}
const char *GetPostInstall(install_info *info)
{
    return xmlGetProp(info->config->root, "postinstall");
}
const char *GetPostUnInstall(install_info *info)
{
    return xmlGetProp(info->config->root, "postuninstall");
}
const char *GetDesktopInstall(install_info *info)
{
    return xmlGetProp(info->config->root, "desktop");
}
const char *GetRuntimeArgs(install_info *info)
{
    const char *args;

    args = xmlGetProp(info->config->root, "args");
    if ( args == NULL ) {
        args = "";
    }
    return args;
}
const char *GetInstallOption(install_info *info, const char *option)
{
    return xmlGetProp(info->config->root, option);
}

/* Create the initial installation information */
install_info *create_install(const char *configfile,
                             const char *install_path,
                             const char *binary_path,
							 const char *product_prefix)
{
    install_info *info;
	char *temppath;
    const char *compname;

    /* Allocate the installation info block */
    info = (install_info *)malloc(sizeof *info);
    if ( info == NULL ) {
        fprintf(stderr, _("Out of memory\n"));
        return(NULL);
    }
    memset(info, 0, (sizeof *info));

    /* Load the XML configuration file */
    info->config = xmlParseFile(configfile);
    if ( info->config == NULL ) {
        delete_install(info);
        return(NULL);
    }

    /* Add information about install */
    info->name = GetProductName(info);
    info->desc = GetProductDesc(info);
    info->version = GetProductVersion(info);
	info->category = GetProductCategory(info),
    info->update_url = GetProductUpdateURL(info);
    info->arch = detect_arch();
    info->libc = detect_libc();
	info->distro = detect_distro(&info->distro_maj, &info->distro_min);

 	log_quiet(_("Detected distribution: %s %d.%d"), distribution_name[info->distro], info->distro_maj, info->distro_min);

    info->cdroms_list = NULL;

    /* Set product DB stuff to nothing by default */
    info->product = loki_openproduct(info->name);
    info->component = NULL;
    info->components_list = NULL;
	info->envvars_list = NULL;

	if ( getcwd(info->setup_path, PATH_MAX) == NULL )
		perror("create_install: getcwd");

    /* Read the optional default arguments for the game */
    info->args = GetRuntimeArgs(info);

    /* Add the default install path */
    if(GetProductIsAppBundle(info))
    {
        // If appbundle attribute set, the destination directory is
        // chosen differently that with a regular product installation.
        snprintf(info->install_path, PATH_MAX, "%s", GetDefaultPath(info));
    }
    else
    {
        snprintf(info->install_path, PATH_MAX, "%s/%s", GetDefaultPath(info),
			    GetProductName(info));
    }
    strcpy(info->symlinks_path, DEFAULT_SYMLINKS);

	*info->play_binary = '\0';

    /* If the default path starts with a ~, then expand it to the user's
       home directory */
    temppath = strdup(info->install_path);
    expand_home(info, temppath, info->install_path);
    free(temppath);

    /* if paths were passed in as command line args, set them here */
    if ( disable_install_path ) {
        strncpy(info->install_path, install_path, sizeof(info->install_path));
    }
    if ( disable_binary_path && binary_path ) {
        strncpy(info->symlinks_path, binary_path, sizeof(info->symlinks_path));
    } else if ( GetProductDefaultBinaryPath(info) ) {
        strncpy(info->symlinks_path, GetProductDefaultBinaryPath(info), sizeof(info->symlinks_path));
	}

	if ( product_prefix ) {
		strncpy(info->prefix, product_prefix, sizeof(info->prefix));
	} else {
		info->prefix[0] = '\0';
	}

    if ( info->product ) {
        product_info_t *pinfo = loki_getinfo_product(info->product);
        
        disable_install_path = 1;
        strncpy(info->install_path, pinfo->root, sizeof(info->install_path));
    }

    /* Handle component stuff */
    compname = GetProductComponent(info);
    if ( compname && info->product ) {
        info->component = loki_find_component(info->product, compname);
        if ( info->component ) {
            info->component = NULL;
        } else {
            info->component = loki_create_component(info->product, compname,
                                                    info->version);
        }
    }
    
    /* Start a network lookup for any URL */
    if ( GetProductURL(info) ) {
        info->lookup = open_lookup(info, GetProductURL(info));
    } else {
        info->lookup = NULL;
    }

	if ( !restoring_corrupt() ) {
		/* Now run any auto-detection commands */	
		mark_cmd_options(info, info->config->root, 0);
	}

    /* That was easy.. :) */
    return(info);
}

const char *remove_root(install_info *info, const char *path)
{
	if(strcmp(path, info->install_path) &&
       strstr(path, info->install_path) == path) {
		return path + strlen(info->install_path) + 1;
	}
	return path;
}

struct cdrom_elem *add_cdrom_entry(install_info *info, const char *id, const char *name,
                                   const char *file)
{
    struct cdrom_elem *elem;

    elem = (struct cdrom_elem *)malloc(sizeof *elem);
    if ( elem ) {
        elem->name = strdup(name);
        elem->id = strdup(id);
        elem->file = strdup(file);
        elem->mounted = NULL;
        elem->next = info->cdroms_list;
        info->cdroms_list = elem;
    }
    return elem;
}

void set_cdrom_mounted(struct cdrom_elem *cd, const char *path)
{
    if ( cd ) {
        free(cd->mounted);
        cd->mounted = path ? strdup(path) : NULL;
    }
}

struct envvar_elem *add_envvar_entry(install_info *info, struct component_elem *comp, const char *name)
{
	struct envvar_elem *elem;

	elem = (struct envvar_elem *) malloc(sizeof *elem);
	if ( elem ) {
		elem->name = strdup(name);
		if ( comp ) {
			elem->next = comp->envvars_list;
			comp->envvars_list = elem;
		} else { /* Product global env variable */
			elem->next = info->envvars_list;
			info->envvars_list = elem;
		}
	}
	return elem;
}

struct component_elem *add_component_entry(install_info *info, const char *name, const char *version,
                                           int def, const char *preun, const char *postun)
{
    struct component_elem *elem;

    elem = (struct component_elem *)malloc(sizeof *elem);
    if ( elem ) {
        elem->name = strdup(name);
        elem->version = strdup(version);
        elem->is_default = def;
        elem->options_list = NULL;
		elem->envvars_list = NULL;
		elem->preun = preun ? strdup(preun) : NULL;
		elem->postun = postun ? strdup(postun) : NULL;
		elem->message = NULL;
        elem->next = info->components_list;
        info->components_list = elem;
    }
    return elem;
}

struct option_elem *add_option_entry(struct component_elem *comp, const char *name, const char *tag)
{
    struct option_elem *elem;

    if ( ! comp ) {
        fprintf(stderr, _("Could not find default component. Make sure to define components for all entries."));
        abort();
    }

    elem = (struct option_elem *)malloc(sizeof *elem);
    if ( elem ) {
        elem->name = strdup(name);
		elem->tag = tag ? strdup(tag) : NULL;
        elem->file_list = NULL;
        elem->dir_list = NULL;
        elem->bin_list = NULL;
        elem->pre_script_list = elem->post_script_list = NULL;
        elem->rpm_list = NULL;
        elem->next = comp->options_list;
        comp->options_list = elem;
    }
    return elem;
}

/* Add a file entry to the list of files installed */
struct file_elem *add_file_entry(install_info *info, struct option_elem *comp,
                                 const char *path, const char *symlink, int mutable)
{
    struct file_elem *elem;

    elem = (struct file_elem *)malloc(sizeof *elem);
    if ( elem ) {
        elem->path = strdup(remove_root(info, path));
		elem->mutable = mutable;
        if ( elem->path ) {
            memset(elem->md5sum, 0, 16);
            elem->next = comp->file_list;
			if ( symlink ) {
				elem->symlink = strdup(symlink);
			} else {
				elem->symlink = NULL;
			}
            comp->file_list = elem;
        }
    } else {
        log_fatal(_("Out of memory"));
    }
	return elem;
}

void add_rpm_entry(install_info *info, struct option_elem *comp,
                   const char *name, const char *version, 
                   int release, const int autoremove)
{
    struct rpm_elem *elem;
    
    elem = (struct rpm_elem *)malloc(sizeof *elem);
    if ( elem ) {
        elem->name = strdup(name);
        elem->version = strdup(version);
        elem->release = release;
        if ( elem->name && elem->version) {
            elem->next = comp->rpm_list;
            comp->rpm_list = elem;
        }
		elem->autoremove = autoremove;
    } else {
        log_fatal(_("Out of memory"));
    }
}

void add_script_entry(install_info *info, struct option_elem *comp,
                      const char *script, int post)
{
    struct script_elem *elem;

    elem = (struct script_elem *)malloc(sizeof *elem);
    if ( elem ) {
        elem->script = strdup(script);
        if ( elem->script ) {
            if(post){
              elem->next = comp->post_script_list;
              comp->post_script_list = elem;
            }else{
              elem->next = comp->pre_script_list;
              comp->pre_script_list = elem;
            }
        }
    } else {
        log_fatal(_("Out of memory"));
    }
}

/* Add a directory entry to the list of directories installed */
void add_dir_entry(install_info *info, struct option_elem *comp,
                   const char *path)
{
    struct dir_elem *elem;

    if ( !strcmp(path, info->install_path) )
        return; /* Do not add an entry for the installation directory */

    elem = (struct dir_elem *)malloc(sizeof *elem);
    if ( elem ) {
        elem->path = strdup(remove_root(info, path));
        if ( elem->path ) {
            elem->next = comp->dir_list;
            comp->dir_list = elem;
        }
    } else {
        log_fatal(_("Out of memory"));
    }
}

/* Add a binary entry to the list of binaries installed */
void add_bin_entry(install_info *info, struct option_elem *comp, struct file_elem *file,
                   const char *symlink, const char *desc, const char *menu,
                   const char *name, const char *icon, const char *play)
{
    struct bin_elem *elem;

    elem = (struct bin_elem *)malloc(sizeof *elem);
    if ( elem ) {
        elem->file = file;
        elem->symlink = symlink;
        elem->desc = desc ? desc : "";
        elem->menu = menu;
        elem->name = name;
        elem->icon = icon;
        elem->next = comp->bin_list;
        comp->bin_list = elem;
        if ( play ) {
			if ( !strcmp(play, "yes") || !strcmp(play, "gui") ) {
				if ( !symlink ) {
					log_fatal(_("You must use a 'symlink' attribute with 'play'"));
				} else if ( !info->installed_symlink ) {
					info->installed_symlink = symlink;
					if ( !strcmp(play, "yes") || 
					     (!strcmp(play, "gui") && UI.is_gui) ) {
					  snprintf(info->play_binary, PATH_MAX, "%s/%s", info->symlinks_path, symlink);
					}
				} else {
					log_fatal(_("There can be only one binary with a 'play' attribute"));
				}
			} else if ( strcmp(play, "no") ) {
				log_fatal(_("The only valid values for the 'play' attribute are yes, gui and no"));
			}
        } else if ( symlink && !info->installed_symlink ) { /* Defaults to 'yes' */
			info->installed_symlink = symlink;
			snprintf(info->play_binary, PATH_MAX, "%s/%s", info->symlinks_path, symlink);
		}
    } else {
        log_fatal(_("Out of memory"));
    }
}

/* Expand a path with home directories into the provided buffer */
void expand_home(install_info *info, const char *path, char *buffer)
{
    buffer[0] = '\0';
    if ( *path == '~' ) {
        ++path;
        if ( (*path == '\0') || (*path == '/') ) {
            const char *home;

            /* Substitute '~' with our home directory */
            home = detect_home();
            if ( home ) {
                strcpy(buffer, home);
            } else {
                log_warning(_("Couldn't find your home directory"));
            }
        } else {
            char user[PATH_MAX];
            int i;
            struct passwd *pwent;

            /* Find out which user to use for home directory */
            for ( i=0; *path && (*path != '/'); ++i ) {
                user[i] = *path++;
            }
            user[i] = '\0';

            /* Get their home directory if possible */
            pwent = getpwnam(user);
            if ( pwent ) {
                strcpy(buffer, pwent->pw_dir);
            } else {
                log_warning(_("Couldn't find home directory for %s"), user);
            }
        }
    }
	/* TODO: Expand shell variables as well */
    strcat(buffer, path);
}

/* Function to set the install path string, expanding home directories */
void set_installpath(install_info *info, const char *path)
{
    size_t len = strlen(path);
    char newpath[len + 2];      // Allocate for possible extra char and null

    if(len > 0)
    {
        // If last character is not a '/', then add one.  A '/' character at
        //  the end of the install path is required for later code to work
        //  correctly.
        if(path[len-1] != '/')
        {
            strcpy(newpath, path);
            strcat(newpath, "/");
            path = newpath;
        }
    }
    expand_home(info, path, info->install_path);
}

/* Function to set the symlink path string, expanding home directories */
void set_symlinkspath(install_info *info, const char *path)
{
    expand_home(info, path, info->symlinks_path);
}

/* Mark/unmark an option node for install, optionally recursing */
void mark_option(install_info *info, xmlNodePtr node,
                 const char *value, int recurse)
{
    /* Unmark this option for installation */
    if ( !strcmp(node->name, "option") ) {
        xmlSetProp(node, "install", value);
    }

    /* Recurse down any other options */
    if ( recurse ) {
        node = node->childs;
        while ( node ) {
            if ( !strcmp(node->name, "option") ) {
                mark_option(info, node, value, recurse);
            }
			/* We don't touch exclusive options */
            node = node->next;
        }
    }
}

/* Enable an option, given its name */
int enable_option_recurse(install_info *info, xmlNodePtr node, const char *option)
{
    int ret = 0;
    while ( node ) {
        if ( !strcmp(node->name, "option")) {
            /* Is this the option we're looking for ? */
            char name[BUFSIZ];
            const char *text = xmlNodeListGetString(info->config, node->childs, 1);
            *name = '\0';
            while ( (*name == 0) && parse_line(&text, name, sizeof(name)) )
                ;
            if ( !strcmp(name, option) ) {
                mark_option(info, node, "true", 1);
                ret ++;
            }
        } else if ( !strcmp(node->name, "exclusive") ) {
            ret += enable_option_recurse(info, node->childs, option);
        } else if ( !strcmp(node->name, "component") ) {
            ret += enable_option_recurse(info, node->childs, option);            
        } 
        node = node->next;
    }
    return ret;
}

int enable_option(install_info *info, const char *option)
{
   return enable_option_recurse(info, info->config->root->childs, option);
}

/* Check for all the 'require' tags */
int CheckRequirements(install_info *info)
{
	xmlNodePtr node = info->config->root->childs;
	char line[BUFSIZ], buf[BUFSIZ];
    const char *text;

	while ( node ) {
		if ( !strcmp(node->name, "require") && match_locale(xmlGetProp(node, "lang")) &&
			 match_arch(info, xmlGetProp(node, "arch")) &&
			 match_libc(info, xmlGetProp(node, "libc")) &&
			 match_distro(info, xmlGetProp(node, "distro")) ) {
			const char *prop = xmlGetProp(node, "command");
			if ( !prop ) {
				log_fatal(_("XML: 'require' tag doesn't have a mandatory 'command' attribute"));
			} else {
				/* Launch the command */
				if ( run_script(info, prop, 0, 0) != 0 ) {
					/* We failed: print out error message */
					text = xmlNodeListGetString(info->config, node->childs, 1);
					if(text) {
						*buf = '\0';
						while ( *text ) {
							parse_line(&text, line, sizeof(line));
							strcat(buf, line);
							strcat(buf, "\n");
						}
						UI.prompt(buf, RESPONSE_OK);
					}
					return 0;
				}
			}
		}
		node = node->next;
	}
	return 1; /* All requirements passed */
}

/* Get the name of an option node */
char *get_option_name(install_info *info, xmlNodePtr node, char *name, int len)
{
    static char line[BUFSIZ];
    const char *text;

    if ( name == NULL ) {
        name = line;
        len = (sizeof line);
    }
    text = xmlNodeListGetString(info->config, node->childs, 1);
    *name = '\0';
    if ( text ) {
		xmlNodePtr n;
        while ( (*name == 0) && parse_line(&text, name, len) )
            ;
		/* Parse the children and look for a 'lang' element for translated names */
		n = node->childs;
		while ( n ) {
			if( strcmp(n->name, "lang") == 0 ) {
				const char *prop = xmlGetProp(n, "lang");
				if ( ! prop ) {
					log_fatal(_("XML: 'lang' tag does not have a mandatory 'lang' attribute"));
				} else if ( match_locale(prop) ) {
					text = xmlNodeListGetString(info->config, n->childs, 1);
					if(text) {
						*name = '\0';
						while ( (*name == 0) && parse_line(&text, name, len) )
							;
						break;
					} else {
						log_warning(_("XML: option listed without translated description for locale '%s'"), prop);
					}
				}
			}
			n = n->next;
		}
    } else {
        log_warning(_("XML: option listed without description"));
    }
    return name;
}

/* Get the optional help of an option node, with localization support */
const char *get_option_help(install_info *info, xmlNodePtr node)
{
	static char line[BUFSIZ];
    const char *help = xmlGetProp(node, "help"), *text;
	xmlNodePtr n;

	*line = '\0';
	if ( help ) {
		strncpy(line, help, sizeof(line));
		log_warning("The 'help' attribute is deprecated, please use the 'help' element from now on.");
	}
	/* Look for translated strings */
	n = node->childs;
	while ( n ) {
		if( strcmp(n->name, "help") == 0 ) {
			const char *prop = xmlGetProp(n, "lang");
			if ( match_locale(prop) ) {
				text = xmlNodeListGetString(info->config, n->childs, 1);
				if(text) {
					*line = '\0';
					while ( (*line == 0) && parse_line(&text, line, sizeof(line)) )
						;
					break;
				}
			}
		}
		n = n->next;
	}
	return (*line) ? line : NULL;
}

/* Get the optional selection warning of an option node, with localization support */
const char *get_option_warn(install_info *info, xmlNodePtr node)
{
	static char buf[BUFSIZ];
	char line[BUFSIZ];
    const char *text;
	xmlNodePtr n;

	*buf = *line = '\0';
	/* Look for translated strings */
	n = node->childs;
	while ( n ) {
		if( strcmp(n->name, "warn") == 0 ) {
			const char *prop = xmlGetProp(n, "lang");
			if ( match_locale(prop) ) {
				text = xmlNodeListGetString(info->config, n->childs, 1);
				if(text) {
					*line = '\0';
					*buf = '\0';
					while ( *text ) { 
						parse_line(&text, line, sizeof(line));
						strcat(buf, line);
						strcat(buf, "\n");
					}
					break;
				}
			}
		}
		n = n->next;
	}
	return (*buf) ? buf : NULL;
}


/* Determine if an option should be displayed */
int get_option_displayed(install_info *info, xmlNodePtr node)
{
    if ( node ) {
	const char *txt = xmlGetProp(node, "show");
	if ( txt ) {
	    if ( !strcasecmp(txt, "false") )
		return 0;

	    /* Launch the command */
	    return run_script(info, txt, 0, 0) == 0;
	}
    }
    return 1;
}

void delete_cdrom_install(install_info *info)
{
    struct cdrom_elem *cdrom;
    while ( info->cdroms_list ) {
        cdrom = info->cdroms_list;
        info->cdroms_list = cdrom->next;
        free(cdrom->name);
        free(cdrom->id);
        free(cdrom->file);
        free(cdrom->mounted);
        free(cdrom);
    }
}

/* Free the install information structure */
void delete_install(install_info *info)
{
    struct component_elem *comp;
	struct envvar_elem *var;

	while ( info->envvars_list ) {
		var = info->envvars_list;
		info->envvars_list = var->next;
		free(var->name);
		free(var);
	}

    while ( info->components_list ) {
        struct option_elem *opt;
        
        comp = info->components_list;
        info->components_list = comp->next;

        while ( comp->options_list ) {
            opt = comp->options_list;
            comp->options_list = opt->next;

            while ( opt->file_list ) {
                struct file_elem *elem;
 
                elem = opt->file_list;
                opt->file_list = elem->next;
                free(elem->path);
                free(elem);
            }
            while ( opt->dir_list ) {
                struct dir_elem *elem;
 
                elem = opt->dir_list;
                opt->dir_list = elem->next;
                free(elem->path);
                free(elem);
            }
            while ( opt->bin_list ) {
                struct bin_elem *elem;
 
                elem = opt->bin_list;
                opt->bin_list = elem->next;
                free(elem);
            }
            while ( opt->pre_script_list ) {
                struct script_elem *elem;
 
                elem = opt->pre_script_list;
                opt->pre_script_list = elem->next;
                free(elem->script);
                free(elem);
            }
            while ( opt->post_script_list ) {
                struct script_elem *elem;
 
                elem = opt->post_script_list;
                opt->post_script_list = elem->next;
                free(elem->script);
                free(elem);
            }
            while ( opt->rpm_list ) {
                struct rpm_elem *elem;
 
                elem = opt->rpm_list;
                opt->rpm_list = elem->next;
                free(elem->name);
                free(elem->version);
                free(elem);
            }
            free(opt->name);
			free(opt->tag);
            free(opt);
        }

		while ( comp->envvars_list ) {
			var = comp->envvars_list;
			comp->envvars_list = var->next;
			free(var->name);
			free(var);
		}

        free(comp->name);
        free(comp->version);
		free(comp->preun);
		free(comp->postun);
		free(comp->message);
        free(comp);
    }
	delete_cdrom_install(info);
    if ( info->lookup ) {
        close_lookup(info->lookup);
    }
    free(info);
}


/* Actually install the selected filesets */
install_state install(install_info *info,
					  int (*update)(install_info *info, const char *path, 
									size_t progress, size_t size, const char *current))
{
    xmlNodePtr node;
    install_state state;
	const char *f;
	struct component_elem *comp;
	int keepdirs = 0;
	extern struct option_elem *current_option;

    /* Check if we need to create a default component entry */
    if ( GetProductNumComponents(info) == 0 ) {
        current_component = add_component_entry(info, "Default", info->version, 1, NULL, NULL);
    }

    /* Walk the install tree */
    node = info->config->root->childs;
    info->install_size = size_tree(info, node);

    copy_tree(info, node, info->install_path, update);

	/* Install the optional README and EULA files
	   Warning: those are always installed in the root of the installation directory!
	 */

	for(comp = info->components_list; comp; comp = comp->next ) {
		if ( comp->is_default ) {
			struct option_elem *opt;
			/* The first option of a component is the last in the linked list */
			for ( opt = comp->options_list; opt; opt = opt->next ) {
				if ( opt->next == NULL ) {
					current_option = opt;
					break;
				}
			}
			break;
		}
	}
	f = GetProductREADME(info, &keepdirs);
	if ( f && ! GetProductIsMeta(info) ) {
		if ( strstr(f, info->setup_path) == f )
			f += strlen(info->setup_path)+1;
		copy_path(info, f, info->install_path, NULL, !keepdirs, NULL, update);
	}
	keepdirs = 0;
	f = GetProductEULA(info, &keepdirs);
	if ( f && ! GetProductIsMeta(info) ) {
		if ( strstr(f, info->setup_path) == f )
			f += strlen(info->setup_path)+1;
		copy_path(info, f, info->install_path, NULL, !keepdirs, NULL, update);
	}
    if(info->options.install_menuitems){
		int i;
		for(i = 0; i<MAX_DESKTOPS; i++) {
			if (install_menuitems(info, i))
				break;
        }
    }
    if ( ! GetInstallOption(info, "nouninstall") ) {
        generate_uninstall(info);
    }
	info->install_complete = 1;

    /* Return the new install state */
    if ( GetProductURL(info) ) {
        state = SETUP_WEBSITE;
    } else {
        state = SETUP_COMPLETE;
    }
    return state;
}

/* Remove a partially installed product 
   DO NOT FREE ANYTHING HERE. All memory is free()'d in delete_install()
 */
void uninstall(install_info *info)
{
    char path[PATH_MAX];
    struct option_elem *opt;
    struct component_elem *comp;

	if ( info->installed_bytes == 0 ) { /* Nothing to do */
		return;
	}

    if (GetPreUnInstall(info) && info->installed_bytes>0) {
		snprintf(path, sizeof(path), "sh %s", GetPreUnInstall(info));
        run_script(info, path, 0, 1);
    }

    if ( file_exists(info->install_path) ) {
        push_curdir(info->install_path);
    }

    for ( comp = info->components_list; comp; comp = comp->next ) {
        for ( opt = comp->options_list; opt; opt = opt->next ) {
            struct script_elem *selem;
            struct file_elem *felem;
            struct dir_elem *delem;
            struct rpm_elem *relem;

			/* Do not run scripts if nothing was installed */
			if ( info->installed_bytes>0 ) {
				for ( selem = opt->pre_script_list; selem; selem = selem->next ) { /* RPM pre-uninstall */
					run_script(info, selem->script, 0, 1);
				}
			}

            for ( felem = opt->file_list; felem; felem = felem->next ) {
                if ( unlink(felem->path) < 0 ) {
                    log_warning(_("Unable to remove '%s'"), felem->path);
                }
            }

            for ( delem = opt->dir_list; delem; delem = delem->next ) {
                if ( rmdir(delem->path) < 0 ) {
                    log_warning(_("Unable to remove '%s'"), delem->path);
                }
            }
			if ( info->installed_bytes>0 ) {
				for ( selem = opt->post_script_list; selem; selem = selem->next ) { /* RPM post-uninstall */
					run_script(info, selem->script, 0, 1);
				}
			}

            for ( relem = opt->rpm_list; relem; relem = relem->next ) {
                log_warning(_("The '%s' RPM was installed or upgraded (version %s, release %d)"),
                            relem->name, relem->version, relem->release);
            }

        }
    }
    /* Check for uninstall script and remove it if present */
    snprintf(path, PATH_MAX, "%s/%s", info->install_path, GetProductUninstall(info));
    if ( file_exists(path) && unlink(path) < 0 ) {
        log_warning(_("Unable to remove '%s'"), path);
    }
    if (GetPostUnInstall(info) && info->installed_bytes>0) {
		snprintf(path, sizeof(path), "sh %s", GetPostUnInstall(info));
        run_script(info, path, 0, 1);
    }

    if ( uninstall_generated ) {
		/* Remove support files as well */
		loki_removeproduct(info->product);
    }
    pop_curdir();
}

static char tags[2048] = "";
static int tags_left;

static void optionstag_sub(install_info *info, xmlNodePtr node)
{
	if ( tags_left <= 0 ) /* String full */ {
		return;
	}

	while ( node ) {
		if ( ! strcmp(node->name, "option") ) {
			const char *wanted = xmlGetProp(node, "install");
			if ( wanted  && (strcmp(wanted, "true") == 0) ) {
				const char *tag = xmlGetProp(node, "tag");
				if ( tag &&
					 match_locale(xmlGetProp(node, "lang")) &&
					 match_arch(info, xmlGetProp(node, "arch")) &&
					 match_libc(info, xmlGetProp(node, "libc")) &&
					 match_distro(info, xmlGetProp(node, "distro"))
					 ) {
					/* Do not add the tag if it's already in */
					if  ( info->product ) {
						product_component_t *comp;
						product_option_t *opt;
						char optname[BUFSIZ];
						int found = 0;

						get_option_name(info, node, optname, sizeof(optname));

						for ( comp = loki_getfirst_component(info->product); comp; comp = loki_getnext_component(comp) ) {
							opt = loki_find_option(comp, optname);
							if ( opt && loki_gettag_option(opt) ) {
								found = 1;
								break;
							}
						}
						if ( !found ) {
							strncat(tags, tag, tags_left);
							tags_left -= strlen(tag);
							strncat(tags, " ", tags_left);
							tags_left --;
						}
					} else {
						strncat(tags, tag, tags_left);
						tags_left -= strlen(tag);
						strncat(tags, " ", tags_left);
						tags_left --;
					}
				}
			}
		} else if ( ! strcmp(node->name, "exclusive" ) ) {
			optionstag_sub(info, node->childs);
		} else if ( ! strcmp(node->name, "component" ) ) {
            if ( match_arch(info, xmlGetProp(node, "arch")) &&
                 match_libc(info, xmlGetProp(node, "libc")) &&
				 match_distro(info, xmlGetProp(node, "distro")) ) {
				optionstag_sub(info, node->childs);
			}
		}
		node = node->next;
	}
}

static const char *get_optiontags_string(install_info *info)
{
	if ( *tags == '\0' ) { /* Cache the results */
		tags_left = sizeof(tags)-1;

		/* First look for already installed tags */
		if ( info->product ) {
			product_component_t *comp;
			product_option_t *opt;
			const char *tag;
			
			for ( comp = loki_getfirst_component(info->product); comp; comp = loki_getnext_component(comp) ) {
				for ( opt = loki_getfirst_option(comp); opt; opt = loki_getnext_option(opt) ) {
					tag = loki_gettag_option(opt);
					if ( tag ) {
						strncat(tags, tag, tags_left);
						tags_left -= strlen(tag);
						strncat(tags, " ", tags_left);
						tags_left --;
					}
				}
			}
		}
		
		/* Recursively parse the XML for install="true" tags */
		optionstag_sub(info, info->config->root->childs);
		
		if ( tags_left <= 0 ) /* String full */ {
			log_warning(_("Options tag string maxed out!"));
		} else {
			int len = strlen(tags);
			if ( len > 0 ) { /* Remove the last space */
				tags[len-1] = '\0';
			}
		}
		log_debug("Options tags is '%s'\n", tags);
	}
	return tags;
}

static void output_script_header(FILE *f, install_info *info, product_component_t *comp)
{
    fprintf(f,
            "SETUP_PRODUCTNAME=\"%s\"\n"
            "SETUP_PRODUCTVER=\"%s\"\n"
            "SETUP_COMPONENTNAME=\"%s\"\n"
            "SETUP_COMPONENTVER=\"%s\"\n"
            "SETUP_INSTALLPATH=\"%s\"\n"
            "SETUP_SYMLINKSPATH=\"%s\"\n"
            "SETUP_CDROMPATH=\"%s\"\n"
            "SETUP_DISTRO=\"%s\"\n"
			"SETUP_OPTIONTAGS=\"%s\"\n"
            "export SETUP_PRODUCTNAME SETUP_PRODUCTVER SETUP_COMPONENTNAME SETUP_COMPONENTVER\n"
            "export SETUP_INSTALLPATH SETUP_SYMLINKSPATH SETUP_CDROMPATH SETUP_DISTRO SETUP_OPTIONTAGS\n",
            info->name, info->version,
            loki_getname_component(comp), loki_getversion_component(comp),
            info->install_path,
            info->symlinks_path,
            info->cdroms_list ? info->cdroms_list->mounted : "",
			info->distro ? distribution_symbol[info->distro] : "",
			get_optiontags_string(info)
			);
#ifdef RPM_SUPPORT
    if(strcmp(rpm_root,"/")) /* Emulate RPM environment for scripts */
        fprintf(f,"RPM_INSTALL_PREFIX=%s\n", rpm_root);
#endif
    fseek(f, 0L, SEEK_CUR);
}

static void generate_uninst_script(install_info *info, product_component_t *component, 
								   const char *file, const char *name, script_type_t t)
{
	FILE *pre;
	int count;
	char tmp_name[] = "/tmp/setupXXXXXX", buf[1024];
	int tmp = mkstemp(tmp_name);
	if ( tmp < 0 ) {
		log_fatal(_("Could not create temporary script"));
	}
	
	pre = fdopen(tmp, "w");
	if ( pre ) {
		int script_file = open(file, O_RDONLY);
		output_script_header(pre, info, component);
		if (script_file > 0) {
			for(;;) {
				count = read(script_file, buf, sizeof(buf));
				if(count>0)
					fwrite(buf, 1, count, pre);
				else
					break;
			}
			close(script_file);
		}
		fchmod(fileno(pre), 0755);
		fclose(pre);
		loki_registerscript_fromfile_component(component, t, name, tmp_name);
		unlink(tmp_name);
	}
}

void generate_uninstall(install_info *info)
{
    product_t *product;
    product_component_t *component = NULL;
    product_option_t *option;
    struct component_elem *comp;

    if ( info->component ) { /* Component install, the product has already been opened */
        product = info->product;
        component = info->component;
    } else {
        /* Try to open the product first in case it was installed previously */
        product = loki_openproduct(info->name);
        if ( ! product ) {
            product = loki_create_product(info->name, info->install_path, info->desc,
                                          info->update_url);
        }
		if ( *info->prefix ) {
			loki_setprefix_product(product, info->prefix);
		}
		info->product = product;
    }

    if ( product ) {
        char buf[PATH_MAX];
		struct envvar_elem *var;
		uninstall_generated = 1;

		for(var = info->envvars_list; var; var = var->next ) {
			loki_register_envvar(product, var->name);
		}

        for ( comp = info->components_list; comp; comp = comp->next ) {
            struct file_elem *felem;
            struct bin_elem *belem;
            struct dir_elem *delem;
            struct script_elem *selem;
            struct rpm_elem *relem;
            struct option_elem *opt;

            if ( ! comp->options_list ) {
                /* Skip empty components */
                continue;
            }

            if ( ! info->component ) {
                component = loki_find_component(product, comp->name);
                if ( ! component ) {
                    component = loki_create_component(product, comp->name, comp->version);
                }
                if ( comp->is_default ) {
                    loki_setdefault_component(component);
                }
            }

			/* Store per-component env variables */
			for(var = comp->envvars_list; var; var = var->next ) {
				loki_register_envvar_component(component, var->name);
			}

			/* Register per-component uninstall scripts */
			if ( comp->preun ) {
				snprintf(buf, sizeof(buf), "%s-preun", comp->name);
				generate_uninst_script(info, loki_find_component(product, comp->name),
									   comp->preun, buf, LOKI_SCRIPT_PREUNINSTALL);
			}
			if ( comp->postun ) {
				snprintf(buf, sizeof(buf), "%s-postun", comp->name);
				generate_uninst_script(info, loki_find_component(product, comp->name),
									   comp->postun, buf, LOKI_SCRIPT_POSTUNINSTALL);
			}

			if ( comp->message ) {
				loki_setmessage_component(loki_find_component(product, comp->name), comp->message);
			}

            push_curdir(info->install_path);
            for ( opt = comp->options_list; opt; opt = opt->next ) {
                option = loki_find_option(component, opt->name);
                if ( ! option ) {
                    option = loki_create_option(component, opt->name, opt->tag);
                }
                /* Add files */
                for ( felem = opt->file_list; felem; felem = felem->next ) {
                    if ( felem->symlink || *(int *)felem->md5sum == 0 ) {
                        loki_register_file(option, felem->path, NULL);
                    } else {
                        loki_register_file(option, felem->path, get_md5(felem->md5sum));
                    }
                }

                /* Add binaries */
                for ( belem = opt->bin_list; belem; belem = belem->next ) {
                    if ( belem->file ) {
                        product_file_t *file = loki_register_file(option, belem->file->path,
                                                                  get_md5(belem->file->md5sum));
                        if (! file) {
                            log_warning(_("Could not register binary: %s"), belem->file->path);
                        }
                    }
                }

                /* Add directories */
                for ( delem = opt->dir_list; delem; delem = delem->next ) {
                    loki_register_file(option, delem->path, NULL);
                }

                /* Add RPM entries */
                for ( relem = opt->rpm_list; relem; relem = relem->next ) {
                    loki_register_rpm(option, relem->name, relem->version, relem->release,
                                      relem->autoremove);
                }
		
                /* Generate optional pre and post uninstall scripts in the 'scripts' subdirectory */

                if(opt->pre_script_list){
                    FILE *pre;
					char tmp_name[] = "/tmp/setupXXXXXX";
                    int tmp = mkstemp(tmp_name);
					if ( tmp < 0 ) {
                        log_fatal(_("Could not create temporary script"));
					}

                    pre = fdopen(tmp, "w");
                    if ( pre ) {
                        output_script_header(pre, info, component);

                        fprintf(pre, "pre()\n{\n");
                        for ( selem = opt->pre_script_list; selem; selem = selem->next ) {
                            fprintf(pre, "%s\n", selem->script);
                        }
                        fprintf(pre,"}\npre 0\n");
                    
                        fchmod(fileno(pre), 0755);
                        fclose(pre);
                        snprintf(buf, sizeof(buf), "%s-preun", opt->name);
                        loki_registerscript_fromfile(option, LOKI_SCRIPT_PREUNINSTALL, buf, tmp_name);
                        unlink(tmp_name);
                    } else {
                        log_fatal(_("Could not write temporary pre-uninstall script in %s"), buf);
                    }
                }

                if(opt->post_script_list){
                    FILE *post;
					char tmp_name[] = "/tmp/setupXXXXXX";
                    int tmp = mkstemp(tmp_name);
					if ( tmp < 0 ) {
                        log_fatal(_("Could not create temporary script"));
					}
					
                    post = fdopen(tmp, "w");
                    if ( post ) {
                        output_script_header(post, info, component);
                        fprintf(post, "post()\n{\n");
                        for ( selem = opt->post_script_list; selem; selem = selem->next ) {
                            fprintf(post, "%s\n", selem->script);
                        }
                        fprintf(post,"}\npost 0\n");
                        fclose(post);
                        snprintf(buf, sizeof(buf), "%s-postun", opt->name);
                        loki_registerscript_fromfile(option, LOKI_SCRIPT_POSTUNINSTALL, buf, tmp_name);
                        unlink(tmp_name);
                    } else {
                        log_fatal(_("Could not write post-uninstall script in %s"), buf);
                    }
                }
            }
            pop_curdir();
        }

		if ( install_updatemenus_script ) {
			/* Add a call to the post-uninstall scripts */
			loki_registerscript_component(component, LOKI_SCRIPT_POSTUNINSTALL,
										  "update-menus", 
										  "exec 1>&-\nexec 2>&-\n"
										  "if which update-menus 2> /dev/null > /dev/null || type -p update-menus 2> /dev/null >/dev/null; then update-menus 2> /dev/null; fi\n"
										  "if which kbuildsycoca 2> /dev/null > /dev/null || type -p kbuildsycoca 2> /dev/null >/dev/null; then kbuildsycoca 2>/dev/null; fi\n"
										  "if which dtaction 2> /dev/null > /dev/null || type -p dtaction 2> /dev/null > /dev/null; then dtaction RestorePanel 2>/dev/null; fi\n"
										  "true\n");
		}

        /* Register the pre and post uninstall scripts with the default component */
        component = loki_getdefault_component(product);

        if (GetPreUnInstall(info)) {
			generate_uninst_script(info, component, GetPreUnInstall(info), "preun", LOKI_SCRIPT_PREUNINSTALL);
        }

        if (GetPostUnInstall(info)) {
			generate_uninst_script(info, component, GetPostUnInstall(info), "postun", LOKI_SCRIPT_POSTUNINSTALL);
        }

        snprintf(buf, sizeof(buf), "setup.data/bin/%s/%s/uninstall", detect_os(), detect_arch());
        loki_upgrade_uninstall(product, buf, "setup.data/locale");
		loki_closeproduct(product);
    } else {
		log_fatal(_("Could not create install log"),
				  detect_home(), info->name);
	}
}

/* Launch a web browser with a product information page
   Since this blocks waiting for the browser to return (unless
   you are using netscape and it's already open), you should do
   this as the very last stage of the installation.
 */
int launch_browser(install_info *info, int (*launcher)(const char *url))
{
    const char *url;
    int retval;

    url = NULL;
    if ( info->lookup ) {
        if ( poll_lookup(info->lookup) ) {
            url = GetProductURL(info);
        } else {
            url = GetLocalURL(info);
        }
    }
    retval = -1;
    if ( url ) {
        retval = launcher(url);
        if ( retval < 0 ) {
            log_warning(_("Please visit %s"), url);
        }
    }
    return retval;
}

/* Run pre/post install scripts */
int install_preinstall(install_info *info)
{
    const char *script;
    int exitval = 0;

	if ( ! restoring_corrupt() ) {
		script = GetPreInstall(info);
		if ( script ) {
			exitval = run_script(info, script, -1, 1);
		}
	}
    return exitval;
}
int install_postinstall(install_info *info)
{
    const char *script;
    int exitval = 0;

	if ( ! restoring_corrupt() ) {
		script = GetPostInstall(info);
		if ( script ) {
			exitval = run_script(info, script, -1, 1);
		}
	}
    return exitval;
}

/* Recursively look for options with install="command" and run the commands to determine the actual status */
void mark_cmd_options(install_info *info, xmlNodePtr parent, int exclusive)
{
	int cmd = 1;
	const char *str;
	/* Iterate through the children */
	xmlNodePtr child;
	for ( child = parent->childs; child; child = child->next ) {
		if ( !strcmp(child->name, "option") ) {
			str = xmlGetProp(child, "install");
			if ( str ) {
				if ( !strcmp(str, "command") ) {
					/* Run the command and set it to "true" if the return value is ok */
					str = xmlGetProp(child, "command");
					if ( str ) {
						cmd = run_script(info, str, 0, 0);
						xmlSetProp(child, "install", cmd ? "false" : "true");
						log_debug("Script run: '%s' returned %d\n", str, cmd);
					} else {
						log_fatal(_("Missing 'command' attribute for an option"));
						xmlSetProp(child, "install", "false");
					}
				} else if ( !strcmp(str, "true") ) {
					cmd = 0;
				}
			}
			mark_cmd_options(info, child, 0);
			if ( exclusive && !cmd ) { /* Stop at the first set option if we're in an exclusive block */
				break;
			}
		} else if ( !strcmp(child->name, "component") ) {
			mark_cmd_options(info, child, 0);
		} else if ( !strcmp(child->name, "exclusive") ) {
			mark_cmd_options(info, child, 1);
		}
	}
}

/* Launch the game using the information in the install info */
install_state launch_game(install_info *info)
{
    char cmd[PATH_MAX];

    if ( info->installed_symlink && info->symlinks_path &&
	 *info->symlinks_path && *info->play_binary ) {
        snprintf(cmd, PATH_MAX, "%s %s", info->play_binary, info->args);
	system(cmd);
    }
    return SETUP_EXIT;
}

static void symlink_desktop_file(install_info *info, const char *file, const char *dir, struct bin_elem *elem)
{
	if ( !access(dir, F_OK) ) {
		char newlink[4096];
		char* bname;
		char* finalbufcopy;
		
		finalbufcopy = strdup(file);
		bname = strrchr(finalbufcopy, '/');
		if ( bname )
			bname ++;
		else
			bname = finalbufcopy;
		snprintf(newlink, sizeof(newlink), 
				 "%s/%s/%s", dir,
				 elem->menu ? elem->menu : "Games",
				 bname);
		
		file_symlink(info, file, newlink);
		free(finalbufcopy);
	}
}

/* Install the desktop menu items; returns a boolean indicating if we should stop
   installing more menu items after this */
int install_menuitems(install_info *info, desktop_type desktop)
{
    const char **tmp_links;
    char buf[PATH_MAX];
    struct bin_elem *elem;
    struct option_elem *opt;
    struct component_elem *comp;
    int ret_val = 0, num_items = 0;
    const char *desk_base;
    char icon_base[PATH_MAX], home_base[PATH_MAX];
    const char *app_links[15];
    const char *exec_script_name = NULL;
    char exec_script[PATH_MAX*2];
    char exec_command[PATH_MAX*2];
    int i = 0;
    int gnome_vfolders = 0;
    FILE *fp;

    switch (desktop) {
    case DESKTOP_MENUDEBIAN:
		app_links[0] = "/usr/lib/menu/";
		app_links[1] = "~/.menu/";
		app_links[2] = NULL;
		break;
	case DESKTOP_REDHAT:
		app_links[0] = "/etc/X11/applnk/";
		app_links[1] = "/usr/share/gnome/apps/"; /* For Ximian stuff */
		app_links[2] = NULL;
		break;
	case DESKTOP_KDE:
		desk_base = getenv("KDE2DIR");
		if (desk_base) {
			snprintf(icon_base, PATH_MAX, "%s/share/applnk/", desk_base); 
			app_links[i++] = icon_base;
		} else {
			app_links[i++] = "/opt/kde2/share/applnk/";
		}
		desk_base = getenv("KDEDIR");
		if (desk_base) {
			snprintf(icon_base, PATH_MAX, "%s/share/applnk/", desk_base); 
			app_links[i++] = icon_base;
		} else {
			app_links[i++] = "/opt/kde/share/applnk/";
		}
		app_links[i++] = "/etc/opt/kde3/share/applnk/SuSE/"; /* SuSE 8.2 */
		app_links[i++] = "/opt/kde3/share/applnk/";
		app_links[i++] = "/opt/kde2/share/applnk/";
		app_links[i++] = "/usr/X11R6/share/applnk/";
		app_links[i++] = "/usr/share/applnk/";
		desk_base = getenv("KDEHOME");
		if (desk_base) {
			snprintf(home_base, sizeof(home_base), "%s/share/applnk/", desk_base);
			app_links[i++] = home_base;
		} else {
			app_links[i++] = "~/.kde/share/applnk-redhat/"; /* for RH 8.0 KDE */
			app_links[i++] = "~/.kde3/share/applnk/";
			app_links[i++] = "~/.kde2/share/applnk/";
			app_links[i++] = "~/.kde/share/applnk/";
		}
		app_links[i++] = NULL;
		break;
	case DESKTOP_GNOME:
		app_links[i++] = "/usr/share/applications/";
		fp = popen("exec 2>&-; if which gnome-config > /dev/null || type -p gnome-config > /dev/null; then gnome-config --prefix; fi", "r");
		if (fp) {
			if ( fscanf(fp, "%s", icon_base) ) {
				strcat(icon_base, "/share/gnome/apps/");
				app_links[i++] = icon_base;
			}
			pclose(fp);
		}
		app_links[i++] = "/opt/gnome/share/gnome/apps/";
		app_links[i++] = "/usr/share/gnome/apps/";
		app_links[i++] = "/usr/local/share/gnome/apps/";
		app_links[i++] = "~/.gnome2/vfolders/applications/";
		app_links[i++] = "~/.gnome/apps/";
		app_links[i++] = NULL;
        /* crude check for gnome 2 vfolders (www.freedesktop.org) */
        if (dir_exists("/etc/gnome-vfs-2.0")) {
            char vfolderBuf[4096];
            gnome_vfolders = 1;

            /* the following sets up a user menuitem directory
			   for rh8 gnome 2, it might work for other gnomes as well.
			   sadly, if this directory is be created, then X must 
			   be restarted before menuitems will be visible */
            expand_home(info, "~/.gnome2/vfolders/applications", vfolderBuf);
            dir_create_hierarchy(info, vfolderBuf, 0755);
        }
		break;
	case DESKTOP_CDE:
		app_links[0] = "/etc/dt/appconfig/types/C/"; /* FIXME: Expand $LANG */
		app_links[1] = "~/.dt/types/";
		app_links[2] = NULL;
		break;
	case DESKTOP_IRIX:
		app_links[0] = "/usr/lib/X11/app-chests/";
		app_links[1] = "/usr/local/X11/app-chests/";
		app_links[2] = "~/.auxchestrc/";
		app_links[3] = NULL;
		break;
	default:
		return ret_val;
    }

    /* Get the exec command we want to use. */
    exec_command[0] = 0;
    exec_script_name = GetDesktopInstall(info);
    if( exec_script_name ) {
		snprintf( exec_script, PATH_MAX*2, "%s %s", exec_script_name, info->install_path );
		fp = popen(exec_script, "r");
		if( fp ) {
			fgets(exec_command, PATH_MAX*2, fp);
			pclose(fp);
		}
    }

    for (comp = info->components_list; comp; comp = comp->next ) {
        for (opt = comp->options_list; opt; opt = opt->next ) {
            for (elem = opt->bin_list; elem; elem = elem->next ) {      
				/* Presumably if there is no icon, no desktop entry */
				if ( (elem->icon == NULL) || (elem->symlink == NULL) ) {
					continue;
				}

                for ( tmp_links = app_links; *tmp_links; ++tmp_links ) {
                    FILE *fp;
                    char finalbuf[PATH_MAX];
					char exec[PATH_MAX*2], icon[PATH_MAX];

                    expand_home(info, *tmp_links, buf);

					/* Locate an accessible directory in the list */
                    if ( access(buf, W_OK) < 0 )
                        continue;

		    if (*exec_command) {
			strncpy(exec, exec_command, sizeof(exec));
		    } else {
			snprintf(exec, sizeof(exec), "%s/%s", info->install_path, elem->file->path);
		    }
		    snprintf(icon, PATH_MAX, "%s/%s", info->install_path, elem->icon);

		    switch ( desktop ) {
		    case DESKTOP_MENUDEBIAN:
			/* Present on newer Mandrake and Debian systems, we just create 
			   a single file */

			/* The directory may exist so we need to check for the command as well */
			if( access("/usr/bin/update-menus", X_OK) < 0 )
			    continue;

			snprintf(finalbuf, PATH_MAX, "%s%s", buf, elem->symlink);
			/* Maybe we should try to group menu entries in a single file, but it
			   can be tricky for components. Maybe per component? */
			fp = fopen(finalbuf, "w");
			if (fp) {
			    fprintf(fp,"?package(local.%s):\\\n"
				    " needs=\"X11\" \\\n"
				    " section=\"%s\" \\\n"
				    " title=\"%s\" \\\n"
				    " longtitle=\"%s\" \\\n"
				    " command=\"%s\" \\\n"
				    " icon=\"%s\"\n",
				    info->name, elem->menu ? elem->menu : "Games",
				    elem->name ? elem->name : info->name,
				    elem->desc ? elem->desc : info->desc,
				    exec, icon);
			    fclose(fp);
			    add_file_entry(info, opt, finalbuf, NULL, 0);
			    ++ num_items;
			    ret_val = 1; /* No need to install anything else */
			} else {
			    log_warning(_("Unable to create desktop file '%s'"), finalbuf);
			}
			break;
		    case DESKTOP_GNOME:
		    case DESKTOP_KDE:
		    case DESKTOP_REDHAT:
				if (desktop == DESKTOP_GNOME && gnome_vfolders) {
					// menu paths are embedded in the file, not the pathname
					snprintf(finalbuf, PATH_MAX, "%s", buf);
				} else {
					snprintf(finalbuf, PATH_MAX, "%s%s/", buf, elem->menu ? elem->menu : "Games");
				}
				file_create_hierarchy(info, finalbuf);

			strncat(finalbuf, elem->symlink, PATH_MAX-strlen(finalbuf));

			if ( desktop == DESKTOP_KDE ) {
			    strncat(finalbuf,".kdelnk", PATH_MAX-strlen(finalbuf));
			} else {
			    strncat(finalbuf,".desktop", PATH_MAX-strlen(finalbuf));
			}

			fp = fopen(finalbuf, "w");
			if (fp) {
			    if (desktop == DESKTOP_KDE) {
					fprintf(fp, "# KDE Config File\n");
			    }
			    fprintf(fp, "[%sDesktop Entry]\n"
						"Encoding=UTF-8\n"
						"Name=%s\n"
						"Comment=%s\n"
						"Exec=%s\n"
						"Icon=%s\n"
						"Terminal=0\n"
						"Type=Application\n"
						"Categories=Application;%s;X-Red-Hat-Base;\n",
				    (desktop==DESKTOP_KDE) ? "KDE " : "",
				    elem->name ? elem->name : info->name,
				    elem->desc ? elem->desc : info->desc,
				    exec, icon, info->category);
			    fclose(fp);
			    add_file_entry(info, opt, finalbuf, NULL, 0);
			    ++ num_items;
			    if (desktop == DESKTOP_REDHAT && info->distro==DISTRO_REDHAT && info->distro_maj >= 8) {
					/* try to create a symlink for redhat 8.0 symlink
					   directory; not strictly necessary but it will
					   allow menuitem to show up in kde without an x
					   restart */
					symlink_desktop_file(info, finalbuf, "/usr/share/applnk-redhat", elem);
			    } else  if (desktop == DESKTOP_GNOME && info->distro==DISTRO_SUSE ) {
					/* Now for some SuSE nonsense */
					symlink_desktop_file(info, finalbuf, "/etc/opt/gnome/SuSE", elem);
				}

			    /* successful REDHAT takes care of KDE/GNOME
			       tell caller no need to continue others
			       UNLESS we are Redhat 6.1 or earlier, in which case we need to install
			       everything else. */
			    if ( (info->distro != DISTRO_REDHAT) || (info->distro_maj>6) || (info->distro_min>1) ) {
					ret_val = (desktop == DESKTOP_REDHAT);
			    }

			} else {
			    log_warning(_("Unable to create desktop file '%s'"), finalbuf);
			}
			break;
		    case DESKTOP_CDE:
			snprintf(finalbuf, PATH_MAX, "%s%s.dt", buf, elem->symlink);
			file_create_hierarchy(info, finalbuf);

			fp = fopen(finalbuf, "w");
			if (fp) {
			    fprintf(fp,
						"ACTION %s\n"
						"{\n"
						"     LABEL         %s\n"
						"     TYPE          COMMAND\n"
						"     EXEC_STRING   %s\n"
						"     ICON          %s\n"
						"     WINDOW_TYPE   NO_STDIO\n"
						"     DESCRIPTION   %s\n"
						"}\n\n", elem->symlink,
						elem->name, exec, icon, elem->desc
						);
			    fclose(fp);
			    add_file_entry(info, opt, finalbuf, NULL, 0);
				
			    snprintf(finalbuf, PATH_MAX, "%s%s.fp", buf, elem->symlink);
			    fp = fopen(finalbuf, "w");
			    if ( fp ) {
					fprintf(fp,
							"CONTROL %s\n"
							"{\n"
							"  TYPE icon\n", elem->symlink);
					if ( num_items == 0 ) {
						fprintf(fp,
								"  CONTAINER_NAME	Top\n"
								"  CONTAINER_TYPE	BOX\n");
					} else {
						fprintf(fp,
								"  CONTAINER_NAME	%s_Panel\n"
								"  CONTAINER_TYPE	SUBPANEL\n", info->name
								);
					}
					fprintf(fp,
							"  ICON				%s\n"
							"  PUSH_ACTION		%s\n"
							"  DROP_ACTION		%s\n"
							"  LABEL			%s\n"
							"}\n\n",
							icon, elem->symlink, elem->symlink, elem->name
							);
					if ( num_items == 0 ) {
						fprintf(fp,
								"SUBPANEL %s_Panel\n"
								"{\n"
								"  CONTAINER_NAME %s\n"
								"  TITLE          %s\n"
								"}\n\n", info->name, elem->symlink, info->desc
								);
					}
					fclose(fp);
					add_file_entry(info, opt, finalbuf, NULL, 0);
					++ num_items;
					ret_val = 1;
			    } else {
					log_warning(_("Unable to create CDE desktop file '%s'"), finalbuf);
			    }
			} else {
			    log_warning(_("Unable to create CDE desktop file '%s'"), finalbuf);
			}

			break;
		    case DESKTOP_IRIX:
			snprintf(finalbuf, PATH_MAX, "%s%02d%s.chest", buf, num_items, elem->symlink);
			file_create_hierarchy(info, finalbuf);

			fp = fopen(finalbuf, "w");
			if (fp) {
			    if ( num_items == 0 ) {
				/* First add an entry for the menu itself */
				fprintf(fp,
					"Menu ToolChest\n"
					"{\n"
					"  no-label  f.separator\n"
					"  \"%s\"    f.menu %s\n"
					"}\n\n",  elem->menu ? elem->menu : "Games",
					info->name
					);
			    }
			    fprintf(fp,
				    "Menu %s\n"
				    "{\n"
				    "  no-label  f.separator\n"
				    "  \"%s\"    f.checkexec \"%s\"\n"
				    "}\n",  info->name,
				    elem->name ? elem->name : info->name, exec
				    );
			    fclose(fp);
			    add_file_entry(info, opt, finalbuf, NULL, 0);
			    ++ num_items;
			    ret_val = 1;
			} else {
			    log_warning(_("Unable to create ToolChest menu file '%s'"), finalbuf);
			}
			break;
		    default:
			break;
		    }
                    /* Created a desktop item, our job is done here */
                    break;
                }
            }
        }
    }

    if ( num_items > 0 ) {
		switch(desktop) {
		case DESKTOP_MENUDEBIAN:
			/* Run update-menus */
            if ( loki_valid_program("update-menus") )
				run_command(info, "update-menus", NULL, 1);
			install_updatemenus_script = 1;
			break;
		case DESKTOP_KDE:
			/* Run kbuildsycoca */
            if ( loki_valid_program("kbuildsycoca") )
				run_command(info, "kbuildsycoca", NULL, 0);
			install_updatemenus_script = 1;
			break;
		case DESKTOP_CDE:
			/* Run dtaction */
            if ( loki_valid_program("dtaction") )
				run_command(info, "dtaction", "RestorePanel", 0);
			install_updatemenus_script = 1;
			break;
		default:
			break;
		}
		if ( loki_valid_program("susewm") )
	    	run_command(info, "susewm", "-q", 0);
    }
    return ret_val;
}

/* Run some shell script commands */
int run_script(install_info *info, const char *script, int arg, int include_tags)
{
    char script_file[PATH_MAX];
    int fd;
    int exitval;
    char working_dir[PATH_MAX];
    
    /* We need to append the working directory onto the script name so
       it can always be found. Do this only if the script file exists
       (to avoid problems with 'sh script.sh')
    */
    working_dir[0] = '\0'; 
    if ( access(script, R_OK) == 0 ) {
        if ( getcwd(working_dir, sizeof(working_dir)) == NULL ) {
			perror("run_script: getcwd");
		}
        strncat(working_dir, "/", sizeof(working_dir)-strlen(working_dir));
    }

    snprintf(script_file, PATH_MAX, "%s/tmp_script_XXXXXX", info->install_path);
    fd = mkstemp(script_file);
    if ( fd < 0 ) { /* Maybe the install directory didn't exist? */
        /* This is necessary for some multi-package installs */
        snprintf(script_file, PATH_MAX, "/tmp/tmp_script_XXXXXX");
        fd = mkstemp(script_file);
    }
    exitval = -1;
    if ( fd >= 0 ) {
        FILE *fp;
        char cmd[PATH_MAX];

        fp = fdopen(fd, "w");
        if ( fp ) {
            fprintf(fp, /* Create script file, setting environment variables */
					"#!/bin/sh\n"
					"SETUP_PRODUCTNAME=\"%s\"\n"
					"SETUP_PRODUCTVER=\"%s\"\n"
					"SETUP_INSTALLPATH=\"%s\"\n"
					"SETUP_SYMLINKSPATH=\"%s\"\n"
					"SETUP_CDROMPATH=\"%s\"\n"
					"SETUP_DISTRO=\"%s\"\n"
					"SETUP_REINSTALL=\"%s\"\n"
					"export SETUP_PRODUCTNAME SETUP_PRODUCTVER SETUP_INSTALLPATH SETUP_SYMLINKSPATH SETUP_CDROMPATH SETUP_DISTRO SETUP_REINSTALL\n",
					info->name, info->version,
					info->install_path,
					info->symlinks_path,
					info->cdroms_list ? info->cdroms_list->mounted : "",
					info->distro ? distribution_symbol[info->distro] : "",
					info->options.reinstalling ? "1" : "0");

			if ( include_tags )
				fprintf(fp, 
						"SETUP_OPTIONTAGS=\"%s\"\n"
						"export SETUP_OPTIONTAGS\n", 
						get_optiontags_string(info));
			/* Append script itself */
			fprintf(fp, "%s%s\n", 
					working_dir, script);
            fchmod(fileno(fp),0755); /* Turn on executable bit */
            fclose(fp);
			if ( arg >= 0 ) {
				snprintf(cmd, sizeof(cmd), "%d", arg);
			} else {
				strncpy(cmd, info->install_path, sizeof(cmd));
			}
           
            exitval = run_command(info, script_file, cmd, 1);
        }
        close(fd);
    }
	unlink(script_file);
    return(exitval);
}

int run_command(install_info *info, const char *cmd, const char *arg, int warn)
{
    int exitval = 0;
    pid_t child;

	log_debug("Running command: '%s' '%s'\n", cmd, arg ? arg : "");
    switch( child = fork() ) {
    case 0: {/* Inside the child */
        char *argv[3];
        argv[0] = strdup(cmd);
        argv[1] = arg ? strdup(arg) : NULL;
        argv[2] = NULL;
        execvp(cmd, argv);
		if ( warn ) {
			perror("execv");
			fprintf(stderr, "Command: %s\n", cmd);
		}
        free(argv[0]);
		if(arg)
			free(argv[1]);
		_exit(1);
    }
    case -1: /* Error */
        perror("fork");
        break;
    default: /* Parent */
        while ( waitpid(child, &exitval, WNOHANG) == 0 ) {
            if ( UI.idle ) {
                UI.idle(info); /* Run an idle loop while the command is running */
            } else {
                usleep(10000);
            }
        }
        if ( WIFEXITED(exitval) ) {
            exitval = WEXITSTATUS(exitval);
        } else {
            exitval = 1;
        }
        break;
    }
    return exitval;
}

/* Convenience functions to quickly change back and forth between current directories */

#define MAX_CURDIRS 10

static char curdirs[PATH_MAX][MAX_CURDIRS];
static int curdir_index = 0;

void push_curdir(const char *path)
{
    if (curdir_index >= MAX_CURDIRS) {
        fprintf(stderr,"ERROR: Too many curdirs. FIX IT!\n");
    } else {
        if ( getcwd(curdirs[curdir_index++], PATH_MAX) == NULL )
			perror("getcwd");
        if( chdir(path) < 0)
            fprintf(stderr, "chdir(push: %s): %s\n", path, strerror(errno));
    }
}

void pop_curdir(void)
{
    if (curdir_index>0) {
        if(chdir(curdirs[--curdir_index])<0)
            fprintf(stderr, "chdir(pop: %s): %s\n", curdirs[curdir_index], strerror(errno));
    }
}
