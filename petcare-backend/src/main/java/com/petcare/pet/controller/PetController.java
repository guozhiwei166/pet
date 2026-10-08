package com.petcare.pet.controller;

import com.petcare.common.Result;
import com.petcare.pet.dto.PetDTO;
import com.petcare.pet.dto.VaccineDTO;
import com.petcare.pet.entity.PetHealth;
import com.petcare.pet.entity.PetTag;
import com.petcare.pet.service.PetService;
import com.petcare.pet.vo.PetVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping("/pets")
    public Result<List<PetVO>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(petService.list(keyword));
    }

    @PostMapping("/pets")
    public Result<PetVO> save(@Valid @RequestBody PetDTO dto) {
        return Result.ok(petService.save(dto));
    }

    @PutMapping("/pets/{id}")
    public Result<PetVO> update(@PathVariable Long id, @Valid @RequestBody PetDTO dto) {
        dto = new PetDTO(id, dto.name(), dto.species(), dto.breed(), dto.gender(), dto.birthDate(),
                dto.weight(), dto.sterilized(), dto.furColor(), dto.photo(), dto.temperament(),
                dto.notes(), dto.tagIds());
        return Result.ok(petService.save(dto));
    }

    @DeleteMapping("/pets/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return Result.ok();
    }

    @GetMapping("/pets/{id}")
    public Result<PetVO> detail(@PathVariable Long id) {
        return Result.ok(petService.detail(id));
    }

    @PutMapping("/pets/{id}/tags")
    public Result<Void> setTags(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        petService.setTagsInternal(id, body.get("tagIds"));
        return Result.ok();
    }

    @GetMapping("/pet-tags")
    public Result<List<PetTag>> tagDict() {
        return Result.ok(petService.tagDict());
    }

    @GetMapping("/pets/{id}/vaccines")
    public Result<List<PetVO.MapVaccine>> vaccines(@PathVariable Long id) {
        return Result.ok(petService.vaccines(id));
    }

    @PostMapping("/pets/{id}/vaccines")
    public Result<Void> addVaccine(@PathVariable Long id, @Valid @RequestBody VaccineDTO dto) {
        petService.saveVaccine(id, dto);
        return Result.ok();
    }

    @PutMapping("/pets/{id}/vaccines/{vid}")
    public Result<Void> updateVaccine(@PathVariable Long id, @PathVariable Long vid,
                                      @Valid @RequestBody VaccineDTO dto) {
        petService.saveVaccine(id, new VaccineDTO(vid, dto.vaccineName(), dto.inoculationDate(),
                dto.expireDate(), dto.org(), dto.remark()));
        return Result.ok();
    }

    @DeleteMapping("/pets/{id}/vaccines/{vid}")
    public Result<Void> deleteVaccine(@PathVariable Long id, @PathVariable Long vid) {
        petService.deleteVaccine(id, vid);
        return Result.ok();
    }

    @GetMapping("/pets/{id}/health")
    public Result<List<PetHealth>> healthList(@PathVariable Long id,
                                              @RequestParam(defaultValue = "30") int days) {
        return Result.ok(petService.healthList(id, days));
    }

    @PostMapping("/pets/{id}/health")
    public Result<Void> addHealth(@PathVariable Long id, @RequestBody PetHealth record) {
        petService.addHealth(id, record);
        return Result.ok();
    }

    @GetMapping("/pets/{id}/growth")
    public Result<Map<String, Object>> growth(@PathVariable Long id) {
        return Result.ok(petService.growth(id));
    }
}
