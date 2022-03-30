package utils;


import pa2.DHTData;
import java.util.*;

public class Test {
    private int M;


    public Test(int M) {
        this.M = M;
    }

    public void CheckNodes(DHTData dhtData) {
        int numFalse = 0;

        for (int i = 0; i < dhtData.nodeStructures.size(); i++) {;
            Print.nodeStructure(dhtData.nodeStructures.get(i));
            boolean pred = CheckNodePred(dhtData, i);
            boolean table = CheckNodeTable(dhtData, i);
            System.out.println();

            if (!pred || !table) {
                numFalse++;
            }
        }

        System.out.println("Node ids:");
        for (int i = 0; i < dhtData.nodeStructures.size(); i++) {
           System.out.println("\t" + dhtData.nodeStructures.get(i).id);
        }
        
        if (numFalse == 0) {
            System.out.println("All nodes correct!");
        } else {
            System.out.println(numFalse + " node(s) were incorrect...");
        }
    
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


    private int CalcCircularSubsequence(int start, int end) {
        int val;
        if (end < start) {
            val = end + (((int) Math.pow(2, M)) - start);
        } else {
            val = end - start;
        }

        return val;
    }


    private int CircularAddition(int a, int b) {
        int result = a + b;
        int maxKey = ((int) Math.pow(2, M)) - 1;
        if(result > maxKey) {
            result = result - maxKey - 1;
        }

        return result;
    }
}
