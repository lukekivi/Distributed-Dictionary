package utils;    

import java.math.BigInteger;
import java.security.MessageDigest;    
    
public class HashHelp {
    /**
     * Calls MD5 hash on the word to get its id. Used geeksforgeeks to learn how to use MD5
     * @param String which is the word to be hashed
     * @return the id returned by the hashing function
     */
    public static int hashFunction(String word, int max) {
        int total = 0;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(word.getBytes());
            BigInteger num = new BigInteger(1, messageDigest);
            String stringHash = num.toString(16);
            for (int i = 0; i < stringHash.length(); i++) {
                total += (int) stringHash.charAt(i);
            }
        } catch (Exception e) {
            System.out.println("Caught exception " + e + ", ending now.");
            System.exit(1);
        }
        return (total % max);
    }
}