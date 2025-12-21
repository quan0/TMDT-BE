package com.mindwell.be.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Me")
public record MeDto(
        Integer userId,
        String email,
        String fullName,
        String phoneNumber,
        Integer mindpointsBalance
) {
}
