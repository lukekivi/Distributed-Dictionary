# Word Storage
HashMap (word, defintion)

# App State
**Status**: success, error  
**JoinStatus**: newtotheparty, original, busy, error

# NodeDetails
fields - id, ip, port

# FingerTable
fields - an array of NodeDetails

# Client
## Operations
### Get()
Calls Node get function.
### Put()
Calls Node put function.
### StartClient
Calls SuperNode GetNodeForClient function.

# Supernode
## Interface Operations
### GetNodeForClient()
Get a node for the client to communicate with.
**Input**: void\
**Returns**: status, ip, port, id\
**Logic**: chose nodes 

###  GetNodeForJoin()
Sign up a node for the DHT system. Can be busy, try again later. Block new calls to GetNodeForJoing() until PostJoin() is received.\
**Input**: ip, port - need node specific ip and port in SuperNode so it can be distributed to clients when they get call GetNodeForClient()\
**Returns**: joinStatus, id, nodedetails, msg - the join status, an assigned id for requesting node, nodedetails for an old node for the requesting node to talk to, a message.

### PostJoin()
Alert SuperNode that a Node has finished joining the DHT.
**Input**: void\
**Returns**: status

# Node
Each node maintains a hashmap of words it knows and a fingertable in order to communicate with other nodes.

There will be some type of caching implemented, likely a hashmap of indefinite size. 

## Operations

### StartNode()
Calls SuperNode function GetNodeForJoin(). Decide how to move forward based on JoinStatus. If NewToTheParty it uses old node details to establish itself and then alerts the SuperNode of its completion. If busy, user can try again later.

### FindWord()
Encapsulate process of querying for word in the DHT.Check cache map, if word is not there then look in other nodes.

### HashFunction()
Do the hashing.

### PlaceWord()
Encapsulate the process of placing a word in the DHT

## Interface Operations
### Get()
Compare hashed value. If the word belongs to this node, get it. Else check the cache, if not there pass along to a better node. Terminates search if it was supposed to be in this node and it wasn't. If node called Get() itself because it did not have the word, cache found value on return.\
**Input**: word - the word user wants a definition for.\
**Returns**: status, definition, msg - status represents the transaction conclusion and the msg either contains a definition or some error/failure message.

### Put()
Compare hashed value. If the word belongs to this node, stash it. Else, cache it and send it along to a better node. Wait for better node's response and return that back to sender.\
**Input**: word, meaning - the word user wants a definition for.\
**Returns**: status, msg - status represents the transaction conclusion and the msg is a description of said conclusion

### GetFingerTable()
**Input**: void\
**Returns**: something to setup new node's fingertable.

### UpdateDHT()
Called when a new node is entered into the DHT in order to update existing nodes' finger tables.\
**Input**: void\
**Returns**: 