package com.pawstay.controller;

import com.pawstay.common.Result;
import com.pawstay.dto.PetDTO;
import com.pawstay.entity.Pet;
import com.pawstay.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 宠物档案管理（主人端） */
@RestController
@RequestMapping("/api/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping
    public Result<List<Pet>> myPets() {
        return Result.ok(petService.myPets());
    }

    @PostMapping
    public Result<Void> save(@RequestBody PetDTO dto) {
        petService.save(dto);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody PetDTO dto) {
        petService.save(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return Result.ok();
    }
}
