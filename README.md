# Distributed Dictionary
Created by: \
Lucas Kivi - kivix019 \
Charles Droege - droeg022

## Description
A dictionary implemented via a distributed hash table based on the Chord protocol. Clients can look up and add words. Words are stored on nodes. There is a SuperNode that is responsible for onboarding new nodes and introducing clients to an ambassador node. The ambassador node is used to communicate with the DHT.

# Setup
1. Download a version of this application. If you need a fresh version it is always available via git.
```
git clone https://github.com/lukekivi/Distributed-Dictionary.git
```

2. Set **required** environment variable.
```
export THRIFT_LIB_PATH="<path-to-thrift-libs>"
```

3. Set **optional** environment variables. These are required if you want to use our scripts for automated setup of DHTs and testing. This only needs to be done on the machine  starting these DHT and testing scripts.
```
export DHT_USER_NAME="<username>"
export DHT_APP_PATH="<path-to-distributed-dictionary-app-directory>"
```
**Example environment variables script**
```
export THRIFT_LIB_PATH="/project/kivix019/thrift-0.15.0/lib/java/build/libs"
export DHT_USER_NAME="kivix019"
export DHT_APP_PATH="../../project/kivix019/Distributed-Dictionary/app"
```
4. Be sure to have a current JDK and JRE downloaded in order to compile and run Java applications.

# Commands
Within the project there is a `commands.txt` file located within `Distributed-Dictionary/app`. It contains commands that the `Client` can process. Here you may enter commands you want to give to the system.

## Command Syntax
* One command takes up one line.
* Each command starts with the operation name. 
* Each command has a required amount of arguments. This amount can be 0.
* Command arguments must be delimited by ` :: `. This is two colons surrounded by white space.
---
### Put
---
**Format**
```
put :: word :: definition
```
* `put` - Put a word and definition pair into the DHT.
* `word` - The word to be entered into the DHT.
* `definition` - A definition of that word. There should be no new lines in a definition. If the delimiter ` :: ` appears in the definition whatever comes after the first delimiter will not be recorded. 

**Example**
```
put :: amok :: in a violently raging, wild, or uncontrolled manner
```

---

### Get
---
**Format**
```
get :: word
```
* `get` - Get a definition from the DHT that corresponds to `word`.
* `word` - The word you want to see the definition for. 

**Example**
```
get :: amok 
```

---
### Print
---
**Format**
```
print
```
* `print` - Print the structure of the DHT. The print out includes node specific details including id, predecessor id, finger table, and word/definition pairs.

---
### Test
---
**Format**
```
test
```
* `test` - Test the structure of the DHT. This test assures proper node structure and entry placement.

---
### Kill
---
**Format**
```
kill
```
* `kill` - Kill the entire DHT.


# Config
There is `config.txt` file located at `Distributed-Dictionary/app`. This file is used to configure app state. \
**Example Contents**
```
SuperNode csel-kh4250-19.cselabs.umn.edu 8900
Node 8900 10
M 4
```
**SuperNode**
```
SuperNode machine_address port_number
```
* `SuperNode` - Identifies that this is the line that contains SuperNode's details.
* `machine_address` - The fully qualified address to the target machine.
* `port_number` - The port number you want the SuperNode to use. 

**Node**
```
Node port_number cache_size
```
* `Node` - Identifies that this is the line that contains Node details.
* `port_number` - The port number you want all nodes to run on.
* `cache_size` - The cache size for your nodes.

**M**
```
M m_size
```
* `M` - Identifies that this is the line that contains the size of M.
* `m_size` - The desired size of M. 2^M is the amount of nodes you will be able to enter into the system.

## Running
Each system entity must be run on a separate machine. This is very important to understand.

