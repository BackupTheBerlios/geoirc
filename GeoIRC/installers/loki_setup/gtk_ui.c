/* GTK-based UI
   $Id: gtk_ui.c,v 1.1 2003/10/14 17:00:24 mynameisneo Exp $
*/

/* Modifications by Borland/Inprise Corp.
   04/11/2000: Added check in check_install_button to see if install and
               binary path are the same. If so, leave install button
	       disabled and give user a message.

   04/17/2000: Created two new GladeXML objects, one for the readme dialog,
               the other for the license dialog and modified gtkui_init,
	       setup_button_view_readme_slot and gtkui_license to create &
	       use these new objects. Created 2 new handlers for destroy
	       events on the readme and license dialogs. This was done to fix
	       problems when the user uses the 'X' button in the upper
	       right corner instead of the Close, Cancel or Agree buttons.
	       For the readme, setup would seg fault if the user tried to open
	       the readme dialog a second time. For the license, setup would
	       stop responding.

	       The setup.glade file was  modified to include destroy
	       handlers for the readme_dialog and license_dialog widgets.

	       Added code to gtkui_complete to clean up the GladeXML objects.

  04/21/2000:  Cleaned up a bit too much, too soon in gtkui_complete. 
               Removed the gtk_object_unref for setup_glade because it's 
	       still in use by the Play Now button if uid is root...

  04/28/2000:  More cleanup problems. Can't unref the gtk objects in 
               gtkui_complete...too much is still in use. Maybe make a cleanup
	       routine that gets called by the exit and play button handlers.
	       
	       Added code to disable the View Readme button (all 3 of them)
	       when it is clicked. That way, the user can have only 1 instance
	       of the readme dialog. Multiple instances was causing a problem
	       for the destroy routine. Destroying the latest instance 
	       worked OK. Destroying the others caused a seg fault. Readme
	       buttons are re-enabled when the dialog is closed.

	       Cleaned up close_view_readme_slot to avoid the duplication with
	       destroy_view_readme. close now just calls destroy.

  05/12/2000:  Changed the way the focus mechanism works for the install path
               and binary path fields and how the Begin Install button gets
	       enabled/disabled. There were ways in which an invalid path 
	       could be selected from the combo boxes and the Begin Install
	       button would not be disabled until the user clicked on it. Then,
	       the mouse focus was on an insensitive item, making it appear 
	       that the UI locked up. Here's the changes:

	       Modified gtkui_init to add signal handlers for the keypress and
	       mouse button release events for both the install path and binary
	       path fields. Also added these functions to handle the signals:
	       path_entry_keypress_slot, path_combo_change_slot,
	       binary_path_entry_keypress_slot, binary_path_combo_change_slot.
	       The "keypress" slots evaluate the status of the Begin Install
	       button after every keystroke when the user is typing a pathname.
	       The "combo_change" slots evaluate the status of the button when-
	       ever the user makes a selection from the drop-down list.

	       Modified setup.glade to remove the signal handlers that grabbed
	       the focus when the mouse was passed over these fields.

	       Modified gtkui_prompt to add a RESPONSE_OK option and modifed
	       the yesno_answer structure to support this. This will display
	       a dialog with a message and a single OK button. This was a 
	       result of one of the (many) failed attempts to fix the focus 
	       issue. It turns out that I don't use this anywhere anymore, but 
	       I figured I'd leave it for someone who wants to display a 
	       simple message without using the gnome libs. To use it, just 
	       pass RESPONSE_OK as the last parameter.

  05/17/2000:  Modified gtkui_init to disable the install path or binary path
               widgets if the value was passed in as a command-line parameter
	       (see main.c for the new -i and -b options).

  06/02/2000:  Modified gtkui_update to make sure the progress bar object is
               never sent an invalid value. If the <option size="xx"> value is
	       not set correctly, then the calculated new_update value could
	       sometimes be greater than 1. This generated lots of gtk error
	       messages on the console. Added an if/else statement to make
	       sure the calculated value never exceeds 1.
*/

#include <limits.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <sys/stat.h>
#include <ctype.h>
#include <gtk/gtk.h>
#include <glade/glade.h>

#include "install.h"
#include "install_ui.h"
#include "install_log.h"
#include "detect.h"
#include "file.h"
#include "copy.h"
#include "loki_launchurl.h"

#define SETUP_GLADE SETUP_BASE "setup.glade"

#define LICENSE_FONT            \
        "-misc-fixed-medium-r-semicondensed-*-*-120-*-*-c-*-iso8859-8"

#define MAX_TEXTLEN	40	/* The maximum length of current filename */

/* Globals */

static char *default_install_paths[] = {
    "/usr/local/games",
    "/opt/games",
    "/usr/games",
    NULL
};

/* Use an arbitrary maximum; the lack of
   flexibility shouldn't be an issue */
#define MAX_INSTALL_PATHS       25
 
static char *install_paths[MAX_INSTALL_PATHS];

/* Various warning dialogs */
static enum {
    WARNING_NONE,
    WARNING_ROOT
} warning_dialog;

typedef enum
{
	CLASS_PAGE,
    OPTION_PAGE,
    COPY_PAGE,
    DONE_PAGE,
    ABORT_PAGE,
    WARNING_PAGE,
    WEBSITE_PAGE
} InstallPages;

static GladeXML *setup_glade = NULL;
static GladeXML *setup_glade_readme = NULL;
static GladeXML *setup_glade_license = NULL;
static int cur_state;
static install_info *cur_info;
static int diskspace;
static int license_okay = 0;
static gboolean in_setup = TRUE;
static GSList *radio_list = NULL; /* Group for the radio buttons */

/******** Local prototypes **********/

static const char *check_for_installation(install_info *info);
static void check_install_button(void);
static void update_space(void);
static void update_size(void);
void setup_destroy_view_readme_slot(GtkWidget*, gpointer);
static yesno_answer gtkui_prompt(const char*, yesno_answer);
static void gtkui_abort(install_info *info);

static int iterate_for_state(void)
{
	int start = cur_state;
	while(cur_state == start) {
		if ( !gtk_main_iteration() )
			break;
	}

	/* fprintf(stderr,"New state: %d\n", cur_state); */
	return cur_state;
}

/*********** GTK slots *************/

/*void setup_entry_gainfocus( GtkWidget* widget, gpointer func_data )
{
    gtk_window_set_focus(GTK_WINDOW(gtk_widget_get_toplevel(widget)), widget);
}
void setup_entry_givefocus( GtkWidget* widget, gpointer func_data )
{
    gtk_window_set_focus(GTK_WINDOW(gtk_widget_get_toplevel(widget)), NULL);
}*/

gint path_entry_keypress_slot(GtkWidget *widget, GdkEvent *event, 
				  gpointer data)
{
    char* string;
    
    string = gtk_entry_get_text( GTK_ENTRY(widget) );
    if ( string ) {
        set_installpath(cur_info, string);
        if ( strcmp(string, cur_info->install_path) != 0 ) {
            gtk_entry_set_text(GTK_ENTRY(widget), cur_info->install_path);
        }
        update_space();
    }
    return(TRUE);
}

gint path_combo_change_slot(GtkWidget *widget, GdkEvent *event, 
				  gpointer data)
{
    char* string;
    GtkWidget *install_entry;
    
    install_entry = glade_xml_get_widget(setup_glade, "install_entry");
    string = gtk_entry_get_text( GTK_ENTRY(install_entry) );
    if ( string ) {
        set_installpath(cur_info, string);
        if ( strcmp(string, cur_info->install_path) != 0 ) {
            gtk_entry_set_text(GTK_ENTRY(install_entry), cur_info->install_path);
        }
        update_space();
    }
    return(FALSE);
}

