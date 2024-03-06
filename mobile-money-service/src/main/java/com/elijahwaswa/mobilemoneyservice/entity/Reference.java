package com.elijahwaswa.mobilemoneyservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "references")
public class Reference {

    @Id
    @GeneratedValue
    private long id;

//    @Version
//    private Long version;

    @Column(unique = true)
    private String reference;
}
