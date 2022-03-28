package client;

import utils.ServerInfo;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import pa2.SuperNode;
import pa2.NodeForClientData;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

class Client {
    private static final ClientManager manager = new ClientManager();
    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.ERROR);

        try {
            ServerInfo superNode = manager.getSuperNodeInfo();

            TTransport transport = new TSocket(superNode.ip, superNode.port);
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            SuperNode.Client client = new SuperNode.Client(protocol);

            perform(client); // Passing job as arg for client

            transport.close();
        } catch (TTransportException x) {
            System.out.println("Server not running as expected.");
            System.exit(1);
        }   catch (TException x) {
            x.printStackTrace();
        }
    }

    private static void perform(SuperNode.Client client) throws TException {
        NodeForClientData nodeData = client.GetNodeForClient(); 
        System.out.println("Node data:" +
            "\n\tStatus: " + nodeData.status +
            "\n\tMsg: " + nodeData.msg + "\n"
        );


    }
}