void setup_entry_installpath_slot( GtkWidget* widget, gpointer func_data )
{
    char* string;
    string = gtk_entry_get_text( GTK_ENTRY(widget) );
    if ( string ) {
        set_installpath(cur_info, string);
        if ( strcmp(string, cur_info->install_path) != 0 ) {
            gtk_entry_set_text(GTK_ENTRY(widget), cur_info->install_path);
        }
        update_space();
    }
}

gint binary_path_entry_keypress_slot(GtkWidget *widget, GdkEvent *event, 
				  gpointer data)
{
    char* string;
    
    string = gtk_entry_get_text( GTK_ENTRY(widget) );
    if ( string ) {
        set_symlinkspath(cur_info, string);
        if ( strcmp(string, cur_info->symlinks_path) != 0 ) {
            gtk_entry_set_text(GTK_ENTRY(widget), cur_info->symlinks_path);
        }
        check_install_button();
    }    
    return(TRUE);
}

gint binary_path_combo_change_slot(GtkWidget *widget, GdkEvent *event, 
				  gpointer data)
{
    char* string;
    GtkWidget *binary_entry;
    
    binary_entry = glade_xml_get_widget(setup_glade, "binary_entry");
    string = gtk_entry_get_text( GTK_ENTRY(binary_entry) );
    if ( string ) {
        set_symlinkspath(cur_info, string);
        if ( strcmp(string, cur_info->symlinks_path) != 0 ) {
            gtk_entry_set_text(GTK_ENTRY(widget), cur_info->symlinks_path);
        }
        check_install_button();
    }    
    return(FALSE);
}

void setup_entry_binarypath_slot( GtkWidget* widget, gpointer func_data )
{
    char* string;
    string = gtk_entry_get_text( GTK_ENTRY(widget) );
    if ( string ) {
        set_symlinkspath(cur_info, string);
        if ( strcmp(string, cur_info->symlinks_path) != 0 ) {
            gtk_entry_set_text(GTK_ENTRY(widget), cur_info->symlinks_path);
        }
        check_install_button();
    }
}

/*
   Returns 0 if fails.
*/
static gboolean load_file( GtkText *widget, GdkFont *font, const char *file )
{
    FILE *fp;
    int pos;
    
    gtk_editable_delete_text(GTK_EDITABLE(widget), 0, -1);
    fp = fopen(file, "r");
    if ( fp ) {
        char line[BUFSIZ];
        pos = 0;
        while ( fgets(line, BUFSIZ-1, fp) ) {
            gtk_text_insert(widget, font, NULL, NULL, line, strlen(line));
        }
        fclose(fp);
    }
    gtk_editable_set_position(GTK_EDITABLE(widget), 0);

    return (fp != NULL);
}

void on_class_continue_clicked( GtkWidget  *w, gpointer data )
{
	GtkWidget *widget = glade_xml_get_widget(setup_glade, "recommended_but");

	if ( cur_state != SETUP_CLASS ) {
		return;
	}
	express_setup = gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(widget));

	if ( express_setup ) {
		const char *msg = check_for_installation(cur_info);
		if ( msg ) {
			char buf[BUFSIZ];
			snprintf(buf, sizeof(buf),
					 _("Installation could not proceed due to the following error:\n%s\nTry to use 'Expert' installation."), 
					 msg);
			gtkui_prompt(buf, RESPONSE_OK);
			widget = glade_xml_get_widget(setup_glade, "expert_but");
			gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(widget), TRUE);
			return;
		}
	}
	/* Install desktop menu items */
	if ( !GetProductHasNoBinaries(cur_info)) {
		cur_info->options.install_menuitems = 1;
	}
	widget = glade_xml_get_widget(setup_glade, "setup_notebook");
	gtk_notebook_set_page(GTK_NOTEBOOK(widget), OPTION_PAGE);
	cur_state = SETUP_OPTIONS;
}

void setup_close_view_readme_slot( GtkWidget* w, gpointer data )
{  
    setup_destroy_view_readme_slot(w, data);
}

void setup_destroy_view_readme_slot( GtkWidget* w, gpointer data )
{
    GtkWidget *widget;

    if ( setup_glade_readme ) {
        widget = glade_xml_get_widget(setup_glade_readme, "readme_dialog");
        gtk_widget_hide(widget);
        gtk_object_unref(GTK_OBJECT(setup_glade_readme));
        setup_glade_readme = NULL;
        /*
         * re-enable the 'view readme buttons...all 3 of them since we don't
         * know where we are
         */
        widget = glade_xml_get_widget(setup_glade, "button_readme");
        gtk_widget_set_sensitive(widget, TRUE);
        widget = glade_xml_get_widget(setup_glade, "class_readme");
        gtk_widget_set_sensitive(widget, TRUE);
        widget = glade_xml_get_widget(setup_glade, "view_readme_progress_button");
        gtk_widget_set_sensitive(widget, TRUE);
        widget = glade_xml_get_widget(setup_glade, "view_readme_end_button");
        gtk_widget_set_sensitive(widget, TRUE);
    }
}

void setup_button_view_readme_slot( GtkWidget* w, gpointer data )
{
    GtkWidget *readme;
    GtkWidget *widget;
    const char *file;
    
    setup_glade_readme = glade_xml_new(SETUP_GLADE, "readme_dialog");
    glade_xml_signal_autoconnect(setup_glade_readme);
    readme = glade_xml_get_widget(setup_glade_readme, "readme_dialog");
    widget = glade_xml_get_widget(setup_glade_readme, "readme_area");
    file = GetProductREADME(cur_info, NULL);
    if ( file && readme && widget ) {
        gtk_widget_hide(readme);
        load_file(GTK_TEXT(widget), NULL, file);
        gtk_widget_show(readme);
		/* there are 3 'view readme' buttons...disable all of them */
		widget = glade_xml_get_widget(setup_glade, "button_readme");
		gtk_widget_set_sensitive(widget, FALSE);
		widget = glade_xml_get_widget(setup_glade, "class_readme");
		gtk_widget_set_sensitive(widget, FALSE);
		widget = glade_xml_get_widget(setup_glade, "view_readme_progress_button");
		gtk_widget_set_sensitive(widget, FALSE);
		widget = glade_xml_get_widget(setup_glade, "view_readme_end_button");
		gtk_widget_set_sensitive(widget, FALSE);
    }
}

void setup_button_license_agree_slot( GtkWidget* widget, gpointer func_data )
{
    GtkWidget *license;

    license = glade_xml_get_widget(setup_glade_license, "license_dialog");
    gtk_widget_hide(license);
    license_okay = 1;
    check_install_button();
    cur_state = SETUP_README;
}

void setup_destroy_license_slot( GtkWidget* w, gpointer data )
{
    GtkWidget *widget;
    
    widget = glade_xml_get_widget(setup_glade_license, "license_dialog");
    gtk_widget_hide(widget);
    cur_state = SETUP_EXIT;
    gtk_object_unref(GTK_OBJECT(setup_glade_license));
}

void setup_button_warning_continue_slot( GtkWidget* widget, gpointer func_data )
{
    switch (warning_dialog) {
        case WARNING_NONE:
            break;
        case WARNING_ROOT:
            cur_state = SETUP_PLAY;
            break;
    }
    warning_dialog = WARNING_NONE;
}
void setup_button_warning_cancel_slot( GtkWidget* widget, gpointer func_data )
{
    switch (warning_dialog) {
        case WARNING_NONE:
            break;
        case WARNING_ROOT:
            cur_state = SETUP_EXIT;
            break;
    }
    warning_dialog = WARNING_NONE;
}

void setup_button_complete_slot( GtkWidget* _widget, gpointer func_data )
{
    cur_state = SETUP_COMPLETE;
}

