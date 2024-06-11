package com.pandev.entities;


import jakarta.persistence.*;
import lombok.Getter;

import javax.annotation.processing.Generated;

@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"order"}))
public class Pandev {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer root;
    private Integer parent;

    @Column(columnDefinition = "varchar(150)")
    String txt;

    int order;
    int level;
}
