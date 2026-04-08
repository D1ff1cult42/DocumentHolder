package com.d1ff.utils;

import com.d1ff.utils.dto.AnalyticDto;
import jakarta.servlet.http.HttpServletRequest;

public class IpAndUserAgentResolver {
    public static AnalyticDto resolve(HttpServletRequest request){
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();

        String userAgent = request.getHeader("User-Agent");
        return new AnalyticDto(ip, userAgent);
    }
}
