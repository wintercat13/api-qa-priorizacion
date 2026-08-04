package com.qapriorizacion.api.service;

import com.qapriorizacion.api.dto.request.LoginRequest;
import com.qapriorizacion.api.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
