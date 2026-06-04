package com.raahul.hms.service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorageManager<T extends Serializable> {

    private final String filePath;

    public FileStorageManager(String filePath) {
        this.filePath = filePath;
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        try {
            Path parent = Paths.get(filePath).getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void saveAll(List<T> data) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(filePath))) {
            out.writeObject(new ArrayList<>(data));
        } catch (IOException e) {
            System.err.println("Error saving data to " + filePath + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized List<T> loadAll() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(filePath))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                return (List<T>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data from " + filePath + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public synchronized void add(T item) {
        List<T> data = loadAll();
        data.add(item);
        saveAll(data);
    }

    public synchronized boolean remove(T item) {
        List<T> data = loadAll();
        boolean removed = data.remove(item);
        if (removed) {
            saveAll(data);
        }
        return removed;
    }

    public synchronized boolean update(T oldItem, T newItem) {
        List<T> data = loadAll();
        int index = data.indexOf(oldItem);
        if (index >= 0) {
            data.set(index, newItem);
            saveAll(data);
            return true;
        }
        return false;
    }

    public synchronized int size() {
        return loadAll().size();
    }

    public synchronized void clear() {
        saveAll(new ArrayList<>());
    }
}
