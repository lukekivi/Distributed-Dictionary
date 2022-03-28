package node;

import utils.ReadIn;
import utils.ServerInfo;
import pa2.Node;
import pa2.SuperNode;
import pa2.NodeJoinData;
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

    private static ReadIn r;
    public static void main(String[] args) {
        if (args.length != 0) {
            System.out.println("No args taken.");
            System.exit(1);
        }
        try {
            InetAddress ipAddress = (InetAddress.getLocalHost());
            String ip = ipAddress.getHostAddress();
            r = new ReadIn();
            int port = r.getNodePort();

            NodeManager nodeManager = new NodeManager();
            nodeManager.info.port = port;
            nodeManager.info.ip = ip;

            NodeHandler handler = new NodeHandler();
            handler.info.port = port;
            handler.info.ip = ip;
            handler.manager = nodeManager;

            Node.Processor processor = new Node.Processor<NodeHandler>(handler);

            Runnable simple = new Runnable() {
                public void run() {
                    simple(processor, port);
                }
            };

            establishSelf(nodeManager, handler);
            new Thread(simple).start();


        } catch (Exception e) {
            e.printStackTrace();
        }
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
    }

    private static void establishSelf(NodeManager nodeManager, NodeHandler handler) {
        try {
            ServerInfo superNode = r.getSuperNodeInfo();

            TTransport transport = new TSocket(superNode.ip, superNode.port);
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            SuperNode.Client client = new SuperNode.Client(protocol);

            NodeJoinData joinData = client.GetNodeForJoin(); 
            int cacheSize = r.getNodeCacheSize();
            handler.InitializeNode(joinData, nodeManager.info.port);
            nodeManager.initManager(joinData, cacheSize);
            nodeManager.setLog(joinData.id);

            transport.close();
        } catch (TTransportException x) {
            System.out.println("Server not running as expected.");
            System.exit(1);
        } catch (TException x) {
            x.printStackTrace();
        }
    }
}