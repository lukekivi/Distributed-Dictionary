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
import java.io.PrintStream;


public class NodeServer {
    private static final String LOG_FILE = "log/node1Log.txt";
    private static ReadIn readIn = new ReadIn();
    public static void main(String[] args) {
        try {

            PrintStream fileOut = new PrintStream(LOG_FILE);

            System.setOut(fileOut);

            NodeHandler handler = new NodeHandler();
            Node.Processor processor = new Node.Processor<NodeHandler>(handler);

            Runnable simple = new Runnable() {
                public void run() {
                    simple(processor);
                }
            };

            new Thread(simple).start();

            establishSelf();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void simple(Node.Processor processor) {
        try {
            // read in portnumber from the config file
            int port = readIn.getNodePort();

            TServerTransport serverTransport = new TServerSocket(port);
            TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

            System.out.println("Starting the NodeServer...");
            server.serve();
        } catch (Exception e) {
            System.out.println("NodeServer: Client connection closed with exception.");
            e.printStackTrace();
        }
    }

    private static void establishSelf() {
        try {
            ServerInfo superNode = readIn.getSuperNodeInfo();

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
        NodeJoinData nodeData;

        nodeData = client.GetNodeForJoin(); 
        System.out.println("Node data:" +
            "\n\tStatus: " + nodeData.status +
            "\n\tMsg: " + nodeData.msg + "\n"
        );
    }
}
