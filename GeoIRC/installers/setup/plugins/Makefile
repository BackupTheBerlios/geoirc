# Generated automatically from Makefile.in by configure.
#
# Makefile for the setup plugins
# $Id: Makefile,v 1.1 2003/10/11 23:12:58 mynameisneo Exp $
#


STATICS = cpio.c
PLUGINS = tar.c
CFLAGS  = -fPIC -fsigned-char -funroll-loops -Wall -g -O2 -I.  -I/usr/include/gnome-xml -I/usr/include/gnome-xml -I/usr/include/libglade-1.0 -I/usr/include/gtk-1.2 -I/usr/include/glib-1.2 -I/usr/lib/glib/include -I/usr/include/gtk-1.2 -I/usr/include/glib-1.2 -I/usr/lib/glib/include -I/home/striker/CVSROOT/setup/../setupdb -I..
CC		= gcc
DYN_PLUGINS = false

arch := x86
libc := glibc-2.1

.SUFFIXES: .so .o .c

OBJS	= $(STATICS:.c=.o)

ifeq ($DYN_PLUGINS,true)

# Build all plugins as .so files
SOBJS = $(PLUGINS:.c=.so)

all: $(SOBJS) libplugins.a

install: all
	for a in $(SOBJS); do \
		cp -v $$a ../image/setup.data/bin/$(arch)/$(libc)/plugins; \
		strip ../image/setup.data/bin/$(arch)/$(libc)/plugins/$$a; \
	done

install.dbg: all
	for a in $(SOBJS); do \
		cp -v $$a ../image/setup.data/bin/$(arch)/$(libc)/plugins; \
	done

else

# Make a libplugin.a archive that will be linked with the main program
OBJS += $(PLUGINS:.c=.o)

all: libplugins.a

install:
	true

install.dbg:
	true

endif

libplugins.a: $(OBJS)
	ar rcs libplugins.a $(OBJS)

.c.so:
	$(CC) -shared -o $@ $*.c $(CFLAGS)
.c.o:
	$(CC) -o $@ -c $*.c $(CFLAGS)

clean:
	rm -f *.o *.so *.a *~

depend:
	$(CC) -MM $(CFLAGS) $(OBJS:.o=.c) > .depend

ifeq ($(wildcard .depend),.depend)
include .depend
endif
