package org.example.task1_1;

public interface MyHashMap<K, V> {
    V put (K key, V value);
    V get (K key);
    V remove (K key);
    void print();
}
