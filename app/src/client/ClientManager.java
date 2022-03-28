package client;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import pa2.Node;
import pa2.NodeDetails;
import pa2.NodeForClientData;
import pa2.Status;
import pa2.SuperNode;
import pa2.GetData;
import utils.ReadIn;
import utils.ServerInfo;
import utils.ThriftConnection;


public class ClientManager {
    private final ReadIn readIn = new ReadIn();
    private ThriftConnection nodeConn;

    /**
     * Connect the ClientManager to the DHT. The process sets nodeConn as an ambassador node
     */
    public void connectToDHT() {
        try {
            ThriftConnection superNodeConn = getSuperNodeConn();

            // get a node from supernode for communication with the DHT
            NodeForClientData nodeData = ((SuperNode.Client) superNodeConn.client).GetNodeForClient(); 
            System.out.println("Node data:" +
                "\n\tid: " + nodeData.nodeInfo.id +
                "\n\tip: " + nodeData.nodeInfo.ip +
                "\n\tport: " + nodeData.nodeInfo.port +
                "\n\tStatus: " + nodeData.status +
                "\n\tMsg: " + nodeData.msg + "\n"
            );

            if (nodeData.status == Status.ERROR) {
                System.out.println("ERROR: Client.connectToDHT: received an error from the supernode.\n" +
                    "\tmsg: " + nodeData.msg);
                System.exit(1);
            }

            setNode(nodeData.nodeInfo);

            closeConn(superNodeConn);
        } catch (TTransportException x) {
            System.out.println("Server not running as expected.");
            System.exit(1);
        }   catch (TException x) {
            x.printStackTrace();
        }
    }


    private ThriftConnection getSuperNodeConn() throws TTransportException {
        ThriftConnection superNodeConn = new ThriftConnection();
        ServerInfo superNode = getSuperNodeInfo();
    
        superNodeConn.transport = new TSocket(superNode.ip, superNode.port);
        superNodeConn.transport.open();

        TProtocol protocol = new  TBinaryProtocol(superNodeConn.transport);
        superNodeConn.client = new SuperNode.Client(protocol);

        return superNodeConn;
    }


    /**
     * Read SuperNode details from config file.
     */
    private ServerInfo getSuperNodeInfo() {
        return readIn.getSuperNodeInfo();
    }


    /**
     * Setup connection to Node for communication with the DHT.
     * @param nodeInfo designated ambassador node
     */
    private void setNode(NodeDetails nodeInfo) {
        try {
            nodeConn = new ThriftConnection();

            nodeConn.transport = new TSocket(nodeInfo.ip, nodeInfo.port);
            nodeConn.transport.open();

            TProtocol protocol = new  TBinaryProtocol(nodeConn.transport);
            nodeConn.client = new Node.Client(protocol);
        } catch (TTransportException x) {
            System.out.println("ERROR: ClientManager.setNode()"); 
            x.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Close connection to the node
     */
    private void closeConn(ThriftConnection conn) {
        if (conn.transport.isOpen()) {
            conn.transport.close();
        }
        conn = null;
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
        try {
            switch(command[0]) {
                case "put": handlePut(command);
                            break;
                case "get": handleGet(command);
                            break;
                case "print": handlePrint(command);
                            break;
            }
        } catch (TException x) {
            System.out.println("ERROR: ClientManager.decipherCommand() - TException occurred in command " + command[0]);
            x.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Give a put request to the DHT.
     * @param command the put request
     */
    private void handlePut(String[] command) throws TException {
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

        // StatusData statusData = ((Node.Client) nodeConn.client).Put(command[1], command[2]);

        // if (statusData.status == Status.SUCCESS) {
        //     System.out.println(command[1] + " successfully entered into the dictionary.");
        // } else {
        //     System.out.println(command[1] + " failed to be entered into the dictionary.\n\t" + statusData.msg);
        // }
    }


    /**
     * Give a get request to the DHT. Print the results.
     * @param command the get request
     */
    private void handleGet(String[] command) throws TException {
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

        GetData getData = ((Node.Client) nodeConn.client).Get(command[1]);

        if (getData.status == Status.SUCCESS) {
            System.out.println("Successful Get:\n\t" + command[1] + " : " + getData.definition);
        } else {
            System.out.println(command[1] + " failed to be entered into the dictionary.\n\t" + getData.msg);
        }
    }


    /**
     * Give a GetNodeStructure request to the DHT. Print the results.
     * @param command
     */
    private void handlePrint(String[] command) throws TException {

        if (command.length != 1) {
            System.out.println("ERROR: ClientManager.handlePrint(): print command of length "  +
                                command.length + " is invalid. Command should be of" +
                                " the form 'print'");
            System.exit(1);
        }

        System.out.println("ClientManager.handleGet():");
        System.out.println("\tword: " + command[1]);
    }


    public void close() {
        closeConn(nodeConn);
    }
}
