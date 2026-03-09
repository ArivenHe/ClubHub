package com.clubhub.service;

import com.clubhub.dto.LoginRequest;
import com.clubhub.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout();

    void changePassword(String oldPassword, String newPassword);
}
