package com.example.kolla.models;

import jakarta.persistence.*;
import lombok.Data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
@Table(name = "room")
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "room_code")
    private String roomCode;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meeting> meetings;
}
