package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllItems_shouldReturnItems() throws Exception {
        List<Item> items = Arrays.asList(new Item(1L, "Test", "Nothing", "NEW", "test@gmail.com"), new Item(2L, "TestItem 2", "Nothing 2", "NEW", "test@yahoo.com"));
        Mockito.when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        Item item = new Item(1L, "Test", "Nothing", "NEW", "test@gmail.com");
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    void getItemById_shouldReturnNothing() throws Exception {
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        Item item = new Item(null, "Test", "Nothing", "NEW", "test@gmail.com");
        Item savedItem = new Item(1L, "Test", "Nothing", "NEW", "test@gmail.com");

        Mockito.when(itemService.save(any())).thenReturn(savedItem);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createInvalidItem_shouldReturnBadRequest() throws Exception {
        Item invalidItem = new Item(null, "Test", "Nothing", "NEW", "test@gm@ail.com");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        Item updatedItem = new Item(1L, "Test2", "Nothing", "NEW", "test@gmail.com");
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(updatedItem));
        Mockito.when(itemService.save(any())).thenReturn(updatedItem);

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test2"));
    }

    @Test
    void deleteItem_shouldReturnNoContent() throws Exception {
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(new Item(1L, "Test", "Nothing", "NEW", "test@gmail.com")));

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void processItems_shouldReturnProcessedList() throws Exception {
        List<Item> processed = Arrays.asList(new Item(1L, "Test", "Nothing", "PROCESSED", "test@gmail.com"));

        CompletableFuture<List<Item>> future = CompletableFuture.completedFuture(processed);
        Mockito.when(itemService.processItemsAsync()).thenReturn(future);

        MvcResult result = mockMvc.perform(get("/api/items/process")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].status").value("PROCESSED"));
    }
}