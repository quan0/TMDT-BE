package com.mindwell.be.controller;

import com.mindwell.be.dto.user.MeDto;
import com.mindwell.be.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's basic profile for prefilling checkout contact fields.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public MeDto me(@AuthenticationPrincipal com.mindwell.be.service.security.UserPrincipal principal) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User user = principal.getUser();
        return new MeDto(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getMindpointsBalance()
        );
    }
}
