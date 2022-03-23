package Experiment;

import java.util.*;

public class NodeExperiment {

    private static final int M = 5;
    private static ArrayList<Node> nodes = new ArrayList<Node>();
    
    public static void main(String[] args) {

        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        AddNode();
        

        CheckNodes();

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

    private static void AddNode(int id) {
        int size = nodes.size();
    
        Node node = new Node(M);

        if (size == 0) {
            node.Join(null, id);
        } else {
            node.Join(nodes.get(size-1), id);
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

        Random r = new Random();
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

    private static void CheckNodes() {
        int numFalse = 0;

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.PrintNode();
            boolean pred = CheckNodePred(node);
            boolean table = CheckNodeTable(node);
            System.out.println();

            if (!pred || !table) {
                numFalse++;
            }
        }

        System.out.println("Node ids:");
        for (int i = 0; i < nodes.size(); i++) {
            System.out.println("\t" + nodes.get(i).GetId());
        }
        
        if (numFalse == 0) {
            System.out.println("All nodes correct!");
        } else {
            System.out.println(numFalse + " node(s) were incorrect...");
        }
    
    }

    private static boolean CheckNodeTable(Node node) {
        Finger[] fingers = node.GetFingers();
        int numFalse = 0;
        for (int i = 0; i < M; i++) {
            int succId = CircularAddition(node.GetId(), ((int) Math.pow(2, i)));
            int correctSucc = -1;
            int leastDiff = (int) Math.pow(2, M + 1);

            for (int j = 0; j < nodes.size(); j++) {
                int currId = nodes.get(j).GetId();

                int diff = CalcCircularSubsequence(succId, currId);
                if (diff < leastDiff) {
                    leastDiff = diff;
                    correctSucc = currId;
                }
            }

            
            if (correctSucc == fingers[i].succ.GetId()) {
                System.out.println("finger[" + i + "]: correct");
            } else {
                System.out.println("finger[" + i + "]: false, " + fingers[i].succ.GetId() + " is not " + correctSucc);
                numFalse++;
            }
        }

        return numFalse == 0;
    }

    private static boolean CheckNodePred(Node node) {
        // check pred
        int id = node.GetId();
        int correctPred = -1;
        int leastDiff = (int) Math.pow(2, M + 1);

        for (int i = 0; i < nodes.size(); i++) {
            int currId = nodes.get(i).GetId();

            if (currId == id) {
                continue;
            } 

            int diff = CalcCircularPrecedence(id, currId);
            if (diff < leastDiff) {
                leastDiff = diff;
                correctPred = currId;
            }
        }

        System.out.println("Pred: " + (correctPred == node.GetPredId()));
        return (correctPred == node.GetPredId());
    }

    private static int CalcCircularPrecedence(int start, int end) {
        int val;
        if (start < end) {
            val = start + (((int) Math.pow(2, M)) - end);
        } else {
            val = start - end;
        }

        return val;
    }

    private static int CalcCircularSubsequence(int start, int end) {
        int val;
        if (end < start) {
            val = end + (((int) Math.pow(2, M)) - start);
        } else {
            val = end - start;
        }

        return val;
    }


    private static int CircularAddition(int a, int b) {
        int result = a + b;
        int maxKey = ((int) Math.pow(2, M)) - 1;
        if(result > maxKey) {
            result = result - maxKey - 1;
        }

        return result;
    }
 }