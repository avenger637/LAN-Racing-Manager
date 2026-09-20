package com.lanracing.DSA;

import java.util.Comparator;
import java.util.PriorityQueue;

public class CustomPriorityQueue<T> {
    private final PriorityQueue<T> priorityQueue;

    public CustomPriorityQueue(Comparator<T> comparator) {
        this.priorityQueue = new PriorityQueue<>(comparator);
    }

    public void enqueue(T value) {
        priorityQueue.offer(value);
    }

    public T dequeue() {
        return priorityQueue.poll();
    }

    public T peek() {
        return priorityQueue.peek();
    }

    public boolean isEmpty() {
        return priorityQueue.isEmpty();
    }
}
