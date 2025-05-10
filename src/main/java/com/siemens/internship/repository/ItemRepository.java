package com.siemens.internship.repository;

import com.siemens.internship.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for managing Items.
 * Extends JpaRepository to provide basic CRUD operations.
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Gets all Item IDs from the database in a list.
     * @return a list containing the IDs from all the items in the database.
     */
    @Query("SELECT id FROM Item")
    List<Long> findAllIds();
}
