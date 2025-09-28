package org.example.task1_1;

public class MyTestHashMap {
    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMapImpl<>();

        System.out.println("PUT: A=10, B=20, C=30");
        map.put("A", 10);
        map.put("B", 20);
        map.put("C", 30);
        map.print(); //3 элемента

        System.out.println("GET 'B': " + map.get("B")); // 20
        System.out.println("GET 'D': " + map.get("D")); // null

        System.out.println("\nPUT 'A' (Update): 50. Old value: " + map.put("A", 50));
        map.print(); // 'A' теперь 50

        System.out.println("\nREMOVE 'B'. Removed value: " + map.remove("B")); // 20
        System.out.println("REMOVE 'D'. Removed value: " + map.remove("D")); // null

        map.print(); // A=50, C=30

        // Проверка ключа null
        System.out.println("\nPUT null=100");
        map.put(null, 100);
        System.out.println("GET null: " + map.get(null)); // 100
        map.print();
    }
}
