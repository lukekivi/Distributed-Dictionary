package utils;

import pa2.NodeStructure;
import pa2.Finger;
import pa2.Entry;
import pa2.NodeDetails;
import pa2.NodeJoinData;

public class Print {
    public static void nodeStructure(NodeStructure node) {
        System.out.println("Node[" + node.id + "]:\n" + 
            "\t- pred: " + node.predId + "\n" +
            "\t- table:");

        for(int i = 0; i < node.fingers.size(); i++) {
            System.out.print("\t\t");
            if (node.fingers.get(i) == null) {   
                System.out.println("null");
            } else {
                finger(node.fingers.get(i));
            }
        }

        System.out.println("\t- entries:");

        if (node.entries.size() == 0) {
            System.out.println("\t\tEMPTY");
        }

        for(int i = 0; i < node.entries.size(); i++) {
            System.out.print("\t\t");
            if (node.entries.get(i) == null) {   
                System.out.println("null");
            } else {
                entry(node.entries.get(i));
            }
        }

        System.out.println();
    }


    public static void finger(Finger finger) {        
        System.out.println("start: " + finger.start + " end: " + finger.last + " succ: " + finger.succ.id);
    }


    public static void entry(Entry entry) {
        System.out.println(entry.word + " : " + entry.definition);
    }


    public static void nodeDetails(NodeDetails nodeInfo) {
        System.out.println(
            "Node " + nodeInfo.id + "\n" +
            "\tip: " + nodeInfo.ip + 
            "\tport: " + nodeInfo.port
        );
    }

    public static void nodeJoinData(NodeJoinData nodeJoinData) {
        System.out.println("NodeJoinData:"
            + "\n\tnew node id: " + nodeJoinData.id
            + "\n\tstatus: " + nodeJoinData.status
            + "\n\tmsg: " + nodeJoinData.msg
            + "\nnode for setup:"
        );
        if (nodeJoinData.nodeInfo == null) {
            System.out.println("no node provided; this is the original");
        } else {
            nodeDetails(nodeJoinData.nodeInfo);
        }
    }
}
