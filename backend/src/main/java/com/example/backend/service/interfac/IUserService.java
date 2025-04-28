package com.example.backend.service.interfac;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.Response;
import com.example.backend.entity.User;

public interface IUserService {
    Response register(User user);

    Response login(LoginRequest loginRequest);

    Response getAllUsers();

    Response getUserBookingHistory(String userId);

    Response deleteUser(String userId);

    Response getUserById(String userId);

    Response getMyInfo(String email);

}