package node;

import pa2.Entry;

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

    public List<Entry> getList() {
        List<Entry> list = new ArrayList<Entry>();
        for (int i = 0; i < size; i++) {
            if (cache[i] == null) {
                return list;
            }
            list.add(cache[i]);
        }
        return list;
    }

    public List<Finger> getFingers() {
        List<Finger> list = new ArrayList<Finger>();
        for (int i = 0; i < fingers.size; i++) {
            list.add(fingers[i]);
        }
        return list;
    }
}
