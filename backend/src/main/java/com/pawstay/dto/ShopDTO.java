package com.pawstay.dto;

public record ShopDTO(
        String shopName,
        String address,
        String phone,
        String license,
        String description,
        String tags,
        String serviceSpecies) {
}
