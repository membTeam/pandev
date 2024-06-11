package com.pandev.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "rootnode", "ordernum" }) })
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