void setup_button_play_slot( GtkWidget* _widget, gpointer func_data )
{
	/* Enable this only if the application actually does that */
#if 0
    if ( getuid() == 0 ) {
		GtkWidget *widget;
		const char *warning_text =
			_("If you run this as root, the preferences will be stored in\n"
			  "root's home directory instead of your user account directory.");

        warning_dialog = WARNING_ROOT;
        widget = glade_xml_get_widget(setup_glade, "setup_notebook");
        gtk_notebook_set_page(GTK_NOTEBOOK(widget), WARNING_PAGE);
        widget = glade_xml_get_widget(setup_glade, "warning_label");
        gtk_label_set_text(GTK_LABEL(widget), warning_text);
    } else
#endif
        cur_state = SETUP_PLAY;
}

void setup_button_exit_slot( GtkWidget* widget, gpointer func_data )
{
	cur_state = SETUP_EXIT;
}

void setup_button_abort_slot( GtkWidget* widget, gpointer func_data )
{
	/* Make sure that the state will be different so that we can iterate */
	cur_state = (cur_state == SETUP_ABORT) ? SETUP_EXIT : SETUP_ABORT;
}

void setup_button_cancel_slot( GtkWidget* widget, gpointer func_data )
{
	switch(cur_state) {
	case SETUP_COMPLETE:
	case SETUP_OPTIONS:
	case SETUP_ABORT:
	case SETUP_EXIT:
		cur_state = SETUP_EXIT;
		break;
	default:
		if ( gtkui_prompt(_("Are you sure you want to abort\nthis installation?"), RESPONSE_NO) == RESPONSE_YES ) {
			cur_state = SETUP_ABORT;
			abort_install();
		}
		break;
	}
}

void setup_button_install_slot( GtkWidget* widget, gpointer func_data )
{
    GtkWidget *notebook;

    notebook = glade_xml_get_widget(setup_glade, "setup_notebook");
    gtk_notebook_set_page(GTK_NOTEBOOK(notebook), COPY_PAGE);
    cur_state = SETUP_INSTALL;
}

void setup_button_browser_slot( GtkWidget* widget, gpointer func_data )
{
    /* Don't let the user accidentally double-launch the browser */
    gtk_widget_set_sensitive(widget, FALSE);

    launch_browser(cur_info, loki_launchURL);
}

/* Returns NULL if installation can be performed */
static const char *check_for_installation(install_info *info)
{
    if ( ! license_okay ) {
        return _("Please respond to the license dialog");
    }
	return IsReadyToInstall(info);
}

/* Checks if we can enable the "Begin install" button */
static void check_install_button(void)
{
    const char *message;
    GtkWidget *options_status;
    GtkWidget *button_install;

	message = check_for_installation(cur_info);

    /* Get the appropriate widgets and set the new state */
    options_status = glade_xml_get_widget(setup_glade, "options_status");
    button_install = glade_xml_get_widget(setup_glade, "button_install");
    if ( message ) {
        gtk_widget_set_sensitive(button_install, 0);
    } else {
        message = _("Ready to install!");
        gtk_widget_set_sensitive(button_install, 1);
    }
    gtk_label_set_text(GTK_LABEL(options_status), message);
}

static void update_size(void)
{
    GtkWidget *widget;
    char text[32];

    widget = glade_xml_get_widget(setup_glade, "label_install_size");
    if ( widget ) {
        snprintf(text, sizeof(text), _("%ld MB"), BYTES2MB(cur_info->install_size));
        gtk_label_set_text(GTK_LABEL(widget), text);
        check_install_button();
    }
}

static void update_space(void)
{
    GtkWidget *widget;
    char text[32];

    widget = glade_xml_get_widget(setup_glade, "label_free_space");
    if ( widget ) {
        diskspace = detect_diskspace(cur_info->install_path);
        snprintf(text, sizeof(text), _("%d MB"), diskspace);
        gtk_label_set_text(GTK_LABEL(widget), text);
        check_install_button();
    }
}

static void empty_container(GtkWidget *widget, gpointer data)
{
    gtk_container_remove(GTK_CONTAINER(data), widget);
}

static void enable_tree(xmlNodePtr node, GtkWidget *window)
{
  if ( strcmp(node->name, "option") == 0 ) {
	  GtkWidget *button = (GtkWidget*)gtk_object_get_data(GTK_OBJECT(window),
														  get_option_name(cur_info, node, NULL, 0));
	  if(button)
		  gtk_widget_set_sensitive(button, TRUE);
  }
  node = node->childs;
  while ( node ) {
	  enable_tree(node, window);
	  node = node->next;
  }
}

/*-----------------------------------------------------------------------------
**  on_use_binary_toggled
**      Signal function to repsond to a toggle state in the
** 'Use symbolic link' checkbox.
**---------------------------------------------------------------------------*/
void on_use_binary_toggled ( GtkWidget* widget, gpointer func_data)
{
    GtkWidget *binary_path_widget;
    GtkWidget *binary_label_widget;
    GtkWidget *binary_entry;
    char *string;

    /*-------------------------------------------------------------------------
    ** Pick up widget handles
    **-----------------------------------------------------------------------*/
    binary_path_widget = glade_xml_get_widget(setup_glade, "binary_path");
    binary_label_widget = glade_xml_get_widget(setup_glade, "binary_label");

    /*-------------------------------------------------------------------------
    ** Mark the appropriate widgets active or inactive
    **-----------------------------------------------------------------------*/
    gtk_widget_set_sensitive(binary_path_widget, GTK_TOGGLE_BUTTON(widget)->active);
    gtk_widget_set_sensitive(binary_label_widget, GTK_TOGGLE_BUTTON(widget)->active);

    /*-------------------------------------------------------------------------
    ** Finally, set the symlinks_path.  If we've made it active
    **      again, we have to go get the current binary entry box
    **      value and restash it into the global symlinkspath.
    **-----------------------------------------------------------------------*/
    if (GTK_TOGGLE_BUTTON(widget)->active) {
        binary_entry = glade_xml_get_widget(setup_glade, "symlink_entry");
        string = gtk_entry_get_text( GTK_ENTRY(binary_entry) );
    } else {
        string = NULL;
	}
    set_symlinkspath(cur_info, string ? string : "");
    check_install_button();
}


