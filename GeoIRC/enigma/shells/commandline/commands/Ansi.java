package enigma.shells.commandline.commands;

import java.awt.Color;
import java.io.*;

import enigma.console.*;
import enigma.console.terminal.AnsiOutputStream;
import enigma.core.*;
import enigma.util.Util;

public class Ansi {
    public static void main(String[] arg) {
        if (arg.length == 0 || (!arg[0].equalsIgnoreCase("on") && !arg[0].equalsIgnoreCase("off"))) {
            System.out.println(Util.msg(Ansi.class, "usage.info"));
            return;
        }
        
        if (arg[0].equalsIgnoreCase("on")) {
            System.setOut(new PrintStream(new AnsiOutputStream(Enigma.getConsole())));
            System.out.println(Util.msg(Ansi.class, "support.enabled"));
        }
        else {
            System.setOut(Enigma.getConsole().getOutputStream());
            System.out.println(Util.msg(Ansi.class, "support.disabled"));
        }            
    }
}
