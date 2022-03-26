package superNode;

import pa2.DHTData;
import pa2.NodeDetails;
import pa2.NodeForClientData;
import pa2.NodeJoinData;
import pa2.Status;
import pa2.JoinStatus;
import pa2.StatusData;
import pa2.SuperNode;

public class SuperNodeHandler implements SuperNode.Iface {
    


    @Override
    public NodeForClientData GetNodeForClient() {
        NodeForClientData nodeData = new NodeForClientData();
        NodeDetails nodeDetails = new NodeDetails();

        nodeDetails.port = 1;
        nodeDetails.ip = "ip address";

        nodeData.nodeInfo = nodeDetails;    
        nodeData.status = Status.SUCCESS;
        nodeData.msg = "All good in the hood.";

        return nodeData;
    }

    @Override
    public NodeJoinData GetNodeForJoin() {
        NodeJoinData nodeJoinData = new NodeJoinData();
        NodeDetails nodeInfo = new NodeDetails();
        nodeJoinData.id = 1;

        nodeInfo.id = 2;
        nodeInfo.ip = "ip address";
        nodeInfo.port = 2;

        nodeJoinData.nodeInfo = nodeInfo;

        nodeJoinData.status = JoinStatus.NEW;
        nodeJoinData.msg = "We've got this.";

        return nodeJoinData;
    }

    @Override
    public StatusData PostJoin(String ip, int port) {
        return null;
    }

    @Override
    public DHTData GetDHTStructure() {
        return null;
    }
}