void setup_checkbox_option_slot( GtkWidget* widget, gpointer func_data)
{
	GtkWidget *window;
	xmlNodePtr node, data_node = (xmlNodePtr) func_data; //gtk_object_get_data(GTK_OBJECT(widget),"data");
	
	if(!data_node)
		return;
	
	window = glade_xml_get_widget(setup_glade, "setup_window");

	if ( GTK_TOGGLE_BUTTON(widget)->active ) {
		const char *warn = get_option_warn(cur_info, data_node);

		/* does this option require a seperate EULA? */
		xmlNodePtr child;
		child = data_node->childs;
		while(child)
		{
			if (!strcmp(child->name, "eula"))
			{
				/* this option has some EULA nodes
				 * we need to prompt before this change can be validated / turned on
				 */
				const char* name = GetProductEULANode(cur_info, data_node, NULL);
				if (name)
				{
					GtkWidget *license;
					GtkWidget *license_widget;
					
					if (!setup_glade_license)
						setup_glade_license = glade_xml_new(SETUP_GLADE, "license_dialog");
					glade_xml_signal_autoconnect(setup_glade_license);
					license = glade_xml_get_widget(setup_glade_license, "license_dialog");
					license_widget = glade_xml_get_widget(setup_glade_license, "license_area");
					if ( license && license_widget ) {
						GdkFont *font;
						install_state start;
						
						font = gdk_font_load(LICENSE_FONT);
						gtk_widget_hide(license);
						load_file(GTK_TEXT(license_widget), font, name);
						gtk_widget_show(license);
						gtk_window_set_modal(GTK_WINDOW(license), TRUE);
						
						start = cur_state; /* happy hacking */
						license_okay = 0;
						iterate_for_state();
						cur_state = start;

						gtk_widget_hide(license);
						if (!license_okay)
						{
							/* the user doesn't accept the option EULA, leave this option disabled */
							license_okay = 1; /* put things back in order regarding the product EULA */
							gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(widget), FALSE);
							return;
						}
						license_okay = 1;
						break;
					}
				}
				else
				{
					log_warning("option-specific EULA not found, can't set option on\n");
					/* EULA not found 	or not accepted */
					gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(widget), FALSE);
					return;
				}
			}
			child = child->next;
		}
		
		if ( warn && !in_setup ) { /* Display a warning message to the user */
			gtkui_prompt(warn, RESPONSE_OK);
		}

		/* Mark this option for installation */
		mark_option(cur_info, data_node, "true", 0);
		
		/* Recurse down any other options to re-enable grayed out options */
		node = data_node->childs;
		while ( node ) {
			enable_tree(node, window);
			node = node->next;
		}
	} else {
		/* Unmark this option for installation */
		mark_option(cur_info, data_node, "false", 1);
		
		/* Recurse down any other options */
		node = data_node->childs;
		while ( node ) {
			if ( !strcmp(node->name, "option") ) {
				GtkWidget *button;
				
				button = (GtkWidget*)gtk_object_get_data(GTK_OBJECT(window),
														 get_option_name(cur_info, node, NULL, 0));
				if(button){ /* This recursively calls this function */
					gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(button), FALSE);
					gtk_widget_set_sensitive(button, FALSE);
				}
			} else if ( !strcmp(node->name, "exclusive") ) {
				xmlNodePtr child;
				for ( child = node->childs; child; child = child->next) {
					GtkWidget *button;
					
					button = (GtkWidget*)gtk_object_get_data(GTK_OBJECT(window),
															 get_option_name(cur_info, child, NULL, 0));
					if(button){ /* This recursively calls this function */
						gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(button), FALSE);
						gtk_widget_set_sensitive(button, FALSE);
					}
				}
			}
			node = node->next;
		}
	}
    cur_info->install_size = size_tree(cur_info, cur_info->config->root->childs);
	update_size();
}

void setup_checkbox_menuitems_slot( GtkWidget* widget, gpointer func_data)
{
    cur_info->options.install_menuitems = (GTK_TOGGLE_BUTTON(widget)->active != 0);
}

static yesno_answer prompt_response;

static void prompt_yesbutton_slot( GtkWidget* widget, gpointer func_data)
{
    prompt_response = RESPONSE_YES;
}

static void prompt_nobutton_slot( GtkWidget* widget, gpointer func_data)
{
    prompt_response = RESPONSE_NO;
}

static void prompt_okbutton_slot( GtkWidget* widget, gpointer func_data)
{
    prompt_response = RESPONSE_OK;
}

static yesno_answer gtkui_prompt(const char *txt, yesno_answer suggest)
{
    GtkWidget *dialog, *label, *yes_button, *no_button, *ok_button;
       
    /* Create the widgets */
    
    dialog = gtk_dialog_new();
    label = gtk_label_new (txt);
	gtk_misc_set_padding(GTK_MISC(label), 8, 8);
    ok_button = gtk_button_new_with_label(_("OK"));

    prompt_response = RESPONSE_INVALID;
    
    /* Ensure that the dialog box is destroyed when the user clicks ok. */
    
    gtk_signal_connect_object (GTK_OBJECT (ok_button), "clicked",
                               GTK_SIGNAL_FUNC (prompt_okbutton_slot), GTK_OBJECT(dialog));

	gtk_signal_connect_object(GTK_OBJECT(dialog), "delete-event",
							  GTK_SIGNAL_FUNC(prompt_nobutton_slot), GTK_OBJECT(dialog));

    if (suggest != RESPONSE_OK) {
		yes_button = gtk_button_new_with_label(_("Yes"));
		no_button = gtk_button_new_with_label(_("No"));

		gtk_signal_connect_object (GTK_OBJECT (yes_button), "clicked",
								   GTK_SIGNAL_FUNC (prompt_yesbutton_slot), GTK_OBJECT(dialog));
		gtk_signal_connect_object (GTK_OBJECT (no_button), "clicked",
								   GTK_SIGNAL_FUNC (prompt_nobutton_slot), GTK_OBJECT(dialog));

        gtk_container_add (GTK_CONTAINER (GTK_DIALOG(dialog)->action_area),
                           yes_button);
        gtk_container_add (GTK_CONTAINER (GTK_DIALOG(dialog)->action_area),
                           no_button);
    } else {
        gtk_container_add (GTK_CONTAINER (GTK_DIALOG(dialog)->action_area),
                           ok_button);
    }
    
    /* Add the label, and show everything we've added to the dialog. */
    
    gtk_container_add (GTK_CONTAINER (GTK_DIALOG(dialog)->vbox),
                       label);
    gtk_window_set_title(GTK_WINDOW(dialog), _("Setup"));
    gtk_window_set_modal(GTK_WINDOW(dialog), TRUE);
    gtk_window_set_position(GTK_WINDOW(dialog), GTK_WIN_POS_CENTER);
    gtk_widget_show_all (dialog);

    while ( prompt_response == RESPONSE_INVALID ) {
        gtk_main_iteration();
    }
    gtk_widget_destroy(dialog);
    return prompt_response;
}

static inline int str_in_g_list(const char *str, GList *list)
{
    /* See if the item is already in our list */
    int i;
    char *path_elem;
    for ( i=0; (path_elem= (char *) g_list_nth_data(list, i)) != NULL; ++i ) {
        if ( strcmp(path_elem, str) == 0 ) {
            return 1;  /* it's in the list. */
        }
    }

    return 0;  /* not in the list. */
}

static void init_install_path(void)
{
    GtkWidget* widget;
    GList* list;
    int i;
    char path[PATH_MAX];
    xmlNodePtr node;
    char *homedir = getenv("HOME");

    widget = glade_xml_get_widget(setup_glade, "install_path");
    
    if ( GetProductIsMeta(cur_info) ) {
		gtk_widget_hide(widget);
		return;
    }

    list = NULL;
    if (access(cur_info->install_path, R_OK) == 0)
        list = g_list_append( list, cur_info->install_path);

    /*----------------------------------------------------------------------
    **  Retrieve the list of install paths from the config file, if we can
    **--------------------------------------------------------------------*/
    for (i = 0, node = cur_info->config->root->childs; node;  node = node->next) {
        if (strcmp(node->name, "install_drop_list") == 0) {
            /* Retrieve the value - note that it's up to us to free
                the memory, which means we can use it without copying it */
            char *content = xmlNodeGetContent(node);
            char *p;
            if (content) {
                for (p = strtok(content, "\n\t \r\b"); p; 
					 p = strtok(NULL,"\n\t \r\b" )) {
                    /*--------------------------------------------------------
                    **  Expand any ~s in the path
                    **------------------------------------------------------*/
                    char *temp_buf;
                    temp_buf = malloc(PATH_MAX);
                    if (! temp_buf) {
                        fprintf(stderr, _("Fatal error:  out of memory\n"));
                        return;
                    }
                    expand_home(cur_info, p, temp_buf);

                    if (i > sizeof(install_paths) > sizeof(install_paths[0])) {
                        fprintf(stderr, 
								_("Error: maximum of %d install_path entries exceeded\n"),
								sizeof(install_paths) > sizeof(install_paths[0]));
                        return; 
                    }

                    install_paths[i++] = temp_buf;
                }
            }
        }
    }

    
    /*----------------------------------------------------------------------
    **  If no installation paths were specified, use the default
    **      values that are hard coded in.
    **--------------------------------------------------------------------*/
    if (i == 0) {
        for (i = 0; default_install_paths[i]; ++i) {
            install_paths[i] = default_install_paths[i];
		}
	}

    /*----------------------------------------------------------------------
    **  Add in the home directory, as a last-resort.
    **--------------------------------------------------------------------*/
    if (homedir != NULL)
        install_paths[i++] = homedir;

    /*----------------------------------------------------------------------
    **  Terminate the array
    **--------------------------------------------------------------------*/
    install_paths[i] = NULL;

    /*----------------------------------------------------------------------
    **  Now translate the default install paths into the gtk list,
    **      avoiding the current default value (which is already in the list)
    **--------------------------------------------------------------------*/
    for ( i=0; install_paths[i]; ++i ) {
        snprintf(path, sizeof(path), "%s/%s", install_paths[i], GetProductName(cur_info));
        if ((!str_in_g_list(path, list)) && (access(install_paths[i], W_OK) == 0)) {
            list = g_list_append( list, strdup(path));
        }
    }
    gtk_combo_set_popdown_strings( GTK_COMBO(widget), list );
    /* !!! FIXME: Should we g_list_free ( list ) or not? */
    
    /*gtk_entry_set_text( GTK_ENTRY(GTK_COMBO(widget)->entry), cur_info->install_path );*/
    gtk_entry_set_text( GTK_ENTRY(GTK_COMBO(widget)->entry), list->data );

    /* cheat. Make the first entry the default for IsReadyToInstall(). */
    strncpy(cur_info->install_path, list->data, sizeof (cur_info->install_path));

    gtk_combo_set_use_arrows( GTK_COMBO(widget), 0);
}