1. You must first run a `SuperNode`
2. Run as many `Node`s as you want. Number of nodes must be at least one and no more than 2^M. Read more about M in the **Config** section.
3. Run client as many times as you want. Must have completed step 2 before this step.
### Running a SuperNode
1. Follow the **Setup** section steps above.
2. Navigate to `Distributed-Dictionary/app`.
3. Be sure that the `config.txt` file is configured to have the machine running the SuperNode as the SuperNode address field. 
3. Run a `SuperNode`
```
ant superNode
```
### Running a Node
1. Follow the **Setup** steps above.
2. Navigate to `Distributed-Dictionary/app`. 
3. Run a `Node`.
```
ant node
```

### Running a Client
1. Follow the **Setup** steps above.
2. Navigate to `Distributed-Dictionary/app`. 
3. Setup `commands.txt` file. Read more about this in **Commands** section.
3. Run a `client`.
```
ant client
```
## Running The Easy Way
1. Navigate to the `Distributed-Dictionary/DHTSetupScripts` directory. 
2. Be sure you followed **Setup** section steps, including the optional step.
3. Choose the DHT size you would like to make.
4. Open that directory.
5. Check the file contents of the `ssh_commands_<#>_nodes.sh` file and be sure you have access to those machines. They will be pointing to UMN machines by default.
6. Be sure the machine used for the `ant superNode` command in this file matches the `config.txt` file SuperNode address entry. More details about how to do this in the **Config** section.
7. If you change the machines be sure to change the machines in the `ssh_cleanup_<#>_nodes.sh` file to match.
8. Source (run) the `ssh_commands_<#>_nodes.sh`.
9. If everything is running correctly you may then go follow the **Runnning a Client** section steps in order to run a client locally. 
10. You can run a client as many times as you want and modify your `command.txt` file between each run. See details about the `commands.txt` file in the **Commands** section.
11. Once you are done it may behoove you too run the `ssh_cleanup_<#>_nodes.sh` script as well. This will make subsequent runs easier.

### Test 1 - Standard
Number of Nodes: 5
M-bit value: 4
Key/Node Value Range: 0-15
Test `Get()` and `Put()`, 16 of each. All words put in are unique and all words grabbed are already entered. Expect the node log files to describe its process upon receiving `Get()` and `Put()` requests as well as how they deal with them (Caching, Forwarding, and Dictionary). Expect the client log to have success messages for put()s and get()s.

### Test 2 - Put() an entry in twice
Number of Nodes: 5
M-bit value: 4
Key/Node Value Range: 0-15
Test `Get()` and `Put()`, 10 puts and 9 gets. Mouse is entered twice with a different definition. All of the gets are for words that exist in the dictionary. Expect the node log files to describe its process upon receiving `Get()` and `Put()` requests as well as how they deal with them (Caching, Forwarding, and Dictionary). In the client log, the `Get(mouse)` call should return it's updated definition describing a computer mouse. It is however possible that the DHT gets the description from a cache that hasn't been updated with the new meaning.

### Test 3 - Get() an absent entry
Number of Nodes: 5
M-bit value: 4
Key/Node Value Range: 0-15
Test `Get()` and `Put()`, 9 puts and 10 gets. All of the puts are unique while theres a get at the end that's for 'glasses' which doesn't appear in the DHT. Expect the node log files to describe its process upon receiving `Get()` and `Put()` requests as well as how they deal with them (Caching, Forwarding, and Dictionary). For the client log, the Get() request for 'glasses' will respond with a failure message saying that the word isn't in the dictionary.

### Test 4 - Standard but with 1 Node
Number of Nodes: 1
M-bit value: 4
Key/Node Value Range: 0-15

Test `Get()` and `Put()`, 9 of each. All words put in are unique and all words grabbed are already entered. Expect the node log files to describe adding everything to its dictionary instead of caching the values since it's the correct node. Also there will be no forwarding since there's only one node. Expect the client log to have success messages for put()s and get()s.


# Experiments
This was our local DHT implementation we used for proof of concept. We left in the project because it was a lot of work. We will not document the sub-app because we have sunk so much time into this project and the documentation would be the last straw.



