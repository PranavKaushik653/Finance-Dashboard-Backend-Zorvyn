package com.financeDashboard.backend.dto;

import com.financeDashboard.backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType="Bearer";
    private Long userId;
    private String username;
    private String email;
    private Role role;
}
