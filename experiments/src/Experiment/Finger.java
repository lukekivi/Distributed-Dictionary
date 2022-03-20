package Experiment;

public class Finger {
    public Finger(Node succ) {
        this.succ = succ;
    } 

    int start;
    int end;
    Node succ;

    public void Print() {
        System.out.println("start: " + start + " end: " + end + " succ: " + succ.GetId());
    }
}
