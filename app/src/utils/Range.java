package utils;

/**
 * This class is used to calculate ranges in a circular manner. You will
 * find functions use in and ex to indicate the inclusiveness over their bounds.
 */
public class Range {
    /**
     * exclusive start, inclusive end. Handles circular ranges.
     */
    public static boolean InRangeExIn(int id, int start, int end) {
        if (end < start) {
            if (id <= end) {
                return true;
            } else if (id > start) {
                return true;
            } else {
                return false;
            }
        } else if (end == start) {
            return true;
        }
        return ((id > start) && (id <= end));
    }


    /**
     * inclusive start, exclusive end. Handles circular ranges.
     */
    public static boolean InRangeInEx(int id, int start, int end) {
        if(end < start) {
            if (id < end || id >= start) {
                return true;
            } else {
                return false;
            }
        } else if (end == start) {
            return false;
        }
        return ((id >= start) && (id < end));
    }

    
    /**
     * exclusive start, exclusive end. Handles circular ranges.
     */
    public static boolean InRangeExEx(int id, int start, int end) {
        if(end < start) {
            if (id < end || id > start) {
                return true;
            } else {
                return false;
            }
        } else if (end == start) {
            return id != start;
        }
        return ((id > start) && (id < end));
    }

    /**
     * Perform circular subtaction on an id.
     * id of 0 - 1 == maxKey
     */
    public static int CircularSubtraction(int id, int val, int maxKey) {
        int result;
        
        result = id - val;

        if (result < 0) {
            result = maxKey + result + 1;
        }
        
        return result;
    }
}