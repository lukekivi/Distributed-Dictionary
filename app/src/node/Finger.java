package node;

public class Finger {
    int start;
    int end;
    Node succ;

    public Finger(NodeDetails succ) {
        this.succ = succ;
    } 

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