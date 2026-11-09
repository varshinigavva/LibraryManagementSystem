package com.library.storage;

import java.io.IOException;

/**
 * Interface defining operations for loading and saving library data.
 */
public interface StorageService {
    /**
     * Loads library data from persistent storage.
     * If storage file does not exist, returns freshly generated initial/sample data.
     */
    LibraryData loadData() throws IOException, ClassNotFoundException;

    /**
     * Saves library data to persistent storage.
     */
    void saveData(LibraryData data) throws IOException;

    /**
     * Checks if persistent storage file already exists.
     */
    boolean hasExistingData();

    /**
     * Generates a fresh set of sample demonstration records.
     */
    LibraryData createSampleData();
}

