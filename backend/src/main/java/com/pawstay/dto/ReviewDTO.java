package com.pawstay.dto;

public record ReviewDTO(
        Integer rating,
        String content,
        String photos) {
}
