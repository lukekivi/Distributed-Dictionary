package node;

import utils.ReadIn;
import utils.ServerInfo;
import pa2.Node;
import pa2.SuperNode;
import pa2.NodeJoinData;
import pa2.NodeDetails;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import java.io.FileNotFoundException;
import java.net.InetAddress;

public class NodeServer {
    private static ReadIn r = new ReadIn();
    private static NodeManager manager;
    public static void main(String[] args) {
        try {
            Logger.getRootLogger().setLevel(Level.ERROR);
            NodeDetails nodeInfo = new NodeDetails();

            nodeInfo.ip = InetAddress.getLocalHost().getHostName();
            nodeInfo.port = r.getNodePort();

            manager = new NodeManager(nodeInfo);
            NodeHandler handler = new NodeHandler(manager);

            // enter into the DHT
            establishSelf(handler);
            
            // Perform server duties
            Node.Processor processor = new Node.Processor<NodeHandler>(handler);
            Runnable simple = new Runnable() {
                public void run() {
                    simple(processor, nodeInfo.port);
                }
            };
            new Thread(simple).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void establishSelf(NodeHandler handler) {
        try {
            ServerInfo superNode = r.getSuperNodeInfo();

            TTransport transport = new TSocket(superNode.ip, superNode.port);
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            SuperNode.Client superNodeClient = new SuperNode.Client(protocol);

            joinDHT(superNodeClient);

            transport.close();
        } catch (TTransportException x) {
            System.out.println("Server not running as expected.");
            System.exit(1);
        } catch (TException x) {
            x.printStackTrace();
            System.exit(1);
        }
    }


    private static void joinDHT(SuperNode.Client superNodeClient) throws TException {
        // Get join data from supernode
        NodeJoinData joinData = superNodeClient.GetNodeForJoin(); 

        System.out.println("NodeData:"
            + "\n\tid: " + joinData.id
            + "\n\tnodeInfo: " + joinData.nodeInfo
            + "\n\tstaus: " + joinData.status
            + "\n\tmsg: " + joinData.msg);

        int cacheSize = r.getNodeCacheSize();
        manager.setLog(joinData.id);
        
        // manager.Join(joinData, cacheSize);

        // superNodeClient.PostJoin(manager.info);

        // *** THIS IS TEMPORARY FOR TESTING - uncomment above
        NodeDetails node = manager.info;
        node.id = joinData.id;
        superNodeClient.PostJoin(node);
        // *** FINISH TESTING

    }


    public static void simple(Node.Processor processor, int port) {
        try {
            TServerTransport serverTransport = new TServerSocket(port);
            TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

            System.out.println("Starting the NodeServer...");
            server.serve();
        } catch (Exception e) {
            System.out.println("NodeServer: Client connection closed with exception.");
            e.printStackTrace();
        }

        System.out.println("Closed the stream");
        manager.closeLog();
    }
}