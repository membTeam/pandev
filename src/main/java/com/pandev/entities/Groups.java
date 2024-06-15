package com.pandev.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        indexes = {@Index(columnList = "txtgroup") },
        uniqueConstraints = {
            @UniqueConstraint(columnNames = { "rootnode", "ordernum" }),
            @UniqueConstraint(columnNames = { "parentnode", "txtgroup"} )})
public class Groups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    private Integer rootnode;
    @Setter
    private Integer parentnode;

    @Column(columnDefinition = "varchar(150)")
    private String txtgroup;

    @Setter
    private int ordernum;
    private int levelnum;
}
