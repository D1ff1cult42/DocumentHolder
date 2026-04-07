package com.d1ff.authservice.mapper.request;

import com.d1ff.authservice.dto.request.RegisterRequest;
import com.d1ff.authservice.entity.User;
import org.mapstruct.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)

public interface RegisterRequestMapper {
    @Mapping(target = "passwordHash", expression = "java(passwordEncoder.encode(registerRequest.password()))")
    User authRequestToUser(
            @Context BCryptPasswordEncoder passwordEncoder,
            RegisterRequest registerRequest);
}
