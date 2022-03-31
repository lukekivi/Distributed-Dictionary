package client;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

class Client {
    private static final ClientManager manager = new ClientManager();
    public static void main(String[] args) {
        String commandsPath = null;
        Logger.getRootLogger().setLevel(Level.ERROR);

        /**
         * Users can provide a file with commands for the
         * client to complete.
         */
        if (args.length == 1) {
            commandsPath = args[0];
        }

        manager.connectToDHT();

        if (commandsPath != null) {
            manager.doCommands(commandsPath);
        }

        manager.close();
    }
}