package com.example.backend.service.impl;

import com.example.backend.dto.Response;
import com.example.backend.dto.RoomDTO;
import com.example.backend.entity.Room;
import com.example.backend.exception.OurException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.RoomRepository;
import com.example.backend.service.AwsS3Service;
import com.example.backend.service.interfac.IRoomService;
import com.example.backend.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService implements IRoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private AwsS3Service awsS3Service;

    @Override
    public Response addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice, String description) {
        Response response = new Response();

        try {
            if (photo == null || photo.isEmpty()) {
                throw new OurException("Room photo is required");
            }
            if (roomType == null || roomType.trim().isEmpty()) {
                throw new OurException("Room type is required");
            }
            if (roomPrice == null || roomPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new OurException("Room price must be greater than zero");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new OurException("Room description is required");
            }

            String imageUrl = awsS3Service.saveImageToS3(photo);

            Room room = new Room();
            room.setRoomPhotoUrl(imageUrl);
            room.setRoomType(roomType);
            room.setRoomPrice(roomPrice);
            room.setRoomDescription(description);

            Room savedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(savedRoom);

            response.setStatusCode(200);
            response.setMessage("Room added successfully");
            response.setRoom(roomDTO);

        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public Response getAllRooms() {
        Response response = new Response();

        try {
            List<Room> rooms = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(rooms);

            response.setStatusCode(200);
            response.setMessage("Rooms retrieved successfully");
            response.setRoomList(roomDTOList);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving rooms: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response deleteRoom(Long roomId) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room Not Found"));

            roomRepository.delete(room);

            response.setStatusCode(200);
            response.setMessage("Room deleted successfully");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response updateRoom(Long roomId, String description, String roomType, BigDecimal roomPrice, MultipartFile photo) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room Not Found"));

            if (roomType != null) room.setRoomType(roomType);
            if (roomPrice != null) room.setRoomPrice(roomPrice);
            if (description != null) room.setRoomDescription(description);
            if (photo != null && !photo.isEmpty()) {
                String imageUrl = awsS3Service.saveImageToS3(photo);
                room.setRoomPhotoUrl(imageUrl);
            }

            Room updatedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(updatedRoom);

            response.setStatusCode(200);
            response.setMessage("Room updated successfully");
            response.setRoom(roomDTO);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error updating room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getRoomById(Long roomId) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room Not Found"));
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTOPlusBookings(room);

            response.setStatusCode(200);
            response.setMessage("Room retrieved successfully");
            response.setRoom(roomDTO);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAvailableRoomsByDataAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        Response response = new Response();

        try {
            List<Room> availableRooms = roomRepository.findAvailableRoomsByDatesAndTypes(checkInDate, checkOutDate, roomType);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(availableRooms);

            response.setStatusCode(200);
            response.setMessage("Available rooms retrieved successfully");
            response.setRoomList(roomDTOList);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving available rooms: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllAvailableRooms() {
        Response response = new Response();

        try {
            List<Room> rooms = roomRepository.getAllAvailableRooms();
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(rooms);

            response.setStatusCode(200);
            response.setMessage("Available rooms retrieved successfully");
            response.setRoomList(roomDTOList);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving available rooms: " + e.getMessage());
        }
        return response;
    }
}