static void init_binary_path(void)
{
    char pathCopy[ 4069 ];
    GtkWidget* widget;
    GList* list;
    char* path;
    int change_default = TRUE;

    widget = glade_xml_get_widget(setup_glade, "binary_path");

    if ( GetProductIsMeta(cur_info) ) {
		gtk_widget_hide(widget);
		return;
    }

    list = 0;

    if ( access(cur_info->symlinks_path, W_OK) == 0 ) {
        list = g_list_append( list, cur_info->symlinks_path );
        change_default = FALSE;
    }

    path = (char *)getenv( "PATH" );
    if( path )
    {
        int len;
        char* pc0;
        char* pc;
        int sc;
        int end = 0;

        pc = pathCopy;
        strncpy( pathCopy, path, sizeof (pathCopy) - 1 );
        pathCopy[sizeof (pathCopy) - 1] = '\0';  /* just in case. */

        while( *pc != ':' && *pc != '\0' ) {
            pc0 = pc;
            len = 0;
            while( *pc != ':' && *pc != '\0' ) {
                len++;
                pc++;
            }
            if( *pc == '\0' )
                end = 1;
            else
                *pc = '\0';

            if( len && ((sc=strcmp( pc0, cur_info->symlinks_path)) != 0) && (*pc0 != '.') ) {
				if ((!str_in_g_list(pc0, list)) && (access(pc0, W_OK) == 0)) {
					list = g_list_append( list, pc0 );
				}
            }

            if( ! end )
                pc++;
        }
    }

    path = (char *)getenv( "HOME" );
    if( path )
    {
	    if ((!str_in_g_list(path, list)) && (access(path, W_OK) == 0)) {
		    list = g_list_append( list, path );
		}
    }

    if ( list ) {
        gtk_combo_set_popdown_strings( GTK_COMBO(widget), list );
    }
    if ( change_default && list && g_list_length(list) ) {
        set_symlinkspath(cur_info, g_list_nth(list,0)->data);
    }

    if ((list == NULL || g_list_length(list) == 0) && change_default)
    {
        log_warning(_("Warning: No writable targets in path... You may want to be root.\n"));
        /* FIXME */
    }
    /* !!! FIXME: Should we g_list_free ( list ) or not? */
    gtk_entry_set_text( GTK_ENTRY(GTK_COMBO(widget)->entry), cur_info->symlinks_path );
    gtk_combo_set_use_arrows( GTK_COMBO(widget), 0);
}

static void init_menuitems_option(install_info *info)
{
    GtkWidget* widget;

    widget = glade_xml_get_widget(setup_glade, "setup_menuitems_checkbox");
    if ( widget ) {
        if ( ! GetProductHasNoBinaries(info) ) {
            setup_checkbox_menuitems_slot(widget, NULL);
        } else {
            gtk_widget_hide(widget);
        }
    } else {
        log_warning(_("Unable to locate 'setup_menuitems_checkbox'"));
    }
}

static void parse_option(install_info *info, const char *component, xmlNodePtr node, GtkWidget *window, GtkWidget *box, int level, GtkWidget *parent, int exclusive, int excl_reinst, GSList **radio)
{
    xmlNodePtr child;
    char text[1024] = "";
    const char *help;
    const char *wanted;
    gchar *name;
    int i;
    GtkWidget *button = NULL;

    /* See if this node matches the current architecture */
    wanted = xmlGetProp(node, "arch");
    if ( ! match_arch(info, wanted) ) {
        return;
    }

    wanted = xmlGetProp(node, "libc");
    if ( ! match_libc(info, wanted) ) {
        return;
    }

    wanted = xmlGetProp(node, "distro");
    if ( ! match_distro(info, wanted) ) {
        return;
    }

    if ( ! get_option_displayed(info, node) ) {
		return;
    }

    /* See if the user wants this option */
	if ( node->type == XML_TEXT_NODE ) {
		log_debug("Parsing text node.\n");
		name = g_strdup(node->content);
		g_strstrip(name);
		if ( *name ) {
			log_debug("String: '%s'\n", name);
			button = gtk_label_new(name);
			gtk_widget_show(button);
			gtk_box_pack_start(GTK_BOX(box), GTK_WIDGET(button), FALSE, FALSE, 0);
		}
		g_free(name);
		return;
	} else {
		name = get_option_name(info, node, NULL, 0);
		for(i=0; i < (level*5); i++)
			text[i] = ' ';
		text[i] = '\0';
		strncat(text, name, sizeof(text)-strlen(text));
	}

	log_debug("Parsing option: '%s'\n", text);
	if ( GetProductIsMeta(info) ) {
		button = gtk_radio_button_new_with_label(radio_list, text);
		radio_list = gtk_radio_button_group(GTK_RADIO_BUTTON(button));
	} else if ( exclusive ) {
		button = gtk_radio_button_new_with_label(*radio, text);
		*radio = gtk_radio_button_group(GTK_RADIO_BUTTON(button));
	} else {
		button = gtk_check_button_new_with_label(text);
	}

    /* Add tooltip help, if available */
    help = get_option_help(info, node);
    if ( help ) {
        GtkTooltipsData* group;

        group = gtk_tooltips_data_get(window);
        if ( group ) {
            gtk_tooltips_set_tip( group->tooltips, button, help, 0);
        } else {
            gtk_tooltips_set_tip( gtk_tooltips_new(), button, help, 0);
        }
    }

    /* Set the data associated with the button */
	if ( button ) {
		gtk_object_set_data(GTK_OBJECT(button), "data", (gpointer)node);

		/* Register the button in the window's private data */
		window = glade_xml_get_widget(setup_glade, "setup_window");
		gtk_object_set_data(GTK_OBJECT(window), name, (gpointer)button);
	}

    /* Check for required option */
    if ( xmlGetProp(node, "required") ) {
		xmlSetProp(node, "install", "true");
		gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);
    }

    /* If this is a sub-option and parent is not active, then disable option */
    wanted = xmlGetProp(node, "install");
    if( level>0 && GTK_IS_TOGGLE_BUTTON(parent) && !gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(parent)) ) {
		wanted = "false";
		gtk_widget_set_sensitive(GTK_WIDGET(button), FALSE);
    }
	if ( button ) {
		gtk_box_pack_start(GTK_BOX(box), GTK_WIDGET(button), FALSE, FALSE, 0);
		gtk_signal_connect(GTK_OBJECT(button), "toggled",
						   GTK_SIGNAL_FUNC(setup_checkbox_option_slot), (gpointer)node);
		gtk_widget_show(button);
	}

	if ( wanted && (strcmp(wanted, "true") == 0) ) {
        gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(button), TRUE);
    } else {
        /* Unmark this option for installation */
        mark_option(info, node, "false", 1);
    }

    /* Recurse down any other options */
    child = node->childs;
    while ( child ) {
		if ( !strcmp(child->name, "option") ) {
			parse_option(info, component, child, window, box, level+1, button, 0, 0, NULL);
		} else if ( !strcmp(child->name, "exclusive") ) {
			xmlNodePtr exchild;
			GSList *list = NULL;
			int reinst = ! GetReinstallNode(info, node);

			for ( exchild = child->childs; exchild; exchild = exchild->next) {
				parse_option(info, component, exchild, window, box, level+1, button, 1, reinst, &list);
			}
		}
		child = child->next;
    }

    /* Disable any options that are already installed */
    if ( info->product ) {
		product_component_t *comp;
		if ( component ) {
			comp = loki_find_component(info->product, component);
		} else {
			comp = loki_getdefault_component(info->product);
		}
		if ( excl_reinst ) { /* Reinstall an exclusive option - make sure to select the installed one */
			gtk_widget_set_sensitive(button, FALSE);
			if ( comp && loki_find_option(comp, name) ) {
				gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(button), TRUE);
				mark_option(info, node, "true", 1);
			} else {
				gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(button), FALSE);
				mark_option(info, node, "false", 1);
			}
		} else if ( ! GetProductReinstall(info) ) {			
			if ( comp && loki_find_option(comp, name) ) {
				/* Unmark this option for installation */
				gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(button), FALSE);
				gtk_widget_set_sensitive(button, FALSE);
				mark_option(info, node, "false", 1);
			}
		} else if (!GetReinstallNode(info, node)) {
			/* Unmark this option for installation, unless it was not installed already */
			gtk_widget_set_sensitive(button, FALSE);
			gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(button), FALSE);
			mark_option(info, node, "false", 1);
		}
    }
}

