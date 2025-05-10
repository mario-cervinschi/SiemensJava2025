package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll_shouldReturnAllItems() {
        List<Item> items = Arrays.asList(new Item(1L, "Test", "Nothing", "NEW", "test@gmail.com"), new Item(2L, "TestItem 2", "Nothing 2", "NEW", "test@yahoo.com"));
        when(itemRepository.findAll()).thenReturn(items);

        List<Item> result = itemService.findAll();

        assertEquals(2, result.size());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnItem() {
        Item item = new Item(1L, "Test", "Nothing", "NEW", "test@gmail.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Optional<Item> result = itemService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getName());
    }

    @Test
    void save_shouldReturnSavedItem() {
        Item item = new Item(1L, "Test", "Nothing", "NEW", "test@gmail.com");
        when(itemRepository.save(item)).thenReturn(item);

        Item saved = itemService.save(item);

        assertEquals("Test", saved.getName());
        verify(itemRepository).save(item);
    }

    @Test
    void delete_shouldCallRepository() {
        itemService.deleteById(1L);
        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void processItemsAsync_shouldProcessAndReturnItems() throws Exception {
        List<Long> ids = Arrays.asList(1L, 2L);
        Item item1 = new Item(1L, "Test", "Nothing", "NEW", "test@gmail.com");
        Item item2 = new Item(2L, "TestItem 2", "Nothing 2", "NEW", "test@yahoo.com");

        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> result = future.get();

        assertEquals(2, result.size());
        assertEquals("PROCESSED", result.get(0).getStatus());
        assertEquals("PROCESSED", result.get(1).getStatus());
    }
}
