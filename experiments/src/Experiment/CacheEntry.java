package Experiment;

public class CacheEntry {

    private String word;
    private String def;

    public CacheEntry(String word, String def) {
        this.word = word;
        this.def = def;
    }
    public String getDef() {
        return this.def;
    }

    public String getEntry() {
        return this.word + ": " + this.def;
    }

    public String getWord() {
        return this.word;
    }

}