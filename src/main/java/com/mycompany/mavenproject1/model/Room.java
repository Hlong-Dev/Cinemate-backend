package com.mycompany.mavenproject1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor  // Constructor không tham số
@AllArgsConstructor // Constructor có tất cả các tham số
@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String currentVideoUrl;
    private String currentVideoTitle;
    @Column(name = "name")
    private String name;

    private String thumbnail;
    private String ownerUsername;
}
