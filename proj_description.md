# Distributed Dictionary
Created by:\ 
Lucas Kivi - kivix019\
Charles Droege - droeg022

## Overview
A dictionary implemented via a distributed hash table based on the Chord protocol. Clients can look up and add words.

## Relations

	Client	        Server
	Node   contacts SuperNode

	Client		Server
	Client contacts SuperNode

	Client		Server
	Client contacts Node

## Description
### Node setup
- node contacts supernode to join: Join()
- supernode will give the node an id and information of a previous DHT node
	- format for information given to new node: [NewNodeID,OldNodeIP:OldNodePort:OldNodeID]
	- node information can be represented as a string in the [IP:Port:ID] format
- new node should connect to this node
- node will find its successor and predecessor and build its fingertable
- node will distribute an update notice to predecessors to update their own finger tables
- node should let supernode know when finished

- nodes can be identified by (ip,port) pairs to ensure no collisions occur
- each node should be able to print information in the following format e.g ---> 
										- Node ID - range of keys - predecessor - number of words stored
										Words List
										Finger Table


### Client Request
- client connects to supernode with request
- supernode takes request and returns node information to the client
- client contacts the node with their get() or put() request
- node needs to hash the key and then store the word and meaning if responsible
- otherwise look the hash value up in the finger table and forward the request to a new node recursively
- routing is done synchronously until handled
	- need to track the visited nodes


## Function Descriptions

SuperNode function calls:

	GetNodeForJoin(IP,Port): 
		- Node contacts SuperNode(using thrift) to join
		- SuperNode returns one random node's info
			 - If SuperNode is busy with another node, returns NACK
		- Joining Node uses the node's info to build DHT table

	PostJoin():
		- Node notifies SuperNode it's finished joining
		- SuperNode can now unblock and allow a new node to request to join

	GetNodeForClient():
		- Client send request to SuperNode
		- SuperNode returns information of a random node
		- Client can only contact SuperNode once when running (can contact for each request when testing)

Node function calls:

	Put(word, meaning):
		- Node will store information if it's the correct node, otherwise will recursively route the Put() request

	Get(word):
		- Node will check if its the proper node and return the meaning
		- Otherwise will properly forward the Get() request

	UpdateDHT():
		- New node contacts existing nodes so they can update their own finger tables
		- Calls PostJoin() once finished to allow SuperNode to get new node requests




## Extra Notes and Reminders

- Caching can be used. When a put request is submitted, cache the word/meaning pair with each node recursively accessed during the Put() call
	- caching part seems like it undermines the whole DHT system and it's not listed in the grading so we'll need more clarification
- More than 2 thrift files will be needed, I think 3 will be
- Word and its Meaning should be updated if client calls Put() on an existing word
- Number of DHT nodes passed as parameter when SuperNode is created
- Don't need to worry about node failures
- Node returns Error message to client if Get() is called on a word that isn't stored
- Nodes should be multithreaded servers since multiple clients can contact the node at once

	


