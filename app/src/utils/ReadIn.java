package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * This class is used to manage configuration of the app and
 * read in commands for the client.
 */
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
    public int getNodeCacheSize() {
        int cacheSize = -1;

        try {
            FileInputStream file = new FileInputStream(CONFIG_FILE);
            Scanner scanConfig = new Scanner(file);
            String[] line;

            while (scanConfig.hasNextLine()) {
                line = scanConfig.nextLine().split(" ");
                if (line[0].equals("Node")) { // Found SupeNode line
                    cacheSize = Integer.parseInt(line[2]);
                    break;
                }
            }

            scanConfig.close();
        } catch (Exception exception) {
            System.out.println("ERROR: ReadIn.getNodeCacheSize() - " + exception);
            System.exit(1);
        }
        if (cacheSize == -1) { // Server line never found
            System.out.println("ERROR: ReadIn.getNodeCacheSize() Improper Configuration file - Invalid cacheSize value.\n");
            System.exit(1);
        }

        return cacheSize;
    }


    /**
     * Set the command file to read from.
     */
    public void setCommandFile(String filePath) {
        try {
            commandFile = new FileInputStream(filePath);
            scanCommand = new Scanner(commandFile);
        } catch (Exception exception) {
            System.out.println("ERROR: ReadIn.setCommandFile() - " + exception);
        }
    }

    /**
     * Read commands from the command file.
     */
    public String[] readCommand() {
        String[] results = null;

        if (commandFile == null || scanCommand == null) {
            System.out.println("ERROR: ReadIn.setCommandFile() - tried to read from null file.");
            System.exit(1);
        }

        if (scanCommand.hasNextLine()) {
            results = scanCommand.nextLine().split(" :: ");
        } else {
            commandFile = null;
            scanCommand = null;
        }

        return results;
    }


    public int getM() {
        int M = -1;

        try {
            FileInputStream file = new FileInputStream(CONFIG_FILE);
            Scanner scanConfig = new Scanner(file);
            String[] line;

            while (scanConfig.hasNextLine()) {
                line = scanConfig.nextLine().split(" ");
                if (line[0].equals("M")) { 
                    M = Integer.parseInt(line[1]);
                    break;
                }
            }

            scanConfig.close();
        } catch (Exception exception) {
            System.out.println("ERROR: ReadIn.getM() - " + exception);
            System.exit(1);
        }
        if (M == -1) { 
            System.out.println("ERROR: ReadIn.getM() Improper Configuration file - Invalid cacheSize value.\n");
            System.exit(1);
        }

        return M;
    }
}
