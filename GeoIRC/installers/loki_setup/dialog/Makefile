# $Id: Makefile,v 1.1 2003/10/14 17:00:24 mynameisneo Exp $
# template makefile for DIALOG
#
SHELL		= /bin/sh

VERSION		= $(shell cat VERSION)

CFLAGS		= -fsigned-char -funroll-loops -Wall -g -O2 -I.  -I/usr/X11R6/include -D_REENTRANT -I/home/striker/CVSROOT/icculus/loki_setup/../loki_setupdb -I/usr/include/gnome-xml -I/usr/include/gnome-xml -I/usr/include/libglade-1.0 -I/usr/include/gtk-1.2 -I/usr/include/glib-1.2 -I/usr/lib/glib/include -I/usr/include/gtk-1.2 -I/usr/include/glib-1.2 -I/usr/lib/glib/include -DENABLE_DIALOG -DHAVE_COLOR -I. -I..
CPPFLAGS	=  -DHAVE_CONFIG_H -DLOCALEDIR=\"locale\" -DVERSION=\"$(VERSION)\"
CC		= gcc
LDFLAGS		= 
LIBS		=  -L/usr/local/lib -lutil  /usr/lib/libxml.a -Wl,-Bstatic  -Wl,-Bstatic -lintl  -lz $(BDYNAMIC)
RANLIB		= ranlib

RM		= rm -f
LINT		= lint

#
# Standard .c to .o compile line.
#
.c.o :
	$(CC) $(CFLAGS) $(EXTRA_CFLAGS) $(CPPFLAGS) -c $<

OBJS = 	arrows.o \
	buttons.o \
	checklist.o \
	inputbox.o \
	inputstr.o \
	menubox.o \
	mouse.o \
	mousewget.o \
	msgbox.o \
	textbox.o \
	ui_getc.o \
	util.o \
	gauge.o \
	fselect.o \
	timebox.o \
	yesno.o

SRCS = $(OBJS:.o=.c)
HDRS = \
	colors.h \
	dialog.h

LIB	= libdialog.a
ALL	= $(LIB) $(PROG)

all	:: $(ALL)

dialog.o $(OBJS) : dialog.h

$(LIB) : $(OBJS)
	ar crv $(LIB) $(OBJS)
	$(RANLIB) $@

clean:
	rm -f core *.o *~ tags TAGS $(ALL)

tags:
	ctags $(SRCS) $(HDRS)

TAGS:
	etags $(SRCS) $(HDRS)

lint:
	$(LINT) $(CPPFLAGS) *.c


depend:
	$(CC) -MM $(CFLAGS) $(SRCS) > .depend

ifeq ($(wildcard .depend),.depend)
include .depend
endif
