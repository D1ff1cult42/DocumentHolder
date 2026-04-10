package com.d1ff.utils;
import com.d1ff.exceptions.UnauthorizedException;
import java.util.Arrays;

public class UnauthorizedHandler {
    public static void handleXHeader(String... headers){
        if(headers == null || Arrays.asList(headers).contains(null)){
            throw new UnauthorizedException("Unauthorized");
        }
    }
}
