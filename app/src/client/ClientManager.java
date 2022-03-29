package client;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import pa2.*;
import utils.ReadIn;
import utils.ServerInfo;
import utils.SuperConn;
import utils.ConnFactory;
import utils.NodeConn;
import utils.Print;


public class ClientManager {
    private final ReadIn readIn = new ReadIn();
    private ConnFactory connFactory = new ConnFactory();
    private ServerInfo superInfo = null;
    private NodeConn nodeConn;

    /**
     * Connect the ClientManager to the DHT. The process sets nodeConn as an ambassador node
     */
    public void connectToDHT() {
        try {
            SuperConn superConn = connFactory.makeSuperConn(getSuperNodeInfo());

            
            // get a node from supernode for communication with the DHT
            NodeForClientData nodeData = superConn.client.GetNodeForClient(); 

            if (nodeData.status == Status.ERROR) {
                System.out.println("ERROR: Client.connectToDHT: received an error from the supernode.\n" +
                    "\tmsg: " + nodeData.msg);
                System.exit(1);
            }

            System.out.println("Got node " + nodeData.nodeInfo.id + " as an ambassador.");
        
            setNode(nodeData.nodeInfo);

            connFactory.closeSuperConn(superConn);
        } catch (TTransportException x) {
            System.out.println("Server not running as expected.");
            System.exit(1);
        }   catch (TException x) {
            x.printStackTrace();
        }
    }


    /**
     * Setup connection to Node for communication with the DHT.
     * @param nodeInfo designated ambassador node
     */
    private void setNode(NodeDetails nodeInfo) {
        try {
            nodeConn = connFactory.makeNodeConn(nodeInfo);

        } catch (TTransportException x) {
            System.out.println("ERROR: ClientManager.setNode()"); 
            x.printStackTrace();
            System.exit(1);
        }
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
        try {
            if (!nodeConn.transport.isOpen()) {
                System.out.println("ERROR: ClientManager.handlePut() - nodeTransport closed!");
                System.exit(1);
            }

            if (command.length != 3) {
                System.out.println("ERROR: ClientManager.handlePut(): put command of length "  + command.length + " is invalid. Command should be of" +
                    " the form 'put, <word>, <definition>'");
                System.exit(1);
            }

            System.out.println("ClientManager.handlePut():");
            System.out.println("\tword: " + command[1]);
            System.out.println("\tdefiniton: " + command[2]);

            StatusData statusData = nodeConn.client.Put(command[1], command[2]);

            if (statusData.status == Status.SUCCESS) {
                System.out.println(command[1] + " successfully entered into the dictionary.");
            } else {
                System.out.println(command[1] + " failed to be entered into the dictionary.\n\t" + statusData.msg);
            }

        } catch (TException x) {
            System.out.println("ERROR: ClientManager.handlePut() - TException occurred in command " + command[0]);
            x.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Give a get request to the DHT. Print the results.
     * @param command the get request
     */
    private void handleGet(String[] command) {
        try {
            if (!nodeConn.transport.isOpen()) {
                System.out.println("ERROR: ClientManager.handleGet() - nodeTransport closed!");
                System.exit(1);
            }

            if (command.length != 2) {
                System.out.println("ERROR: ClientManager.handleGet(): get command of length " + command.length + " is invalid. Command should be of" +
                    " the form 'get, <word>'");
                System.exit(1);
            }

            System.out.println("ClientManager.handleGet():");
            System.out.println("\tword: " + command[1]);

            GetData getData = nodeConn.client.Get(command[1]);

            if (getData.status == Status.SUCCESS) {
                System.out.println("Successful Get:\n\t" + command[1] + " : " + getData.definition);
            } else {
                System.out.println(command[1] + " failed to be entered into the dictionary.\n\t" + getData.msg);
            }
        } catch (TException x) {
            System.out.println("ERROR: ClientManager.handleGet() - TException occurred in command " + command[0]);
            x.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Give a GetNodeStructure request to the DHT. Print the results.
     * @param command
     */
    private void handlePrint(String[] command) {
        try {
            if (command.length != 1) {
                System.out.println("ERROR: ClientManager.handlePrint(): print command of length "  +
                                    command.length + " is invalid. Command should be of" +
                                    " the form 'print'");
                System.exit(1);
            }
            
            SuperConn superConn = connFactory.makeSuperConn(getSuperNodeInfo());
            DHTData dhtData = superConn.client.GetDHTStructure(); 
            connFactory.closeSuperConn(superConn);

            if (dhtData.status == Status.ERROR) {
                System.out.println("ERROR: ClientManager.handlePrint()\n\t" + dhtData.msg);
                System.exit(1);
            } else {
                for (int i = 0; i < dhtData.nodeStructures.size(); i++) {
                    Print.nodeStructure(dhtData.nodeStructures.get(i));
                }
            }
        } catch (TException x) {
            System.out.println("ERROR: ClientManager.handlePrint() - TException occurred in command " + command[0]);
            x.printStackTrace();
            System.exit(1);
        }
    }


    public void close() {
        connFactory.closeNodeConn(nodeConn);
    }


    private ServerInfo getSuperNodeInfo() {
        if (superInfo == null) {
            superInfo = readIn.getSuperNodeInfo();
        }
        return superInfo;
    }
}
