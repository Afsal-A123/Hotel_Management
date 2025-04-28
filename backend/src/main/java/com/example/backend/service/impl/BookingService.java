package com.example.backend.service.impl;

import com.example.backend.dto.BookingDTO;
import com.example.backend.dto.Response;
import com.example.backend.entity.Booking;
import com.example.backend.entity.Room;
import com.example.backend.entity.User;
import com.example.backend.exception.OurException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.RoomRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.interfac.IBookingService;
import com.example.backend.service.interfac.IRoomService;
import com.example.backend.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private IRoomService roomService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Response saveBooking(Long roomId, Long userId, Booking bookingRequest) {
        Response response = new Response();

        try {
            if (bookingRequest.getCheckInDate() == null || bookingRequest.getCheckOutDate() == null) {
                throw new OurException("Check-in and check-out dates must be provided");
            }

            if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
                throw new OurException("Check-out date must come after check-in date");
            }

            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new OurException("Room Not Found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User Not Found"));

            if (!roomIsAvailable(bookingRequest, room.getBookings())) {
                throw new OurException("Room not available for selected date range");
            }

            bookingRequest.setRoom(room);
            bookingRequest.setUser(user);
            bookingRequest.setBookingConfirmationCode(Utils.generateRandomConfirmationCode(10));
            bookingRepository.save(bookingRequest);

            response.setStatusCode(200);
            response.setMessage("Booking successful");
            response.setBookingConfirmationCode(bookingRequest.getBookingConfirmationCode());

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving booking: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response findBookingByConfirmationCode(String confirmationCode) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode)
                    .orElseThrow(() -> new OurException("Booking Not Found"));
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRooms(booking, true);

            response.setStatusCode(200);
            response.setMessage("Booking found successfully");
            response.setBooking(bookingDTO);

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error finding booking: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllBookings() {
        Response response = new Response();

        try {
            List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingList);

            response.setStatusCode(200);
            response.setMessage("Bookings retrieved successfully");
            response.setBookingList(bookingDTOList);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting bookings: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response cancelBooking(Long bookingId) {
        Response response = new Response();

        try {
            bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new OurException("Booking Not Found"));
            bookingRepository.deleteById(bookingId);

            response.setStatusCode(200);
            response.setMessage("Booking cancelled successfully");

        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error cancelling booking: " + e.getMessage());
        }
        return response;
    }

    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        !(bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckInDate()) ||
                          bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckOutDate()))
                );
    }
}
