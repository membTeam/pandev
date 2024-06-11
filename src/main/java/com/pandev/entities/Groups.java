package com.pandev.entities;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(uniqueConstraints = @UniqueConstraint(columnNames={"order"}))
public class Groups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer rootnode;
    private Integer parentnode;

    @Column(columnDefinition = "varchar(150)")
    private String txtgroup;

    private int ordernum;
    private int levelnum;
}
