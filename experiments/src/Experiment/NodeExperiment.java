package Experiment;

import java.util.*;

public class NodeExperiment {

    private static final int M = 3;
    private static ArrayList<Node> nodes = new ArrayList<Node>();
    
    public static void main(String[] args) {
        
        AddNode();
        PrintStructure();

        AddNode();
        PrintStructure();

        // AddNode();
        // PrintStructure();
    }

    private static boolean InRange(int id, int start, int end) {
    
        if(end < start) {
            
            if (id <= end) {
                return true;
            } else if (id > start) {
                return true;
            } else {
                return false;
            }

        }
            
        return ((id > start) && (id <= end));
        
    }

    /**
     * SuperNode knows the successor and predecessor so
     * it can set those fields.
     */
    private static void AddNode() {
        int size = nodes.size();
    
        Node node = new Node(M);

        if (size == 0) {
            node.Join(null, GetHashID());
        } else {
            node.Join(nodes.get(size-1), GetHashID());
        }

        nodes.add(node);
    }

    private static void PrintStructure() {
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).PrintNode();
        }
    }

    private static int GetHashID() {
        int maxSize = ((int) Math.pow(2, M));

        if (nodes.size() == maxSize) {
            System.out.println("ERROR: exceeded max size.");
            System.exit(-1);
        }

        Random r = new Random(System.currentTimeMillis());
        int id = r.nextInt(maxSize - 1);

        while (!IsAvailable(id)) {
            if (id == maxSize - 1) {
                id = 0;
            } else {
                id++;
            }
        }
        return id;        
    }

    private static boolean IsAvailable(int id) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).GetId() == id) {
                return false;
            }
        }
        return true;
    }
 }