package enigma.console.remote;

import java.net.*;

import enigma.console.*;
import enigma.core.*;
import enigma.loaders.*;
import enigma.util.*;

class RemoteProxyLauncher {
    public static boolean authenticate() {
        // not implemented, just allow all connections
        Console console = Enigma.getConsole();
        console.setTextAttributes(Enigma.getSystemTextAttributes("attributes.emphasis"));
        String address = Util.msg(RemoteProxyLauncher.class, "unknown.ip");
        try {
            address = InetAddress.getLocalHost().toString();
        }
        catch (UnknownHostException e) {
        }
        System.out.println(Util.msg(RemoteProxyLauncher.class, "connected", new Object[] { address }));
        System.out.println();
        console.setTextAttributes(Enigma.getSystemTextAttributes("attributes.console.default"));
        return true;
   }
    
    
    public static void main(String[] args) throws Exception {
        Enigma.installConsole(new RemoteConsoleServer(System.in, System.out));
        if (authenticate()) {
            String[] childArgs = new String[args.length - 1];
            System.arraycopy(args, 1, childArgs, 0, childArgs.length);
            JavaProgramLoader.runClass(args[0], childArgs);
        }
    }
}
