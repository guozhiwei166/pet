package com.petcare.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.mapper.BoardingOrderMapper;
import com.petcare.common.BusinessException;
import com.petcare.pet.dto.PetDTO;
import com.petcare.pet.dto.VaccineDTO;
import com.petcare.pet.entity.*;
import com.petcare.pet.mapper.*;
import com.petcare.pet.vo.PetVO;
import com.petcare.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetMapper petMapper;
    private final PetTagMapper petTagMapper;
    private final PetTagRelationMapper petTagRelationMapper;
    private final PetVaccineMapper petVaccineMapper;
    private final PetHealthMapper petHealthMapper;
    private final BoardingOrderMapper boardingOrderMapper;

    /** 宠物列表：主人看自己的，员工/管理员看全部 */
    public List<PetVO> list(String keyword) {
        String role = SecurityUtils.currentRole();
        LambdaQueryWrapper<Pet> w = new LambdaQueryWrapper<>();
        if ("ROLE_USER".equals(role)) w.eq(Pet::getUserId, SecurityUtils.currentId());
        if (keyword != null && !keyword.isBlank()) w.like(Pet::getName, keyword);
        w.orderByDesc(Pet::getId);
        return petMapper.selectList(w).stream().map(this::toVO).collect(Collectors.toList());
    }

    public PetVO detail(Long id) {
        Pet pet = mustGetWithPermission(id);
        return toVO(pet);
    }

    /** 供其它模块复用宠物可见性校验（属主 / STAFF / ADMIN），越权时抛 403 */
    public void assertVisible(Long petId) {
        mustGetWithPermission(petId);
    }

    @Transactional
    public PetVO save(PetDTO dto) {
        Long uid = SecurityUtils.currentId();
        // 员工/管理员不通过此接口创建宠物（会把档案挂到员工账号下）；仅主人建档
        String role = SecurityUtils.currentRole();
        if (dto.id() == null && !"ROLE_USER".equals(role)) {
            throw new BusinessException(403, "宠物档案由主人在个人中心添加，员工端仅可查看");
        }
        Pet pet = new Pet();
        pet.setName(dto.name() == null ? null : dto.name().trim());
        pet.setSpecies(dto.species());
        pet.setBreed(dto.breed());
        pet.setGender(dto.gender());
        pet.setBirthDate(dto.birthDate());
        pet.setWeight(dto.weight());
        pet.setSterilized(dto.sterilized() == null ? 0 : dto.sterilized());
        pet.setFurColor(dto.furColor());
        pet.setPhoto(dto.photo());
        pet.setTemperament(dto.temperament());
        pet.setNotes(dto.notes());

        if (dto.id() == null) {
            pet.setUserId(uid);
            requireUniqueName(uid, pet.getName(), null);
            petMapper.insert(pet);
        } else {
            Pet old = mustOwn(dto.id());
            requireUniqueName(old.getUserId(), pet.getName(), old.getId());
            pet.setId(old.getId());
            pet.setUserId(old.getUserId());
            petMapper.updateById(pet);
        }
        if (dto.tagIds() != null) setTagsInternal(pet.getId(), dto.tagIds());
        return toVO(petMapper.selectById(pet.getId()));
    }

    @Transactional
    public void delete(Long id) {
        // 仅宠物主人本人可删除（员工/管理员只读，避免误删他人档案）
        String role = SecurityUtils.currentRole();
        if (!"ROLE_USER".equals(role)) throw new BusinessException(403, "仅主人本人可删除宠物档案");
        mustOwn(id);
        Long count = boardingOrderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getPetId, id)
                .ne(BoardingOrder::getStatus, "CANCELLED")
                .ne(BoardingOrder::getStatus, "REFUNDED"));
        if (count > 0) throw new BusinessException("该宠物已有寄养记录，为保留历史档案无法删除");
        petMapper.deleteById(id);
        petTagRelationMapper.delete(new LambdaQueryWrapper<PetTagRelation>().eq(PetTagRelation::getPetId, id));
    }

    /** 覆盖式设置性格标签（仅宠物主人本人） */
    @Transactional
    public void setTagsInternal(Long petId, List<Long> tagIds) {
        mustOwn(petId);
        petTagRelationMapper.delete(new LambdaQueryWrapper<PetTagRelation>().eq(PetTagRelation::getPetId, petId));
        if (tagIds == null) return;
        for (Long tagId : tagIds) {
            PetTagRelation r = new PetTagRelation();
            r.setPetId(petId);
            r.setTagId(tagId);
            petTagRelationMapper.insert(r);
        }
    }

    public List<PetTag> tagDict() {
        return petTagMapper.selectList(new LambdaQueryWrapper<PetTag>().eq(PetTag::getStatus, 1).orderByAsc(PetTag::getId));
    }

    // ---------- 疫苗 ----------

    public List<PetVO.MapVaccine> vaccines(Long petId) {
        mustGetWithPermission(petId);
        return petVaccineMapper.selectList(new LambdaQueryWrapper<PetVaccine>()
                        .eq(PetVaccine::getPetId, petId).orderByDesc(PetVaccine::getInoculationDate))
                .stream().map(this::toVaccine).collect(Collectors.toList());
    }

    @Transactional
    public void saveVaccine(Long petId, VaccineDTO dto) {
        mustOwn(petId);
        PetVaccine v = new PetVaccine();
        v.setPetId(petId);
        v.setVaccineName(dto.vaccineName());
        v.setInoculationDate(dto.inoculationDate());
        v.setExpireDate(dto.expireDate());
        v.setOrg(dto.org());
        v.setRemark(dto.remark());
        if (dto.id() == null) {
            petVaccineMapper.insert(v);
        } else {
            PetVaccine old = petVaccineMapper.selectById(dto.id());
            if (old == null || !old.getPetId().equals(petId)) throw new BusinessException("疫苗记录不存在");
            v.setId(dto.id());
            petVaccineMapper.updateById(v);
        }
    }

    public void deleteVaccine(Long petId, Long vaccineId) {
        mustOwn(petId);
        PetVaccine v = petVaccineMapper.selectById(vaccineId);
        if (v == null || !v.getPetId().equals(petId)) throw new BusinessException("疫苗记录不存在");
        petVaccineMapper.deleteById(vaccineId);
    }

    // ---------- 健康记录 ----------

    public List<PetHealth> healthList(Long petId, int days) {
        mustGetWithPermission(petId);
        LocalDate from = LocalDate.now().minusDays(days);
        return petHealthMapper.selectList(new LambdaQueryWrapper<PetHealth>()
                .eq(PetHealth::getPetId, petId)
                .ge(PetHealth::getRecordDate, from)
                .orderByDesc(PetHealth::getRecordDate));
    }

    /** 健康观察录入：仅员工/管理员（照护链路使用） */
    public void addHealth(Long petId, PetHealth record) {
        mustGetWithPermission(petId);
        String role = SecurityUtils.currentRole();
        if ("ROLE_USER".equals(role)) throw new BusinessException(403, "健康记录仅限工作人员录入");
        record.setPetId(petId);
        record.setStaffId(SecurityUtils.currentId());
        if (record.getRecordDate() == null) record.setRecordDate(LocalDate.now());
        petHealthMapper.insert(record);
    }

    // ---------- 成长档案 ----------

    public Map<String, Object> growth(Long petId) {
        mustGetWithPermission(petId);
        List<BoardingOrder> orders = boardingOrderMapper.selectList(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getPetId, petId)
                .in(BoardingOrder::getStatus, "BOARDING", "COMPLETED")
                .orderByDesc(BoardingOrder::getStartDate));

        int totalDays = orders.stream().mapToInt(o -> o.getDays() == null ? 0 : o.getDays()).sum();

        // 体重曲线：健康观察记录为主，辅以档案当前体重
        List<PetHealth> healths = petHealthMapper.selectList(new LambdaQueryWrapper<PetHealth>()
                .eq(PetHealth::getPetId, petId).isNotNull(PetHealth::getWeight)
                .orderByAsc(PetHealth::getRecordDate));        Pet pet = petMapper.selectById(petId);
        List<Map<String, Object>> curve = new ArrayList<>();
        for (PetHealth h : healths) {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("date", h.getRecordDate().toString());
            p.put("weight", h.getWeight());
            curve.add(p);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalBoardings", orders.size());
        res.put("totalDays", totalDays);
        res.put("currentWeight", pet == null ? null : pet.getWeight());
        res.put("weightCurve", curve);
        res.put("boardings", orders);
        return res;
    }

    // ---------- 内部方法 ----------

    /**
     * 同一主人下宠物昵称唯一：新增时按当前用户校验，编辑时按原主人校验并排除自己。
     * 已逻辑删除的档案不参与判重（删除后可以用回同一个名字）。
     */
    private void requireUniqueName(Long ownerId, String name, Long excludeId) {
        if (name == null || name.isBlank()) return;
        Long dup = petMapper.selectCount(new LambdaQueryWrapper<Pet>()
                .eq(Pet::getUserId, ownerId)
                .eq(Pet::getName, name)
                .ne(excludeId != null, Pet::getId, excludeId));
        if (dup != null && dup > 0) {
            throw new BusinessException("已经有叫「" + name + "」的毛孩子了，换个名字吧");
        }
    }

    private Pet mustOwn(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new BusinessException("宠物不存在");
        String role = SecurityUtils.currentRole();
        if ("ROLE_USER".equals(role) && !pet.getUserId().equals(SecurityUtils.currentId())) {
            throw new BusinessException(403, "无权操作该宠物");
        }
        return pet;
    }

    /** 详情读取：属主 / STAFF / ADMIN */
    private Pet mustGetWithPermission(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) throw new BusinessException("宠物不存在");
        String role = SecurityUtils.currentRole();
        if ("ROLE_USER".equals(role) && !pet.getUserId().equals(SecurityUtils.currentId())) {
            throw new BusinessException(403, "无权查看该宠物");
        }
        return pet;
    }

    private PetVO toVO(Pet pet) {
        PetVO vo = new PetVO();
        org.springframework.beans.BeanUtils.copyProperties(pet, vo);
        if (pet.getBirthDate() != null) {
            vo.setAge((int) ChronoUnit.YEARS.between(pet.getBirthDate(), LocalDate.now()));
        }
        List<Long> tagIds = petTagRelationMapper.selectList(new LambdaQueryWrapper<PetTagRelation>()
                        .eq(PetTagRelation::getPetId, pet.getId()))
                .stream().map(PetTagRelation::getTagId).toList();
        if (!tagIds.isEmpty()) {
            vo.setTags(petTagMapper.selectBatchIds(tagIds).stream().map(PetTag::getName).toList());
        } else {
            vo.setTags(List.of());
        }
        return vo;
    }

    private PetVO.MapVaccine toVaccine(PetVaccine v) {
        PetVO.MapVaccine m = new PetVO.MapVaccine();
        m.setId(v.getId());
        m.setVaccineName(v.getVaccineName());
        m.setInoculationDate(v.getInoculationDate() == null ? null : v.getInoculationDate().toString());
        m.setExpireDate(v.getExpireDate() == null ? null : v.getExpireDate().toString());
        m.setOrg(v.getOrg());
        m.setRemark(v.getRemark());
        if (v.getExpireDate() == null) {
            m.setStatus("有效");
        } else if (v.getExpireDate().isBefore(LocalDate.now())) {
            m.setStatus("已过期");
        } else if (v.getExpireDate().isBefore(LocalDate.now().plusDays(30))) {
            m.setStatus("即将到期");
        } else {
            m.setStatus("有效");
        }
        return m;
    }
}
