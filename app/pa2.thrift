# Charles Droege
# droeg022
# Lucas Kivi
# kivix019

namespace java pa2

/**
 * App status.
 */
enum Status {
    SUCCESS = 0,
    ERROR = 1
}


/**
 * DHT join status of a node.
 */
enum JoinStatus {
    ORIGINAL = 0,
    SUCCESS = 1,
    BUSY = 2,
    ERROR = 3
}


/**
 * Data need to access a node.
 */
struct NodeDetails {
    1: i32 id,
    2: string ip,
    3: i32 port
}


/**
 * Model for a dictionary entry.
 */
struct Entry {
    1: string word,
    2: string definition
}


/**
 * Finger entry for the finger table.
 */
struct Finger {
    1: i32 start,
    2: i32 last,
    3: NodeDetails succ
}


/**
 * Data structure that carries information about a nodes structure.
 */
struct NodeStructure {
    1: i32 id,
    3: i32 predId,
    4: list<Entry> entries,
    5: list<Finger> fingers
}


/**
 * Simple status data structure.
 */
struct StatusData {
    1: Status status,
    2: string msg
}


/**
 * Node for client data that a  SuperNode returns 
 */
struct NodeForClientData {
    1: NodeDetails nodeInfo,
    2: Status status,
    3: string msg
}


/**
 * Data about a node join operation
 */
struct NodeJoinData {
    1: i32 id,
    2: i32 m,
    3: NodeDetails nodeInfo,
    4: JoinStatus status,
    5: string msg
}


/**
 * Data bout the DHT as a whole
 */
struct DHTData {
    1: list<NodeStructure> nodeStructures,
    2: Status status,
    3: string msg
}


/**
 * The SuperNode of the DHT. This supernode is responsible for onboarding clients,
 * nodes, and providing the client with the data about the DHT as a whole.
 */
service SuperNode {
    NodeForClientData GetNodeForClient(),
    NodeJoinData GetNodeForJoin(),
    StatusData PostJoin(1: NodeDetails nodeInfo),
    DHTData GetDHTStructure(),
    oneway void KillDHT()
}


/**
 * Data for a Get() operation
 */
struct GetData {
    1: string definition,
    2: Status status,
    3: string msg
}

/**
 * Data for a findWord() operation
 */
struct EntryData {
    1: Entry entry,
    2: Status status,
}


/**
 * Data returned by a node to represent its structure.
 */
struct NodeStructureData {
    1: NodeStructure nodeStructure,
    2: Status status,
    3: string msg
}

/**
 * The core entity in the DHT. Nodes are responsible for storing Entrys.
 * Clients and Nodes can call Get() and Put() on them.
 * The SuperNode can ask it for its structure.
 * When new nodes enter the DHT they might update other nodes predecessors, 
 * successors, and/or finger tables.
 */
service Node {
    NodeStructureData GetNodeStructure(),
    StatusData UpdateFingerTable(1: NodeDetails node, 2: i32 i)
    NodeDetails GetSucc(),
    StatusData SetSucc(1: NodeDetails nodeInfo),
	NodeDetails GetPred(),
    StatusData SetPred(1: NodeDetails nodeInfo),
	NodeDetails FindSuccessor(1: i32 id),
    NodeDetails ClosestPrecedingFinger(1: i32 id),
    GetData Get(1: string word),
    StatusData Put(1: string word, 2: string definition),
    StatusData InsertWordHelper(1: string word, 2: string definition, 3: i32 wordId),
    StatusData FindPredCachingHelper(1: string word, 2: string definition, 3: i32 wordId),
    oneway void Kill()
}




