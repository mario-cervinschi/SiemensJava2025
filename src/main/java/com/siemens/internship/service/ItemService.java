package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
//    @Async
//    public List<Item> processItemsAsync() {
//
//        List<Long> itemIds = itemRepository.findAllIds();
//
//        for (Long id : itemIds) {
//            CompletableFuture.runAsync(() -> {
//                try {
//                    Thread.sleep(100);
//
//                    Item item = itemRepository.findById(id).orElse(null);
//                    if (item == null) {
//                        return;
//                    }
//
//                    processedCount++;
//
//                    item.setStatus("PROCESSED");
//                    itemRepository.save(item);
//                    processedItems.add(item);
//
//                } catch (InterruptedException e) {
//                    System.out.println("Error: " + e.getMessage());
//                }
//            }, executor);
//        }
//
//        return processedItems;
//    }

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(100);
                        Item item = itemRepository.findById(id).orElse(null);
                        if (item == null) return null;

                        item.setStatus("PROCESSED");
                        return itemRepository.save(item);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Proper interrupt handling
                        throw new RuntimeException("Thread interrupted", e);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to process item ID: " + id, e);
                    }
                }, executor))
                .collect(Collectors.toList());

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        futures.stream()
                                .map(CompletableFuture::join)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
    }

}

