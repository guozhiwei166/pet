package com.pawstay.dto;

public record AuditDTO(
        Boolean pass,
        String reason) {
}
