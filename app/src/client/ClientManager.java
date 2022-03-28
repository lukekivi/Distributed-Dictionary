package client;

import pa2.Node;
import pa2.NodeDetails;
import utils.ReadIn;
import utils.ServerInfo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class ClientManager {
    private final ReadIn readIn = new ReadIn();
    private Node.Client node;
    private TTransport transport;

    /**
     * Read SuperNode details from config file.
     */
    public ServerInfo getSuperNodeInfo() {
        return readIn.getSuperNodeInfo();
    }


    /**
     * Setup connection to Node for communication with the DHT.
     * @param nodeInfo designated ambassador node
     */
    public void setNode(NodeDetails nodeInfo) {
        try {
            transport = new TSocket(nodeInfo.ip, nodeInfo.port);
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            node = new Node.Client(protocol);
        } catch (TTransportException x) {
            System.out.println("ERROR: ClientManager.setNode()"); 
            x.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Close connection to the node
     */
    private void closeNode() {
        if (transport.isOpen()) {
            transport.close();
        }
        transport = null;
        node = null;
    }


    /**
     * Read in and complete a list of commands form the provided file.
     * @param commandsPath file to read from
     */
    public void doCommands(String commandsPath) {
        readIn.setCommandFile(commandsPath);

        String[] input;

        while((input = readIn.readCommand()) != null) {
            decipherCommand(input);
        }
    }


    /**
     * Call the command that coresponds to the command.
     * @param command command read in
     */
    private void decipherCommand(String[] command) {
        switch(command[0]) {
            case "put": handlePut(command);
                        break;
            case "get": handleGet(command);
                        break;
            case "print": handlePrint(command);
                        break;
        }
    }


    /**
     * Give a put request to the DHT.
     * @param command the put request
     */
    private void handlePut(String[] command) {
        if (command.length != 3) {
            System.out.println("ERROR: ClientManager.handlePut(): put command of length "  +
                                command.length + " is invalid. Command should be of" +
                                " the form 'put, <word>, <definition>'");
            System.exit(1);
        }

        System.out.println("ClientManager.handlePut():");
        System.out.println("\tword: " + command[1]);
        System.out.println("\tdefiniton: " + command[2]);
    }


    /**
     * Give a get request to the DHT. Print the results.
     * @param command the get request
     */
    private void handleGet(String[] command) {
        if (command.length != 3) {
            System.out.println("ERROR: ClientManager.handleGet(): get command of length "  +
                                command.length + " is invalid. Command should be of" +
                                " the form 'get, <word>'");
            System.exit(1);
        }

        System.out.println("ClientManager.handleGet():");
        System.out.println("\tword: " + command[1]);
    }


    /**
     * Give a GetNodeStructure request to the DHT. Print the results.
     * @param command
     */
    private void handlePrint(String[] command) {
        if (command.length != 1) {
            System.out.println("ERROR: ClientManager.handlePrint(): print command of length "  +
                                command.length + " is invalid. Command should be of" +
                                " the form 'print'");
            System.exit(1);
        }

        System.out.println("ClientManager.handleGet():");
        System.out.println("\tword: " + command[1]);
    }
}
