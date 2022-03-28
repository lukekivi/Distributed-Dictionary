package client;

import utils.ServerInfo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import pa2.SuperNode;
import pa2.Node;
import pa2.Status;
import pa2.NodeForClientData;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

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

        try {
            ServerInfo superNode = manager.getSuperNodeInfo();

            TTransport transport = new TSocket(superNode.ip, superNode.port);
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            SuperNode.Client client = new SuperNode.Client(protocol);

            connectToDHT(client); // Passing job as arg for client

            transport.close();

            if (commandsPath != null) {
                manager.doCommands(commandsPath);
            }
            
        } catch (TTransportException x) {
            System.out.println("Server not running as expected.");
            System.exit(1);
        }   catch (TException x) {
            x.printStackTrace();
        }
    }

    private static void connectToDHT(SuperNode.Client client) throws TException {
        // get a node from supernode for communication with the DHT
        NodeForClientData nodeData = client.GetNodeForClient(); 
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

        manager.setNode(nodeData.nodeInfo);
    }
}