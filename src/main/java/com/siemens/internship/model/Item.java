package com.siemens.internship.model;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    /**
     * Represents the ID of the Item
     * - it auto generates
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The name of our Item - cannot be blank/missing
     */
    @NotBlank(message = "Name is required.")
    private String name;

    /**
     * The description of our Item - cannot be blank/missing
     */
    @NotBlank(message = "At least a small description is required.")
    private String description;

    /**
     * The status of our Item - can be either NEW, PROCESSED, FINISHED
     */
    @Pattern(regexp = "NEW|PROCESSED|FINISHED", message = "Status must be NEW, PROCESSED or FINISHED")
    @NotBlank(message = "Status required.")
    private String status;

    /**
     * The email of our Item - has to be a valid one!
     */
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email format not valid")
    @NotBlank(message = "Blank email.")
    private String email;
}