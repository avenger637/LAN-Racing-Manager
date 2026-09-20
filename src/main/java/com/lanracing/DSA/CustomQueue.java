package com.lanracing.DSA;

import java.util.LinkedList;

public class CustomQueue<T> {
    private final LinkedList<T> list = new LinkedList<>();

    public void enqueue(T item) {
        list.addLast(item);
    }

    public T dequeue() {
        return list.isEmpty() ? null : list.removeFirst();
    }

    public T peek() {
        return list.peekFirst();
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
