package org.example.task1_1;

import java.util.LinkedList;
import java.util.Objects;


public class MyHashMapImpl<K, V> implements MyHashMap<K, V> {

    private static class Entry<K, V> {
        final K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }


        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry<?, ?> entry = (Entry<?, ?>) o;
            return Objects.equals(key, entry.key);
        }


        @Override
        public final int hashCode() {
            return Objects.hashCode(key);
        }
    }

    private final LinkedList<Entry<K, V>>[] table;
    private static final int DEFAULT_CAPACITY = 16;
    private int size;


    public MyHashMapImpl() {
        this(DEFAULT_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public MyHashMapImpl(int capacity) {
        this.table = new LinkedList[capacity];
        this.size = 0;
    }


    private int indexFor(K key) {
        if (key == null) {
            return 0;
        }

        return Math.abs(key.hashCode() % table.length);
    }

    @Override
    public V put(K key, V value) {
        int index = indexFor(key);

        if (table[index] == null) {
            table[index] = new LinkedList<>();
        }

        LinkedList<Entry<K, V>> bucket = table[index];

        for (Entry<K, V> entry : bucket) {
            if (Objects.equals(entry.key, key)) {
                V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
        }

        bucket.add(new Entry<>(key, value));
        size++;
        return null;
    }

    @Override
    public V get(K key) {
        int index = indexFor(key);
        LinkedList<Entry<K, V>> bucket = table[index];

        if (bucket == null) {
            return null;
        }

        for (Entry<K, V> entry : bucket) {
            if (Objects.equals(entry.key, key)) {
                return entry.value;
            }
        }

        return null;
    }
    @Override
    public V remove(K key) {
        int index = indexFor(key);
        LinkedList<Entry<K, V>> bucket = table[index];

        if (bucket == null) {
            return null;
        }
        Entry<K, V> foundEntry = null;
        for (Entry<K, V> entry : bucket) {
            if (Objects.equals(entry.key, key)) {
                foundEntry = entry;
                break;
            }
        }

        if (foundEntry != null) {
            bucket.remove(foundEntry);
            size--;
            return foundEntry.value;
        }

        return null;
    }


    @Override
    public void print() {
        System.out.println("MyHashMap (Size: " + size + ")");
        boolean isEmpty = true;
        for (int i = 0; i < table.length; i++) {
            LinkedList<Entry<K, V>> bucket = table[i];
            if (bucket != null && !bucket.isEmpty()) {
                isEmpty = false;
                System.out.print("Bucket[" + i + "]: ");
                // Печать всех элементов в бакете
                System.out.println(String.join(" -> ", bucket.stream().map(Entry::toString).toList()));
            }
        }
        if (isEmpty) {
            System.out.println("Map is empty.");
        }
        System.out.println("------------------------------------");
    }
    public int size() {
        return size;
    }
}