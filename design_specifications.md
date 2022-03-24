# Distributed Dictionary
Created by:\ 
Lucas Kivi - kivix019\
Charles Droege - droeg022

# Key Parts

## Keyspace Hashing Mechanism
We will use the md5 hash function. It's 128-bit and returns a string. Since our finger tables are going to be comparing id vals of the nodes and words, we can convert the hashed string to an integer by taking each char of the string and getting its ascii value (int) and adding them all together. Then we will mod the final int by (2^M) which would be the number of entries in a M-bit hash space so each finger table would have M entries. Then if the max id val is (2^M)-1, we could randomly assign an int between 0-((2^M)-1) to the nodes when they contact the SuperNode.

## Caching Mechanism
When a put request comes in, it will cache it at each node visited along the way to its destination node. When a second put request gets sent for the same word, the cache entries will also be updated. We can cap the cache space off and have a fifo system for replacement.

## Word Storage
Words and there definitions will be stored in Nodes within a local hash map. Entries will be of the form: (key = word, value = defintion).

# System Entities

## Client
The core interface to the DHT system. Is able to `Get()` and `Put()` dictionary entries in the system.

### Local Operations

#### Get()
Calls Node get function.

#### Put()
Calls Node put function.

#### StartClient()
Calls SuperNode GetNodeForClient function.

#### PrintStructure()
Calls `SuperNode.GetDHTStructure()` and then prints it.

```
- Node ID - range of keys - predecessor - number of words stored
Words List
Finger Table
```

## Supernode
### Interface Operations
#### GetNodeForClient()
Get a random node for the client to communicate with.
**Input**: void\
**Returns**: Status, NodeDetails\

####  GetNodeForJoin()
Sign up a node for the DHT system. If **NEW**, pass the requesting its *successor* so it can establish itself. Can be **BUSY**, which means try again later. The SuperNode blocks new calls to `GetNodeForJoin()` until `PostJoin()` is received.\
**Input**: ip, port - need node specific ip and port in SuperNode so it can be distributed to entities that want to contact it.\
**Returns**: JoinStatus, id, NodeDetails, msg - the join status, an assigned id for requesting node, nodedetails for an old node for the requesting node to talk to, a message.

#### PostJoin()
Alert SuperNode that a Node has finished joining the DHT.
**Input**: void\
**Returns**: Status, msg

#### GetDHTStructure()
Gets a list of `NodeStructure` objects. 
**Input**: void\
**Returns**: Status, msg, NodeStructure list

## Node
Each node maintains a hashmap of words it knows and a fingertable in order to communicate with other nodes. Each node also maintains `NodeDetails` objects that represents its predecesser and itself.

There will be some type of caching implemented, likely a hashmap of indefinite size. See **Caching Mechanism** section above for more details.

### Local Operations
#### StartNode()
Calls SuperNode function GetNodeForJoin(). Decide how to move forward based on JoinStatus. If **NEW** it uses old node details to establish itself and then alerts the SuperNode of its completion. If **BUSY**, user can try again later. If **ORIGINAL** initialize DHT, all keyspace will correspond to the sole node in the system.

In order for a node to establish itself it must do three things
1. Update its predecessor pointer, its predecessor's successor, and its successor's predecessor. 
2. Update its own fingertable.
3. Update fingers of existing nodes.

#### FindWord()
Encapsulate process of querying for word in the DHT. Check local cache map, if word is not there then look in other nodes.
**Input**: word
**Return**: definition

#### HashFunction()
Do the hashing.
**Input**: word, description
**Return**: integer key

#### PutWord()
Encapsulate the process of placing a word in the DHT
**Input**: word, description, key
**Return**: void

### Interface Operations
#### Get()
Compare hashed value. If the word belongs to this node, get it. Else check the cache, if not there, pass along to a better node in the finger table. Returns **ERROR** if it was supposed to be in this node and it wasn't. If node called `Get()` itself because it did not have the word.\
**Input**: word - the word user wants a definition for.\
**Returns**: Status, definition, msg - status represents the transaction conclusion and the msg either contains a definition or some error/failure message.

#### Put()
Compare hashed value. If the word belongs to this node, stash it. Else, cache it and send it along to a better node using the finger table. Wait for better node's response and return that back to sender.\
**Input**: word, meaning - the word user wants a definition for.\
**Returns**: Status, msg - status represents the transaction conclusion and the msg is a description of said conclusion

#### GetNodeStructure()
Get a `NodeStructure` object that represents the state of a node.
**Input**: void\
**Returns**: NodeStructure

#### UpdatePredecessor()
Called within GetFingerTable to update the predecessor field of a successor Node of a new Node.
**Input**: NodeDetails
**Returns**: Status, msg

#### UpdateSuccessor()
Called within GetFingerTable to update the successor field of a predecessor Node of a new Node.
**Input**: NodeDetails
**Returns**: Status, msg

#### UpdateDHT()
Called when a new node is entered into the DHT in order to update existing nodes' finger tables.\
**Input**: void\
**Returns**: Status, msg

# Data Structures
## App State (enums)
`Status`: `SUCCESS`, `ERROR`  
`JoinStatus`: `NEW`, `ORIGINAL`, `BUSY`, `ERROR`

## NodeDetails
A data structure that represents a node's details.
fields:\  
* `id` 
* `ip`
* `port`

## NodeStructure
`int id` - node id
`int end` - end of range of ids (combined with id you get the full range)
`int predecessorId` - id of predecessor node
`Entry[] entries` - array of all entries on the node
`NodeDetails[] fingerEntries` - the finger table

## FingerTable
fields - an array of NodeDetails
