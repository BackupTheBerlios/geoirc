package enigma.shells.commandline.commands;

import java.awt.Color;
import java.io.*;

import enigma.console.*;
import enigma.core.*;

public class Help {
    public static void main(String[] arg) {
        System.out.println("'Real' help is not yet available in this preview version.");
        System.out.println("However, feel free to play around with the following commands:");
        System.out.println();
        Enigma.getConsole().setTextAttributes(new TextAttributes(new Color(255, 128, 128), Color.black));
        System.out.println("cd");
        System.out.println("exit");
        System.out.println("list");
        System.out.println("mkdir");
        System.out.println("say");
        System.out.println("start");
        System.out.println("type");
        Enigma.getConsole().setTextAttributes(Enigma.getSystemTextAttributes("attributes.console.default"));
        System.out.println();
        System.out.println("A few other commands are supported, but they're not likely to");
        System.out.println("be of much use yet.");
    }
}