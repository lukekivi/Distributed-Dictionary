package node;

import pa2.Node;
import pa2.GetData;
import pa2.StatusData;
import pa2.NodeStructureData;
import pa2.NodeDetails;

public class NodeHandler implements Node.Iface {
    

    @Override
    public GetData Get(String word) {
        return null;
    }

    @Override
    public StatusData Put(String word, String definition) {
        return null;
    }

    @Override
    public NodeStructureData GetNodeStructure() {
        return null;
    }

    @Override
    public StatusData UpdatePredecessor(NodeDetails nodeInfo) {
        return null;
    }

    @Override
    public StatusData UpdateSuccessor(NodeDetails nodeInfo) {
        return null;
    }

    @Override
    public StatusData UpdateFingerTable() {
        return null;
    }
}
