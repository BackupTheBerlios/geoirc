/* $Id: file.h,v 1.1 2003/10/14 17:00:24 mynameisneo Exp $ */

#ifndef __LOKI_FILE_H__
#define __LOKI_FILE_H__

/* Functions to handle logging and low level file functions */

#include <sys/types.h>
#include <stdio.h>
#include <stdarg.h>

#include <zlib.h>

#include "config.h"
#include "install.h"
#include "md5.h"

#ifdef HAVE_BZIP2_SUPPORT
#  include <bzlib.h>
#else
   typedef void *BZFILE;
#endif

typedef struct {
    char *path;
    char mode;
    size_t size;
    FILE *fp;
    gzFile zfp;
	BZFILE *bzfp;
	MD5_CONTEXT md5;
	struct file_elem *elem;
} stream;

extern stream *file_open(install_info *info,const char *path,const char *mode);
extern stream *file_fdopen(install_info *info, const char *path, FILE *fd, gzFile zfd, BZFILE *bzfd, const char *mode);
extern int file_read(install_info *info, void *buf, int len, stream *streamp);
extern void file_skip_zeroes(install_info *info, stream *streamp);
extern void file_skip(install_info *info, int len, stream *streamp);
extern int file_write(install_info *info, void *buf, int len, stream *streamp);
extern int file_eof(install_info *info, stream *streamp);
extern int file_close(install_info *info, stream *streamp);
extern int file_symlink(install_info *info, const char *oldpath, const char *newpath);
extern int file_issymlink(install_info *info, const char *path);
extern int file_mkdir(install_info *info, const char *path, int mode);
extern int file_mkfifo(install_info *info, const char *path, int mode);
extern int file_mknod(install_info *info, const char *path, int mode, dev_t dev);
extern int file_chmod(install_info *info, const char *path, int mode);
extern size_t file_size(install_info *info, const char *path);
extern int file_exists(const char *path);
extern int dir_exists(const char *path);
extern void file_create_hierarchy(install_info *info, const char *path);
extern void dir_create_hierarchy(install_info *info, const char *path, int mode);
extern int dir_is_accessible(const char *path);
#endif
