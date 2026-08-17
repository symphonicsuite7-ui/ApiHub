package com.apihub.auth.service;

import com.apihub.auth.dto.LoginRequest;
import com.apihub.auth.dto.LoginVO;
import com.apihub.auth.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    LoginVO currentUser(String token);
}
