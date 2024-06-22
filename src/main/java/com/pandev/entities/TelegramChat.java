package com.pandev.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Tags;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "telegramchat",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "chatid"}) })
public class TelegramChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "chatid")
    private long chatId;

    @Column(name = "username", columnDefinition = "varchar(150)")
    private String userName;

}
