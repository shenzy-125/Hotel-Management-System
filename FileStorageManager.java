package com.raahul.hms.service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic file-based storage manager that replaces in-memory ArrayList storage.
 * Persists data to .dat files using Java serialization.
 * All operations are thread-safe.
 *
 * @param <T> The type of object to store (must be Serializable)
 */
public class FileStorageManager<T extends Serializable> {

    private final String filePath;

    public FileStorageManager(String filePath) {
        this.filePath = filePath;
        ensureDirectoryExists();
    }

    /**
     * Ensures the parent directory for the data file exists.
     */
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

    /**
     * Saves the entire list to the file, overwriting previous content.
     */
    @SuppressWarnings("unchecked")
    public synchronized void saveAll(List<T> data) {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(filePath))) {
            out.writeObject(new ArrayList<>(data));
        } catch (IOException e) {
            System.err.println("Error saving data to " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Loads all items from the file. Returns an empty list if the file
     * doesn't exist or cannot be read.
     */
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

    /**
     * Adds a single item to the stored list.
     */
    public synchronized void add(T item) {
        List<T> data = loadAll();
        data.add(item);
        saveAll(data);
    }

    /**
     * Removes an item from the stored list (by equals()).
     * @return true if the item was found and removed
     */
    public synchronized boolean remove(T item) {
        List<T> data = loadAll();
        boolean removed = data.remove(item);
        if (removed) {
            saveAll(data);
        }
        return removed;
    }

    /**
     * Updates an item in the list. Finds the old item using equals()
     * and replaces it with the updated version.
     * @return true if the item was found and updated
     */
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

    /**
     * Returns the number of stored items.
     */
    public synchronized int size() {
        return loadAll().size();
    }

    /**
     * Clears all stored data.
     */
    public synchronized void clear() {
        saveAll(new ArrayList<>());
    }
}
