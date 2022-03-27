package Experiment;

import java.util.*;

public class NodeExperiment {

    private static final int M = 5;
    private static ArrayList<Node> nodes = new ArrayList<Node>();

    // private static String node0 = "csel-kh4250-30:5323"; 
    // private static String node1 = "csel-kh4250-31:9934";
    // private static String node2 = "csel-kh4250-32:6322";
    // private static String node3 = "csel-kh4250-33:9643";
    // private static String node4 = "csel-kh4250-34:9064";
    // private static String node5 = "csel-kh4250-35:5324";

    // private static String node6 = "csel-kh1250-20:9000";
    // private static String node7 = "csel-kh1250-21:8000";
    // private static String node8 = "csel-kh1250-22:5235";
    // private static String node9 = "csel-kh1250-23:6755";
    // private static String node10 = "csel-kh1250-24:1954";
    
    // private static String node11 = "csel-kh4250-30:9000";
    // private static String node12 = "csel-kh4250-30:9000";
    // private static String node13 = "csel-kh4250-30:9000";
    // private static String node14 = "csel-kh4250-30:9000";
    // private static String node15 = "csel-kh4250-30:9000";

    // private static String node16 = "csel-kh4250-30:9000";
    // private static String node17 = "csel-kh4250-30:9000";
    // private static String node18 = "csel-kh4250-30:9000";
    // private static String node19 = "csel-kh4250-30:9000";
    // private static String node20 = "csel-kh4250-30:9000";

    // private static String node21 = "csel-kh4250-30:9000";
    // private static String node22 = "csel-kh4250-30:9000";
    // private static String node23 = "csel-kh4250-30:9000";
    // private static String node24 = "csel-kh4250-30:9000";
    // private static String node25 = "csel-kh4250-30:9000";

    // private static String node26 = "csel-kh4250-30:9000";
    // private static String node27 = "csel-kh4250-30:9000";
    // private static String node28 = "csel-kh4250-30:9000";
    // private static String node29 = "csel-kh4250-30:9000";
    // private static String node30 = "csel-kh4250-30:9000";

    
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
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();
        // AddNode();

        System.out.println(put("House", "Building that humans live in."));
        System.out.println(put("apple", "Red fruit."));

        System.out.println(put("car", "Autmobile, form of transportation that a person operates."));
        System.out.println(put("school", "A place where students can learn."));
        System.out.println(put("mouse", "A small rodent."));
        System.out.println(put("keyboard", "Provides input for a pc and allows someone to operate one."));
        System.out.println(put("table", "Flat board with legs that objects can rest on."));
        System.out.println(put("snack", "Small portion of food to satisfy a small hunger."));
        // System.out.println(put("watch", "A mini clock that straps onto someone's wrist."));
        // System.out.println(put("shirt", "A piece of clothing that is worn on the upper body."));
        // System.out.println(put("ear", "Body part on the side of someones head, allows people to hear."));

        // put("House", "Building that humans live in.");
        // put("apple", "Red fruit.");
        // put("car", "Autmobile, form of transportation that a person operates.");
        // put("school", "A place where students can learn.");
        // put("mouse", "A small rodent.");
        // put("keyboard", "Provides input for a pc and allows someone to operate one.");
        // put("table", "Flat board with legs that objects can rest on.");
        // put("snack", "Small portion of food to satisfy a small hunger.");
        // put("watch", "A mini clock that straps onto someone's wrist.");
        // put("shirt", "A piece of clothing that is worn on the upper body.");
        // put("ear", "Body part on the side of someones head, allows people to hear.");

        // System.out.println(get("House"));
        // System.out.println(get("apple"));
        // System.out.println(get("House"));
        // System.out.println(get("car"));
        // System.out.println(get("school"));
        // System.out.println(get("mouse"));
        // System.out.println(get("keyboard"));
        // System.out.println(get("table"));
        // System.out.println(get("snack"));
        // System.out.println(get("watch"));
        // System.out.println(get("shirt"));
        // System.out.println(get("ear"));

        // System.out.println(get("invalid"));





        

        // CheckNodes();

    }


    /**
     * SuperNode knows the successor and predecessor so
     * it can set those fields.
     */
    private static void AddNode() {
        int size = nodes.size();
    
        Node node = new Node(M, 10);

        if (size == 0) {
            node.Join(null, GetHashID());
        } else {
            node.Join(nodes.get(size-1), GetHashID());
        }

        nodes.add(node);
    }


    private static void AddNode(int id) {
        int size = nodes.size();
    
        Node node = new Node(M, 10);

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

    /**
     * Get the definition of the requested word
     */
    private static String get(String word) {
        Random r = new Random();
        int maxKey = ((int) Math.pow(2, M)) - 1;
        int id = utils.hashFunction(word, maxKey);
        int index = r.nextInt(nodes.size());
        Node node = nodes.get(index);
        String ans = node.findWord(word);
        return ans;
    }

    /**
     * Put a word and definition in the DHT
     */
    private static String put(String word, String def) {
        Random r = new Random();
        int maxKey = ((int) Math.pow(2, M)) - 1;
        int id = utils.hashFunction(word, maxKey);
        int index = r.nextInt(nodes.size());
        Node node = nodes.get(index);
        String ans = node.putWord(word, def);
        return ans;
    }
 }