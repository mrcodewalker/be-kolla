package com.example.kolla.repositories;

import com.example.kolla.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    boolean existsByRoomName(String name);
    boolean existsByRoomCode(String roomCode);
    Optional<Room> findByRoomCode(String roomCode);
    List<Room> findByIdIn(List<Long> ids);
}