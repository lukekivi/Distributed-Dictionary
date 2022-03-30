package superNode;

import utils.ReadIn;
import utils.ServerInfo;
import pa2.SuperNode;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;


class SuperNodeServer {

    private static ReadIn readIn = new ReadIn();
    public static void main(String[] args) {
        try {
            Logger.getRootLogger().setLevel(Level.ERROR);

            SuperNodeHandler handler = new SuperNodeHandler(readIn.getM());
            SuperNode.Processor processor = new SuperNode.Processor<SuperNodeHandler>(handler);

            Runnable simple = new Runnable() {
                public void run() {
                    simple(processor);
                }
            };

            new Thread(simple).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void simple(SuperNode.Processor processor) {
        try {
            // read in portnumber from the config file
            ServerInfo superNode = readIn.getSuperNodeInfo();

            TServerTransport serverTransport = new TServerSocket(superNode.port);
            TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

            System.out.println("Starting the SuperNodeServer...");
            server.serve();
        } catch (Exception e) {
            System.out.println("SuperNodeServer: Client connection closed with exception.");
            e.printStackTrace();
        }
    }
}
