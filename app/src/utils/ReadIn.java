package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class ReadIn {
    private final String CONFIG_FILE = "config.txt";

    private FileInputStream commandFile = null;
    private Scanner scanCommand = null;

    public ServerInfo getSuperNodeInfo() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.port = -1;

        try {
            FileInputStream file = new FileInputStream(CONFIG_FILE);
            Scanner scanConfig = new Scanner(file);
            String[] line;

            while (scanConfig.hasNextLine()) {
                line = scanConfig.nextLine().split(" ");
                if (line[0].equals("SuperNode")) { // Found SupeNode line
                    serverInfo.ip = line[1];
                    serverInfo.port = Integer.parseInt(line[2]);
                    break;
                }
            }

            scanConfig.close();
        } catch (Exception exception) {
            System.out.println("ERROR: ReadIn: getServerPort() - " + exception);
            System.exit(1);
        }
        if (serverInfo.port == -1) { // Server line never found
            System.out.println("ERROR: ReadIn: getServerPort() - Improper Configuration file - Invalid SuperNode information.\n");
            System.exit(1);
        }

        return serverInfo;
    }


    public int getNodePort() {
        int port = -1;

        try {
            FileInputStream file = new FileInputStream(CONFIG_FILE);
            Scanner scanConfig = new Scanner(file);
            String[] line;

            while (scanConfig.hasNextLine()) {
                line = scanConfig.nextLine().split(" ");
                if (line[0].equals("Node")) { // Found SupeNode line
                    port = Integer.parseInt(line[1]);
                    break;
                }
            }

            scanConfig.close();
        } catch (Exception exception) {
            System.out.println("ERROR: ReadIn.getServerPort() - " + exception);
            System.exit(1);
        }
        if (port == -1) { // Server line never found
            System.out.println("ERROR: ReadIn.getServerPort() Improper Configuration file - Invalid SuperNode information.\n");
            System.exit(1);
        }

        return port;
    }

    public void setCommandFile(String filePath) {
        try {
            commandFile = new FileInputStream(filePath);
            scanCommand = new Scanner(commandFile);
        } catch (Exception exception) {
            System.out.println("ERROR: ReadIn.setCommandFile() - " + exception);
        }
    }

    public String[] readCommand() {
        String[] results = null;

        if (commandFile == null || scanCommand == null) {
            System.out.println("ERROR: ReadIn.setCommandFile() - tried to read from null file.");
            System.exit(1);
        }

        if (scanCommand.hasNextLine()) {
            results = scanCommand.nextLine().split(" ");
        } else {
            commandFile = null;
            scanCommand = null;
        }

        return results;
    }
}
