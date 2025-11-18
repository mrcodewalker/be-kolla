package com.example.kolla.config;

import com.example.kolla.dto.RoomDTO;
import com.example.kolla.models.Room;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);
        
        // Custom mapping for Room and RoomDTO
        modelMapper.createTypeMap(Room.class, RoomDTO.class)
                .addMapping(Room::getRoomName, RoomDTO::setRoomName)
                .addMapping(Room::getRoomCode, RoomDTO::setRoomCode);
        
        modelMapper.createTypeMap(RoomDTO.class, Room.class)
                .addMapping(RoomDTO::getRoomName, Room::setRoomName)
                .addMapping(RoomDTO::getRoomCode, Room::setRoomCode);
        
        return modelMapper;
    }
}