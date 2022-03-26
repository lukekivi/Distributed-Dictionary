package Experiment;

public class Cache {
    private CacheEntry[] cache;
    private int size;
    private int pointer;

    public Cache(int size) {
        this.cache = new CacheEntry[size];
        this.size = size;
        this.pointer = 0;
        for (int i = 0; i < size; i++) {
            cache[i] = new CacheEntry(null, null);
        }
    }

    public void getCache() {
        for (int i = 0; i < size; i++) {
            System.out.println(cache[i].getEntry());
        }
    }

    public int getSize() {
        return this.size;
    }

    public int getPointer() {
        return this.pointer;
    }

    public void addEntry(CacheEntry entry) {
        cache[pointer] = entry;
        pointer += 1;
        if (pointer >= size) {
            pointer = 0;
        }
    }

    public CacheEntry checkCache(String word) {
        for (int i = 0; i < size; i++) {
            String entryWord = cache[i].getWord();
            if (entryWord != null) {
                if (entryWord.equals(word)) {
                    return cache[i];
                }
            }
        }
        return null;
    }
}