static void update_image(const char *image_file)
{
    GtkWidget* window;
    GtkWidget* frame;
    GdkPixmap* pixmap;
	GdkBitmap* mask;
    GtkWidget* image;
	char image_path[1024] = SETUP_BASE;

	strncat(image_path, image_file, sizeof(image_path));

    frame = glade_xml_get_widget(setup_glade, "image_frame");
    gtk_container_remove(GTK_CONTAINER(frame), GTK_BIN(frame)->child);
    window = gtk_widget_get_toplevel(frame);
    pixmap = gdk_pixmap_create_from_xpm(window->window, &mask, NULL, image_path);
    if ( pixmap ) {
        image = gtk_pixmap_new(pixmap, mask);
        gtk_widget_show(image);
        gtk_container_add(GTK_CONTAINER(frame), image);
    } else {
        gtk_widget_hide(frame);
    }
}

/********** UI functions *************/

static install_state gtkui_init(install_info *info, int argc, char **argv, int noninteractive)
{
    FILE *opened;
    GtkWidget *window;
    GtkWidget *notebook;
    GtkWidget *widget;
    GtkWidget *button;
    GtkWidget *install_path, *install_entry, *binary_path, *binary_entry;
    char title[1024];

    cur_state = SETUP_INIT;
    cur_info = info;

	gtk_set_locale();
    gtk_init(&argc,&argv);
	/* Try to get a RGB colormap */
	gdk_rgb_init();
	gtk_widget_set_default_colormap(gdk_rgb_get_cmap());
	gtk_widget_set_default_visual(gdk_rgb_get_visual());
    glade_init();

    /* Glade segfaults if the file can't be read */
    opened = fopen(SETUP_GLADE, "r");
    if ( opened == NULL ) {
        return SETUP_ABORT;
    }
    fclose(opened);

    setup_glade = glade_xml_new(SETUP_GLADE, "setup_window"); 

    /* add signal handlers to manage enabling/disabling the Install button */
    install_path = glade_xml_get_widget(setup_glade, "install_path");
    install_path = GTK_COMBO(install_path)->list;
    install_entry = glade_xml_get_widget(setup_glade, "install_entry");
    gtk_signal_connect_after(GTK_OBJECT (install_path),
			     "button_release_event",
			     GTK_SIGNAL_FUNC (path_combo_change_slot),
			     NULL);
    gtk_signal_connect_after(GTK_OBJECT (install_entry),
			     "key_press_event",
			     GTK_SIGNAL_FUNC (path_entry_keypress_slot),
			     NULL);

    binary_path = glade_xml_get_widget(setup_glade, "binary_path");
    binary_path = GTK_COMBO(binary_path)->list;
    binary_entry = glade_xml_get_widget(setup_glade, "binary_entry");
    gtk_signal_connect_after(GTK_OBJECT (binary_path),
			     "button_release_event",
			     GTK_SIGNAL_FUNC (binary_path_combo_change_slot),
			     NULL);
    gtk_signal_connect_after(GTK_OBJECT (binary_entry),
			     "key_press_event",
			     GTK_SIGNAL_FUNC (binary_path_entry_keypress_slot),
			     NULL);

    /* add all the other signal handlers defined in setup.glade */
    glade_xml_signal_autoconnect(setup_glade);

#if 0 /* Sam 8/22 - I don't think this is necessary */
    GtkWidget *symlink_checkbox;
    /*-------------------------------------------------------------------------
    ** Connect a signal handle to control whether or not the symlink
    **  should be installed
    **------------------------------------------------------------------------*/
    symlink_checkbox = glade_xml_get_widget(setup_glade, "symlink_checkbox");
    gtk_signal_connect(GTK_OBJECT(symlink_checkbox), "toggled",
			   GTK_SIGNAL_FUNC(on_use_binary_toggled), NULL);
#endif /*0*/

    /* Set up the window title */
    window = glade_xml_get_widget(setup_glade, "setup_window");
    if ( info->component ) {
        snprintf(title, sizeof(title), _("%s / %s Setup"), info->desc, GetProductComponent(info));
    } else {
        snprintf(title, sizeof(title), _("%s Setup"), info->desc);
    }
    gtk_window_set_title(GTK_WINDOW(window), title);

    /* Set the initial state */
    notebook = glade_xml_get_widget(setup_glade, "setup_notebook");

    if ( noninteractive ) {
        gtk_notebook_set_page(GTK_NOTEBOOK(notebook), COPY_PAGE);        
	} else if ( GetProductAllowsExpress(info) ) {
        gtk_notebook_set_page(GTK_NOTEBOOK(notebook), CLASS_PAGE);
		/* Workaround for weird GTK behavior with old Debian, disable the button until
		   we reach the class selection state. We should find a better fix... */
		widget = glade_xml_get_widget(setup_glade, "class_continue");
		gtk_widget_set_sensitive(widget, FALSE);
    } else {
        gtk_notebook_set_page(GTK_NOTEBOOK(notebook), OPTION_PAGE);
    }
	gtk_widget_queue_draw(window);

    /* Disable the "View Readme" button if no README available */
     if ( ! GetProductREADME(cur_info, NULL) ) {
         button = glade_xml_get_widget(setup_glade, "button_readme");
         gtk_widget_set_sensitive(button, FALSE);
         button = glade_xml_get_widget(setup_glade, "view_readme_progress_button");
         gtk_widget_hide(button);
		 button = glade_xml_get_widget(setup_glade, "view_readme_end_button");
         gtk_widget_hide(button);		 
         button = glade_xml_get_widget(setup_glade, "class_readme");
         gtk_widget_hide(button);
     }

    /* Set the text for some blank labels */
    widget = glade_xml_get_widget(setup_glade, "current_option_label");
    if ( widget ) {
        gtk_label_set_text( GTK_LABEL(widget), "");
    }
    widget = glade_xml_get_widget(setup_glade, "current_file_label");
    if ( widget ) {
        gtk_label_set_text( GTK_LABEL(widget), "");
    }

	/* Disable useless widgets for meta-installer */
	if ( GetProductIsMeta(info) ) {
		widget = glade_xml_get_widget(setup_glade, "global_frame");
		if ( widget ) {
			gtk_widget_hide(widget);
		}
		widget = glade_xml_get_widget(setup_glade, "label_free_space");
		if ( widget ) {
			gtk_widget_hide(widget);
		}
		widget = glade_xml_get_widget(setup_glade, "free_space_label");
		if ( widget ) {
			gtk_widget_hide(widget);
		}
		widget = glade_xml_get_widget(setup_glade, "label_install_size");
		if ( widget ) {
			gtk_widget_hide(widget);
		}
		widget = glade_xml_get_widget(setup_glade, "estim_size_label");
		if ( widget ) {
			gtk_widget_hide(widget);
		}
		widget = glade_xml_get_widget(setup_glade, "install_separator");
		if ( widget ) {
			gtk_widget_hide(widget);
		}
	}

    /* Disable the path fields if they were provided via command line args */
    if (disable_install_path) {
        widget = glade_xml_get_widget(setup_glade, "install_path");
		gtk_widget_set_sensitive(widget, FALSE);
    }
    if (disable_binary_path) {
        widget = glade_xml_get_widget(setup_glade, "binary_path");
		gtk_widget_set_sensitive(widget, FALSE);
    }
	if (GetProductHasNoBinaries(info)) {
        widget = glade_xml_get_widget(setup_glade, "binary_path");
		if(widget) gtk_widget_hide(widget);
		widget = glade_xml_get_widget(setup_glade, "binary_label");
		if(widget) gtk_widget_hide(widget);
		widget = glade_xml_get_widget(setup_glade, "setup_menuitems_checkbox");
		if(widget) gtk_widget_hide(widget);
	}

	/*--------------------------------------------------------------------
	**  Hide the checkbox allowing the user to pick whether or
	**      not to install a symlink to the binaries if they
	**      haven't asked for that feature.
	**------------------------------------------------------------------*/
	if (GetProductHasNoBinaries(info) || (!GetProductHasPromptBinaries(info))) {
		widget = glade_xml_get_widget(setup_glade, "symlink_checkbox");
		if (widget)	gtk_widget_hide(widget);
	}

    info->install_size = size_tree(info, info->config->root->childs);

    license_okay = 1; /* Needed so that Expert is detected properly at this point */
    
	/* Check if we should check "Expert" installation by default */
	if ( check_for_installation(info) ) {
		widget = glade_xml_get_widget(setup_glade, "expert_but");
		gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(widget), TRUE);
	}
    
    if ( GetProductEULA(info, NULL) ) {
        license_okay = 0;
        cur_state = SETUP_LICENSE;
    } else {
        license_okay = 1;
        cur_state = SETUP_README;
    }

    /* Realize the main window for pixmap loading */
    gtk_widget_realize(window);

    /* Update the install image */
    update_image(GetProductSplash(info));

    /* Center the installer, it will be shown later */
    gtk_window_set_position(GTK_WINDOW(window), GTK_WIN_POS_CENTER);

    return cur_state;
}

