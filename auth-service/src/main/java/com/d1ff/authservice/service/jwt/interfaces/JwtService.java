package com.d1ff.authservice.service.jwt.interfaces;

import com.d1ff.authservice.dto.response.AccessTokenResponse;
import com.d1ff.authservice.entity.User;

public interface JwtService {
    AccessTokenResponse createToken(User user);
}
