package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service handling the logic for our Items.
 * Has standard CRUD operations and a way to process the Items asynchronously.
 */
@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    // Thread pool for executing asynchronous tasks
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * Gets all items from the database.
     *
     * @return a list containing all the existing items.
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Retrieves the item from the database with a certain ID.
     *
     * @param id the id of the Item.
     * @return Optional containing the item if found.
     */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Saves an Item to the database.
     *
     * @param item The item to be added
     */
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Deletes an Item from the database.
     *
     * @param id The ID of the item to be deleted.
     */
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * The initial method started the async process, but it did not wait
     * for them. The processed items returned before anything finished.
     *
     * {@processedItems} and {@processedCount} were modified from multiple
     * threads without synchronization.
     * -------------------------------------------------
     * The new method returns a CompletableFuture which lets the caller wait
     * for all the processing to finish to get the result.
     *
     * allOf() -> waits for all async tasks.
     *
     * No shared variables between the threads.
     *
     * Custom executor used.
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        // Get all item IDs from the database
        List<Long> itemIds = itemRepository.findAllIds();

        // I'll create a list of CompletableFutures to hold them
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        // Loop over each ID and process it asynchronously
        for (Long id : itemIds) {
            // For each ID, I will create an async task to get, process, and save the item
            CompletableFuture<Item> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // Simulate processing time
                    Thread.sleep(100);

                    // Get item from database
                    Item item = itemRepository.findById(id).orElse(null);
                    if (item == null) {
                        return null; // Item not found
                    }

                    // Mark it as processed
                    item.setStatus("PROCESSED");

                    // Save and return updated item
                    return itemRepository.save(item);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Properly handle thread interruption
                    throw new RuntimeException("Thread was interrupted", e);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to process item with ID: " + id, e);
                }
            }, executor); // Submit the task to the thread pool

            // Add this future to the list
            futures.add(future);
        }

        // Combine all individual futures into one using allOf
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // When all are done, I collect the results
        return allDoneFuture.thenApply(v -> {
            List<Item> results = new ArrayList<>();
            for (CompletableFuture<Item> future : futures) {
                Item item = future.join(); // Waits for this task to complete and gets the result
                if (item != null) {
                    results.add(item); // Add to final list if not null
                }
            }
            return results;
        });
    }

}