static install_state gtkui_license(install_info *info)
{
    GtkWidget *license;
    GtkWidget *widget;

    setup_glade_license = glade_xml_new(SETUP_GLADE, "license_dialog");
    glade_xml_signal_autoconnect(setup_glade_license);
    license = glade_xml_get_widget(setup_glade_license, "license_dialog");
    widget = glade_xml_get_widget(setup_glade_license, "license_area");
    if ( license && widget ) {
        GdkFont *font;

        font = gdk_font_load(LICENSE_FONT);
        gtk_widget_hide(license);
        load_file(GTK_TEXT(widget), font, GetProductEULA(info, NULL));
        gtk_widget_show(license);
        gtk_window_set_modal(GTK_WINDOW(license), TRUE);

        iterate_for_state();
    } else {
        cur_state = SETUP_README;
    }
    return cur_state;
}

static install_state gtkui_readme(install_info *info)
{
    if ( GetProductAllowsExpress(info) ) {
	cur_state = SETUP_CLASS;
    } else {
	cur_state = SETUP_OPTIONS;
    }
    return cur_state;
}

static install_state gtkui_pick_class(install_info *info)
{
	/* Enable the Continue button now */
	GtkWidget *widget = glade_xml_get_widget(setup_glade, "class_continue");

	/* Make sure the window is being shown */
    gtk_widget_show(glade_xml_get_widget(setup_glade, "setup_window"));

	gtk_widget_set_sensitive(widget, TRUE);
	return iterate_for_state();
}

static void gtkui_idle(install_info *info)
{
    while( gtk_events_pending() ) {
        gtk_main_iteration();
    }
}

static install_state gtkui_setup(install_info *info)
{
    GtkWidget *window;
    GtkWidget *options;
    xmlNodePtr node;

    window = glade_xml_get_widget(setup_glade, "setup_window");
	/* Make sure the window is being shown */
    gtk_widget_show(window);

    if ( express_setup ) {
		GtkWidget *notebook = glade_xml_get_widget(setup_glade, "setup_notebook");
		gtk_notebook_set_page(GTK_NOTEBOOK(notebook), COPY_PAGE);
		return cur_state = SETUP_INSTALL;
    }

    /* Go through the install options */
    options = glade_xml_get_widget(setup_glade, "option_vbox");
    gtk_container_foreach(GTK_CONTAINER(options), empty_container, options);
    info->install_size = 0;
    node = info->config->root->childs;
    radio_list = NULL;
    in_setup = TRUE;
    while ( node ) {
		if ( ! strcmp(node->name, "option") ) {
			parse_option(info, NULL, node, window, options, 0, NULL, 0, 0, NULL);
		} else if ( ! strcmp(node->name, "exclusive") ) {
			xmlNodePtr child;
			GSList *list = NULL;
			int reinst = ! GetReinstallNode(info, node);
			for ( child = node->childs; child; child = child->next) {
				parse_option(info, NULL, child, window, options, 0, NULL, 1, reinst, &list);
			}
		} else if ( ! strcmp(node->name, "component") ) {
            if ( match_arch(info, xmlGetProp(node, "arch")) &&
                 match_libc(info, xmlGetProp(node, "libc")) && 
				 match_distro(info, xmlGetProp(node, "distro")) ) {
                xmlNodePtr child;
                if ( xmlGetProp(node, "showname") ) {
                    GtkWidget *widget = gtk_hseparator_new();
                    gtk_box_pack_start(GTK_BOX(options), GTK_WIDGET(widget), FALSE, FALSE, 0);
                    gtk_widget_show(widget);                
                    widget = gtk_label_new(xmlGetProp(node, "name"));
                    gtk_box_pack_start(GTK_BOX(options), GTK_WIDGET(widget), FALSE, FALSE, 10);
                    gtk_widget_show(widget);
                }
                for ( child = node->childs; child; child = child->next) {
					if ( ! strcmp(child->name, "option") ) {
						parse_option(info, xmlGetProp(node, "name"), child, window, options, 0, NULL, 0, 0, NULL);
					} else if ( ! strcmp(child->name, "exclusive") ) {
						xmlNodePtr child2;
						GSList *list = NULL;
						int reinst = ! GetReinstallNode(info, node);
						for ( child2 = child->childs; child2; child2 = child2->next) {
							parse_option(info, xmlGetProp(node, "name"), child2, window, options, 0, NULL, 1, reinst, &list);
						}
					}
                }
            }
		}
		node = node->next;
    }
    init_install_path();
    init_binary_path();
    update_size();
    update_space();
    init_menuitems_option(info);

    in_setup = FALSE;

    return iterate_for_state();
}

