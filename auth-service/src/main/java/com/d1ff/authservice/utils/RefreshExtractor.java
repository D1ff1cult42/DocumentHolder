package com.d1ff.authservice.utils;

import com.d1ff.authservice.exceptions.TokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;

public class RefreshExtractor {
    public static String extract(HttpServletRequest request){
        if(request.getCookies() == null){
            throw new TokenException("Refresh token not found");
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new TokenException("Refresh token not found"));
    }
}
