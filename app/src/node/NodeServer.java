package node;

import utils.ReadIn;
import utils.ServerInfo;
import utils.Print;
import pa2.Node;
import pa2.SuperNode;
import pa2.NodeJoinData;
import pa2.NodeDetails;
import pa2.JoinStatus;
import pa2.Status;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
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

            nodeInfo.ip = InetAddress.getLocalHost().getHostName() + ".cselabs.umn.edu";
            nodeInfo.port = r.getNodePort();

            manager = new NodeManager(nodeInfo);
            NodeHandler handler = new NodeHandler(manager);

            // enter into the DHT
            if (establishSelf(handler) == Status.ERROR) {
                return;
            }
            
            // Perform server duties
            Node.Processor processor = new Node.Processor<NodeHandler>(handler);
            Runnable simple = new Runnable() {
                public void run() {
                    simple(processor, nodeInfo.port);
                }
            };
            new Thread(simple).start();

        } catch (Exception e) {
            System.out.println("ERROR: NodeServer.main()");
            e.printStackTrace();
        }
    }


    private static Status establishSelf(NodeHandler handler) {
        Status status = Status.ERROR;
        try {
            ServerInfo superNode = r.getSuperNodeInfo();

            TTransport transport = new TSocket(superNode.ip, superNode.port);
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            SuperNode.Client superNodeClient = new SuperNode.Client(protocol);

            status = joinDHT(superNodeClient);

            transport.close();

        } catch (TTransportException x) {
            System.out.println("Error: Node can't connect to the superNode");
            x.printStackTrace();
            System.exit(1);
        } catch (TException x) {
            System.out.println("Error: Node couldn't call GetNodeForJoin() on the superNode");
            System.exit(1);
        }

        return status;
    }


    private static Status joinDHT(SuperNode.Client superNodeClient) throws TException {
        // Get join data from supernode
        NodeJoinData joinData = superNodeClient.GetNodeForJoin();
        
        if (joinData.status == JoinStatus.ERROR) {
            System.out.println("ERROR: NodeServer.joinDHT() \n\t" + joinData.msg);
            return Status.ERROR;
        } else if (joinData.status == JoinStatus.BUSY) {
            System.out.println("NodeServer.joinDHT() - SuperNode was busy onboarding another node.");
            return Status.ERROR;
        }

        Print.nodeJoinData(joinData);

        int cacheSize = r.getNodeCacheSize();
        manager.setLog(joinData.id);
        
        manager.Join(joinData, cacheSize);

        superNodeClient.PostJoin(manager.info);

        return Status.SUCCESS;
    }


    public static void simple(Node.Processor processor, int port) {
        try {
            TServerTransport serverTransport = new TServerSocket(port);
            TServer node = new TThreadPoolServer( // Auto creates new threads for each task
                new TThreadPoolServer.Args(serverTransport).processor(processor)
            );

            System.out.println("Starting the multi-threaded NodeServer...");
            node.serve();
        } catch (Exception e) {
            System.out.println("NodeServer: Client connection closed with exception.");
            e.printStackTrace();
        }

        System.out.println("Closed the stream");
        manager.closeLog();
    }
}