package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawstay.common.BusinessException;
import com.pawstay.dto.PetDTO;
import com.pawstay.entity.Pet;
import com.pawstay.mapper.OrderMapper;
import com.pawstay.mapper.PetMapper;
import com.pawstay.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PetService {

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private OrderMapper orderMapper;

    public List<Pet> myPets() {
        return petMapper.selectList(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getUserId, UserContext.requireLogin().getId())
                .orderByDesc(Pet::getId));
    }

    public void save(PetDTO dto) {
        if (dto.name() == null || dto.name().isBlank()) throw new BusinessException("请填写宠物昵称");
        if (!"CAT".equals(dto.species()) && !"DOG".equals(dto.species())) throw new BusinessException("请选择物种");
        Long uid = UserContext.requireLogin().getId();

        Pet pet = new Pet();
        pet.setName(dto.name());
        pet.setSpecies(dto.species());
        pet.setBreed(dto.breed());
        pet.setGender(dto.gender());
        pet.setBirthDate(dto.birthDate());
        pet.setWeight(dto.weight());
        pet.setSterilized(dto.sterilized() == null ? 0 : dto.sterilized());
        pet.setTemperament(dto.temperament());
        pet.setAggressive(dto.aggressive() == null ? 0 : dto.aggressive());
        pet.setVaccinated(dto.vaccinated() == null ? 0 : dto.vaccinated());
        pet.setVaccineNote(dto.vaccineNote());
        pet.setPhoto(dto.photo());
        pet.setNotes(dto.notes());

        if (dto.id() == null) {
            pet.setUserId(uid);
            pet.setCreateTime(LocalDateTime.now());
            petMapper.insert(pet);
        } else {
            Pet old = petMapper.selectById(dto.id());
            if (old == null || !old.getUserId().equals(uid)) throw new BusinessException("宠物不存在");
            pet.setId(dto.id());
            pet.setUserId(uid);
            petMapper.updateById(pet);
        }
    }

    public void delete(Long id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null || !pet.getUserId().equals(UserContext.requireLogin().getId())) {
            throw new BusinessException("宠物不存在");
        }
        Long orderCnt = orderMapper.selectCount(new LambdaQueryWrapper<com.pawstay.entity.BoardingOrder>()
                .eq(com.pawstay.entity.BoardingOrder::getPetId, id));
        if (orderCnt > 0) throw new BusinessException("该宠物已有寄养记录，为保留历史订单无法删除");
        petMapper.deleteById(id);
    }
}
