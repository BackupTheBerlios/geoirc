package enigma.shells.commandline.commands;

import java.io.*;

import enigma.shells.commandline.*;

public class Start {
    public static void main(final String[] args) {
        new Thread() {
            public void run() {
                StringBuffer argString = new StringBuffer();
                for (int i = 0; i < args.length; i++) {
                    argString.append(' ');
                    argString.append(args[i]);
                }
                new CommandLineShell().processCommand(argString.toString());
            }
        }.start();
    }
}
