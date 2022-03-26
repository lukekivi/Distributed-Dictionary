package node;

import utils.ReadIn;
import java.io.PrintStream;
import utils.ServerInfo;

public class NodeManager {
    private final String LOG_FILE = "log/node";
    private ReadIn readIn = new ReadIn();

    public void setLog(int id)  {
        try {
            PrintStream fileOut = new PrintStream(LOG_FILE + id + ".txt");
            System.setOut(fileOut);  
        } catch (FileNotFoundException x) {
            System.out.println("Not able to establish a log file.");
            System.exit(1);
        }
    }

    public String getIpAddressOfSelf() {

    }

    public int getPortOfSelf() {
        // read in portnumber from the config file
        return readIn.getNodePort();
    }

    public ServerInfo getSuperNodeInfo() {
        return readIn.getSuperNodeInfo();
    }
}
