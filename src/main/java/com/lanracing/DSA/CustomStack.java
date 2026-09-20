package com.lanracing.DSA;

import java.util.ArrayDeque;

public class CustomStack<T> {
    private final ArrayDeque<T> stack = new ArrayDeque<>();

    public void push(T item) {
        stack.push(item);
    }

    public T pop() {
        return stack.poll();
    }

    public T peek() {
        return stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }
}
