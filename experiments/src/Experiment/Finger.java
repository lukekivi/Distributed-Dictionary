package Experiment;

public class Finger {
    public Finger(Node succ) {
        this.succ = succ;
    } 

    int start;
    int end;
    Node succ;

    public void Print() {
        String succId;

        if (succ == null) {
            succId = "null";
        } else {
            succId = Integer.toString(succ.GetId());    
        }
        System.out.println("start: " + start + " end: " + end + " succ: " + succId);
    }
}
