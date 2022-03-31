package utils;


import pa2.DHTData;
import pa2.NodeStructure;
import pa2.Entry;
import utils.Hash;
import utils.Range;
import java.util.*;


/**
 * Used to test the DHT. Generally gets called from a client and uses the DHTData structure.
 */
public class Test {
    private int M;

    public Test(int M) {
        this.M = M;
    }

    /**
     * Check the correctness of all nodes in the system. This includes checking their predecessor
     * id, fingers, and entries.
     */
    public void CheckNodes(DHTData dhtData) {
        System.out.println("********************* Starting Node Test *********************");

        int numFalse = 0;

        for (int i = 0; i < dhtData.nodeStructures.size(); i++) {;
            Print.nodeStructure(dhtData.nodeStructures.get(i));
            boolean pred = CheckNodePred(dhtData, i);
            boolean table = CheckNodeTable(dhtData, i);
            boolean entries = checkHashTable(dhtData, i);

            System.out.println();

            if (!pred || !table || !entries) {
                numFalse++;
            }
        }

        System.out.println("*********************       Summary       *********************");

        System.out.println("Node ids:");
        for (int i = 0; i < dhtData.nodeStructures.size(); i++) {
           System.out.println("\t" + dhtData.nodeStructures.get(i).id);
        }
        
        if (numFalse == 0) {
            System.out.println("All nodes correct!");
        } else {
            System.out.println(numFalse + " node(s) were incorrect...");
        }

        System.out.println("********************* Completed Node Test *********************");
    }

    private boolean CheckNodePred(DHTData dhtData, int index) {
        // check pred
        int id = dhtData.nodeStructures.get(index).id;
        int correctPred = (dhtData.nodeStructures.size() == 1) ? dhtData.nodeStructures.get(0).id : -1;
        int leastDiff = (int) Math.pow(2, M + 1);

        for (int i = 0; i < dhtData.nodeStructures.size(); i++) {
            int currId = dhtData.nodeStructures.get(i).id;

            if (currId == id) {
                continue;
            } 

            int diff = CalcCircularPrecedence(id, currId);
            if (diff < leastDiff) {
                leastDiff = diff;
                correctPred = currId;
            }
        }

        int actualId = dhtData.nodeStructures.get(index).predId;
        boolean isCorrect = (correctPred == actualId);

        if (isCorrect) {
            System.out.println("Pred: correct");
        } else {
            System.out.println("Pred: incorrect, " + actualId + " is not " + correctPred);
        }
        return isCorrect;
    }


    /**
     * Check how far end is behind start in a circular manner.
     */
    private int CalcCircularPrecedence(int start, int end) {
        int val;
        if (start < end) {
            val = start + (((int) Math.pow(2, M)) - end);
        } else {
            val = start - end;
        }

        return val;
    }


    private boolean CheckNodeTable(DHTData dhtData, int index) {
        int numFalse = 0;
        int nodeId = dhtData.nodeStructures.get(index).id;

        for (int i = 0; i < M; i++) {
            int succId = CircularAddition(nodeId, ((int) Math.pow(2, i)));
            int correctSucc = -1;
            int leastDiff = (int) Math.pow(2, M + 1);

            for (int j = 0; j < dhtData.nodeStructures.size(); j++) {
                int currId = dhtData.nodeStructures.get(j).id;

                int diff = CalcCircularSubsequence(succId, currId);
                if (diff < leastDiff) {
                    leastDiff = diff;
                    correctSucc = currId;
                }
            }

            int fingerIdActual = dhtData.nodeStructures.get(index).fingers.get(i).succ.id;

            if (correctSucc == fingerIdActual) {
                System.out.println("finger[" + i + "]: correct");
            } else {
                System.out.println("finger[" + i + "]: false, " + fingerIdActual + " is not " + correctSucc);
                numFalse++;
            }
        }

        return numFalse == 0;
    }

    /**
     * Check how far end is after start in a circular manner.
     */
    private int CalcCircularSubsequence(int start, int end) {
        int val;
        if (end < start) {
            val = end + (((int) Math.pow(2, M)) - start);
        } else {
            val = end - start;
        }

        return val;
    }


    /**
     * Perform circular addition.
     */
    private int CircularAddition(int a, int b) {
        int result = a + b;
        int maxKey = ((int) Math.pow(2, M)) - 1;
        if(result > maxKey) {
            result = result - maxKey - 1;
        }

        return result;
    }

    public boolean checkHashTable(DHTData dhtData, int index) {
        NodeStructure node = dhtData.nodeStructures.get(index);
        List<Entry> entryList = node.entries;
        int predId = node.predId;
        int nodeId = node.id;
        for (Entry listEntry : entryList) {
            String word = listEntry.word;
            int maxKey = ((int) Math.pow(2, M)) - 1;
            int key = Hash.makeKey(word, maxKey);

            if (!(Range.InRangeExIn(key, predId, nodeId))) {
                System.out.println("False so return is triggered");
                return false;
            }
            System.out.println("id: " + key + ", word: " + word + ", node" + nodeId + ". Appeared in the correct node, congrats");
        }
        return true;
    }
}
