
package com.example.tasks;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private PriorityQueue<Task> queue;

    public TaskManager() {
        queue = new PriorityQueue<>(new TaskComparator());
    }

    public synchronized void add(Task t) {
        queue.offer(t);
    }

    public synchronized boolean remove(Task t) {
        return queue.remove(t);
    }

    public synchronized List<Task> getAllSorted() {
        // Return a sorted list without modifying internal queue
        return queue.stream()
                    .sorted(new TaskComparator())
                    .collect(Collectors.toList());
    }

    public synchronized Optional<Task> peekNext() {
        return Optional.ofNullable(queue.peek());
    }

    public synchronized void saveToFile(File f) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(this);
        }
    }

    public static TaskManager loadFromFile(File f) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object o = ois.readObject();
            if (o instanceof TaskManager) {
                return (TaskManager) o;
            } else {
                throw new IOException("File does not contain a TaskManager");
            }
        }
    }
}
