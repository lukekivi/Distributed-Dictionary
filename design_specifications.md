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

#### ConnectToDHT()
Establish a connection with the DHT. It needs to read the SuperNode details from the `config.txt` file via the `ReadIn` class. It then establishes a connection with the SuperNode and asks it for an ambassador node in order to communicate with the DHT continuously.

#### DoCommands()
ReadIn a commands from the `commands.txt` file and complete all commands. This can be get, put, test, print, and kill operations.

#### Close()
Close connection to the ambassador node.



## Supernode
### Interface Operations
#### GetNodeForClient()
Get a random node for the client to communicate with.
**Input**: void\
**Returns**: Status, NodeDetails, msg\

####  GetNodeForJoin()
Sign up a node for the DHT system. If **NEW**, pass the requesting its *successor* so it can establish itself. Can be **BUSY**, which means try again later. The SuperNode blocks new calls to `GetNodeForJoin()` until `PostJoin()` is received.\
**Input**: void\
**Returns**: JoinStatus, id, NodeDetails, msg - the join status, an assigned id for requesting node, nodedetails for an old node for the requesting node to talk to, a message.

#### PostJoin()
Alert SuperNode that a Node has finished joining the DHT. Also, track this node now that is successfully added to the system.
**Input**: ip, port - need node specific ip and port in SuperNode so it can be distributed to entities that want to contact it.\
**Returns**: Status, msg

#### GetDHTStructure()
Gets a list of `NodeStructure` objects. 
**Input**: void\
**Returns**: Status, msg, NodeStructure list

#### KillDHT()
Send out kill messages to every node in the DHT then kill self. This is a `oneway` thrift function.
**Input**: void\
**Returns**: n/a







## Node
Each node maintains a hashmap of words it knows, a fingertable in order to communicate with other nodes, and a cache (user specifies size when creating the node). Each node also maintains `NodeDetails` objects that represents its predecesser and itself.

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

### insertWord()
Called when current node is the proper one. Puts word and def into dict.

### findPredCaching()
Called when current node is not proper one, word and def get added into cache as an Entry.

#### PutWord()
Encapsulate the process of placing a word in the DHT
**Input**: word, description, key
**Return**: String

### Join()
Handles the node when it joins. Will initialize the finger table and call updateOthers.

### InitFingerTable
Called to initialize the finger table. This Creates connections to other nodes to grab values for its finger table as well as update the other node's finger tables.

### updateOthers()
Called upon entering the DHT, calls updateFingerTable() on other nodes to update their tables since this node is now joining the DHT.

### FindPredecessor()
Finds predecessor of the index that's passed in as an input.

### ClosestPrecedingFinger()
Finds the closest node that precedes the given index thats passed as an input, returns the closest one.

### InitFinger()
Used to initialize a finger table entry.

### isResponsible
Used to check if the node is responsible to add a word and its def to its dictionary. Just a boolean check statement.

### getNodeEntries()
Returns all of the dictionary entries as an ArrayList.

### getNodeFingers()
returns all of the fingertable entries as an ArrayList.

### getHash()
Takes a word as an input and hashes it, returns the resulting id.

### setSucc()
sets the successor, which is fingers[0].succ, to the input NodeDetails.

### setFingerSucc()
Sets the successor of a fingertable entry.


### Interface Operations
#### Get()
Compare hashed value. If the word belongs to this node, get it. Else check the cache, if not there, pass along to a better node in the finger table. Returns **ERROR** if it was supposed to be in this node and it wasn't. If node called `Get()` itself because it did not have the word.\
**Input**: word - the word user wants a definition for.\
**Returns**: Status, definition, msg - status represents the transaction conclusion and the msg either contains a definition or some error/failure message.

#### Put()
Compare hashed value. If the word belongs to this node, stash it. Else, cache it and send it along to a better node using the finger table. Wait for better node's response and return that back to sender.\
**Input**: word, definition - the word user wants a definition for.\
**Returns**: Status, msg - status represents the transaction conclusion and the msg is a description of said conclusion

#### GetNodeStructure()
Get a `NodeStructure` object that represents the state of a node.
**Input**: void\
**Returns**: NodeStructure, Status, msg

### GetSucc()
Called to get successor of the node.
**Input**: void
**Returns**: NodeDetails

### SetSucc()
Called to set the successor of the node
**Input**: NodeDetails
**Returns**: StatusData

### GetPred()
Called to get node's predecessor.
**Input**: void
**Returns**: NodeDetails

### SetPred()
Called to set the predecessor of the node
**Input**: NodeDetails
**Returns**: StatusData

### FindSuccessor()
Called to find the successor of the id given
**Input**: Int
**Returns**: NodeDetails

### ClosestPrecedingFinger()
Called to get the closest preceding finger to the given id
**Input**: Int
**Returns**: NodeDetails


#### UpdateFingerTable()
Called when a new node is entered into the DHT in order to update existing nodes' finger tables.\
**Input**: NodeDetails, index\
**Returns**: Status, msg

#### Kill()
Kill self. This is a `oneway` thrift function.
**Input**: void\
**Returns**: n/a


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
`int predId` - id of predecessor node
`Entry[] entries` - array of all entries on the node
`Finger[] fingers` - the finger table

## Finger
fields - start, end, successor (`NodeDetails`)

## FingerTable
fields - an array of `Finger`s

## Cache
fields - an array of `CacheEntry`s, size of cache, pointer to spot in cache for FIFO

## CacheEntry
fields - word, def