static int gtkui_update(install_info *info, const char *path, size_t progress, size_t size, const char *current)
{
    static gfloat last_update = -1;
    GtkWidget *widget;
    int textlen;
    char text[1024];
    char *install_path;
    gfloat new_update;

    if ( cur_state == SETUP_ABORT ) {
	return FALSE;
    }

    if ( progress && size ) {
        new_update = (gfloat)progress / (gfloat)size;
    } else { /* "Running script" */
        new_update = 1.0;
    }
    if ( (int)(new_update*100) != (int)(last_update*100) ) {
        if ( new_update == 1.0 ) {
            last_update = 0.0;
        } else {
            last_update = new_update;
        }
        widget = glade_xml_get_widget(setup_glade, "current_option_label");
        if ( widget ) {
            gtk_label_set_text( GTK_LABEL(widget), current);
        }
        widget = glade_xml_get_widget(setup_glade, "current_file_label");
        if ( widget ) {
            snprintf(text, sizeof(text), "%s", path);
            /* Remove the install path from the string */
            install_path = cur_info->install_path;
            if ( strncmp(text, install_path, strlen(install_path)) == 0 ) {
                strcpy(text, &text[strlen(install_path)+1]);
            }
            textlen = strlen(text);
            if ( textlen > MAX_TEXTLEN ) {
                strcpy(text, text+(textlen-MAX_TEXTLEN));
            }
            gtk_label_set_text( GTK_LABEL(widget), text);
        }
        widget = glade_xml_get_widget(setup_glade, "current_file_progress");
        gtk_progress_bar_update(GTK_PROGRESS_BAR(widget), new_update);
        new_update = (gfloat)info->installed_bytes / (gfloat)info->install_size;
	if (new_update > 1.0) {
	    new_update = 1.0;
	}
	else if (new_update < 0.0) {
	    new_update = 0.0;
	}
        widget = glade_xml_get_widget(setup_glade, "total_file_progress");
        gtk_progress_bar_update(GTK_PROGRESS_BAR(widget), new_update);
    }
    while( gtk_events_pending() ) {
        gtk_main_iteration();
    }
	return TRUE;
}

static void gtkui_abort(install_info *info)
{
    GtkWidget *notebook, *w;

	/* No point in waiting for a change of state if the window is not there */
	w = glade_xml_get_widget(setup_glade, "setup_window");
	if ( ! GTK_WIDGET_VISIBLE(w) )
		return;

    if ( setup_glade ) {
        notebook = glade_xml_get_widget(setup_glade, "setup_notebook");
        gtk_notebook_set_page(GTK_NOTEBOOK(notebook), ABORT_PAGE);
        iterate_for_state();
		gtk_widget_hide(w);
    } else {
        fprintf(stderr, _("Unable to open %s, aborting!\n"), SETUP_GLADE);
    }
}

static install_state gtkui_website(install_info *info)
{
    GtkWidget *notebook;
    GtkWidget *widget;
    GtkWidget *hideme;
    const char *website_text;
    int do_launch;

    notebook = glade_xml_get_widget(setup_glade, "setup_notebook");
    gtk_notebook_set_page(GTK_NOTEBOOK(notebook), WEBSITE_PAGE);

    /* Add the proper product text */
    widget = glade_xml_get_widget(setup_glade, "website_product_label");
    gtk_label_set_text(GTK_LABEL(widget), GetProductDesc(info));

    /* Add special website text if desired */
    website_text = GetWebsiteText(info);
    if ( website_text ) {
        widget = glade_xml_get_widget(setup_glade, "website_text_label");
        gtk_label_set_text(GTK_LABEL(widget), website_text);
    }

    /* Hide the proper widget based on the auto_url state */
    do_launch = 0;
    if ( strcmp(GetAutoLaunchURL(info), "true") == 0 ) {
        do_launch = 1;
        hideme = glade_xml_get_widget(setup_glade, "auto_url_no");
    } else {
        do_launch = 0;
        hideme = glade_xml_get_widget(setup_glade, "auto_url_yes");
    }
    gtk_widget_hide(hideme);

    /* Automatically launch the browser if necessary */
    if ( do_launch ) {
        launch_browser(info, loki_launchURL);
    }
    return iterate_for_state();
}

static install_state gtkui_complete(install_info *info)
{
    GtkWidget *widget;
    char text[1024];

    widget = glade_xml_get_widget(setup_glade, "setup_notebook");
    gtk_notebook_set_page(GTK_NOTEBOOK(widget), DONE_PAGE);
    widget = glade_xml_get_widget(setup_glade, "install_directory_label");
    gtk_label_set_text(GTK_LABEL(widget), info->install_path);
    widget = glade_xml_get_widget(setup_glade, "play_game_label");
    if ( info->installed_symlink && info->symlinks_path && *info->symlinks_path ) {
        snprintf(text, sizeof(text), _("Type '%s' to start the program"), info->installed_symlink);
    } else {
		*text = '\0';
    }
    gtk_label_set_text(GTK_LABEL(widget), text);

    /* Hide the play game button if there's no game to play. :) */
    widget = glade_xml_get_widget(setup_glade, "play_game_button");
    if ( widget && 
		 (!info->installed_symlink || !info->symlinks_path || !*info->symlinks_path ) ) {
        gtk_widget_hide(widget);
    }

    /* Hide the 'View Readme' button if we have no readme... */
    if ( ! GetProductREADME(info, NULL) ) {
        widget = glade_xml_get_widget(setup_glade, "view_readme_end_button");
        if(widget)
            gtk_widget_hide(widget);
    }

    /* TODO: Lots of cleanups here (free() mostly) */

    return iterate_for_state();
}

static void gtkui_shutdown(install_info *info)
{
    /* Destroy all windows */
    GtkWidget *window = glade_xml_get_widget(setup_glade, "setup_window");

    gtk_widget_hide(window);
    if ( setup_glade_readme ) {
		window = glade_xml_get_widget(setup_glade_readme, "readme_dialog");
		gtk_widget_hide(window);
    }
    if ( setup_glade_license ) {
		window = glade_xml_get_widget(setup_glade_license, "license_dialog");
		gtk_widget_hide(window);
    }
    gtkui_idle(info);
}

int gtkui_okay(Install_UI *UI, int *argc, char ***argv)
{
    extern int force_console;
    int okay;

    okay = 0;
    if ( !force_console ) {
        /* Try to open a GTK connection */
        if( gtk_init_check(argc, argv) ) {
            /* Set up the driver */
            UI->init = gtkui_init;
            UI->license = gtkui_license;
            UI->readme = gtkui_readme;
            UI->setup = gtkui_setup;
            UI->update = gtkui_update;
            UI->abort = gtkui_abort;
            UI->prompt = gtkui_prompt;
            UI->website = gtkui_website;
            UI->complete = gtkui_complete;
	    UI->pick_class = gtkui_pick_class;
	    UI->idle = gtkui_idle;
	    UI->exit = NULL;
	    UI->shutdown = gtkui_shutdown;
	    UI->is_gui = 1;

            okay = 1;
        }
    }
    return(okay);
}

#ifdef STUB_UI
int console_okay(Install_UI *UI, int *argc, char ***argv)
{
    return(0);
}

int carbonui_okay(Install_UI *UI, int *argc, char ***argv)
{
    return(0);
}

#ifdef ENABLE_DIALOG
int dialog_okay(Install_UI *UI, int *argc, char ***argv)
{
    return(0);
}
#endif

#endif




