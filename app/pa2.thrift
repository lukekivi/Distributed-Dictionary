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
    NEW = 1,
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