package client;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import pa2.Node;
import utils.ReadIn;
import utils.ServerInfo;


public class ClientManager {
    private final ReadIn readIn = new ReadIn();

    public ServerInfo getSuperNodeInfo() {
        return readIn.getSuperNodeInfo();
    }

    public void doCommands(String commandsPath) {
        readIn.setCommandFile(commandsPath);

        String[] input;

        while((input = readIn.readCommand()) != null) {
            for (int i = 0; i < input.length; i++) {
                System.out.print(input[i]);
            }
            System.out.println();
        }
    }
}
