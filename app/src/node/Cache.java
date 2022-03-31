package node;

import pa2.Entry;
import java.util.ArrayList;
import pa2.NodeDetails;
import pa2.Entry;
import pa2.Finger;


public class Cache {
    private Entry[] cache;
    private int size;
    private int pointer;

    public Cache(int size) {
        this.cache = new Entry[size];
        this.size = size;
        this.pointer = 0;
        for (int i = 0; i < size; i++) {
            cache[i] = new Entry();
        }
    }


    public int getSize() {
        return this.size;
    }

    public int getPointer() {
        return this.pointer;
    }

    public void addEntry(Entry entry) {
        for (int i = 0; i < size; i++) {
            String entryWord = cache[i].word;
            if (entryWord != null) {
                if (entryWord.equals(entry.word)) { // entry already in cache
                    cache[i] = entry;
                    return;
                }
            }
        }
        cache[pointer] = entry;
        pointer += 1;
        if (pointer >= size) {
            pointer = 0;
        }
    }

    public Entry checkCache(String word) {
        for (int i = 0; i < size; i++) {
            String entryWord = cache[i].word;
            if (entryWord != null) {
                if (entryWord.equals(word)) {
                    return cache[i];
                }
            }
        }
        return null;
    }

    public ArrayList<Entry> getList() {
        ArrayList<Entry> list = new ArrayList<Entry>();
        for (int i = 0; i < size; i++) {
            if (cache[i] == null) {
                return list;
            }
            list.add(cache[i]);
        }
        return list;
    }
}
