package com.siemens.internship.controller;

import com.siemens.internship.service.ItemService;
import com.siemens.internship.model.Item;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * GET request to show all the Items from our database.
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    /**
     * POST request to add a new item to our database.
     *
     * Initially was returning CREATED status when there was an error.
     * Now return BAD REQUEST status when validation fails and CREATED when it works.
     */
    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.save(item));
    }

    /**
     * GET request to get an item based on the ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT request to update an item based on ID and a new given item.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(null);
        }

        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            return ResponseEntity.ok(itemService.save(item));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * DELETE request to delete an item with a given ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            itemService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET request to process our items and show them on the new page.
     */
    @GetMapping(value = "/process", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {
        return itemService.processItemsAsync()
                .thenApply(items -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(items));
    }
}
