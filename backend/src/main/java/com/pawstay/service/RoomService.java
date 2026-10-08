package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawstay.common.BusinessException;
import com.pawstay.dto.RoomDTO;
import com.pawstay.entity.BoardingOrder;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.Pet;
import com.pawstay.entity.Room;
import com.pawstay.mapper.OrderMapper;
import com.pawstay.mapper.PetMapper;
import com.pawstay.mapper.RoomMapper;
import com.pawstay.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoomService {

    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private PetMapper petMapper;

    public List<Room> myRooms() {
        Merchant m = merchantService.myShop();
        return roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .eq(Room::getMerchantId, m.getId()).orderByAsc(Room::getPricePerDay));
    }

    public void save(RoomDTO dto) {
        Merchant m = merchantService.myShop();
        Room room = new Room();
        room.setName(dto.name());
        room.setSpecies(dto.species() == null ? "ALL" : dto.species());
        room.setMaxWeight(dto.maxWeight());
        room.setNeedSterilized(dto.needSterilized() == null ? 0 : dto.needSterilized());
        room.setAllowAggressive(dto.allowAggressive() == null ? 0 : dto.allowAggressive());
        room.setTotalCount(dto.totalCount() == null ? 1 : dto.totalCount());
        room.setPricePerDay(dto.pricePerDay());
        room.setPhoto(dto.photo());
        room.setTags(dto.tags());
        room.setDescription(dto.description());

        if (dto.id() == null) {
            room.setMerchantId(m.getId());
            room.setStatus(1);
            room.setCreateTime(LocalDateTime.now());
            roomMapper.insert(room);
        } else {
            Room old = roomMapper.selectById(dto.id());
            if (old == null || !old.getMerchantId().equals(m.getId())) throw new BusinessException("房型不存在");
            room.setId(dto.id());
            room.setMerchantId(m.getId());
            roomMapper.updateById(room);
        }
    }

    public void delete(Long id) {
        Merchant m = merchantService.myShop();
        Room room = roomMapper.selectById(id);
        if (room == null || !room.getMerchantId().equals(m.getId())) throw new BusinessException("房型不存在");
        Long active = orderMapper.selectCount(new LambdaQueryWrapper<BoardingOrder>()
                .eq(BoardingOrder::getRoomId, id)
                .in(BoardingOrder::getStatus, "PENDING_CONFIRM", "CONFIRMED", "BOARDING"));
        if (active > 0) throw new BusinessException("该房型存在进行中的订单，暂不能删除");
        roomMapper.deleteById(id);
    }

    public List<Room> listByMerchant(Long merchantId) {
        return roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .eq(Room::getMerchantId, merchantId).eq(Room::getStatus, 1).orderByAsc(Room::getPricePerDay));
    }

    /**
     * 智能排笼（创新点）：按宠物档案与所选日期，过滤并排序可订房型。
     * 硬约束：物种、体重上限、绝育要求、特殊性格接收能力、日期库存；
     * 软策略：房型价格升序排序，最优解标记 recommend。
     */
    public List<Map<String, Object>> matchRooms(Long petId, Long merchantId, LocalDate start, LocalDate end) {
        Long uid = UserContext.requireLogin().getId();
        Pet pet = petMapper.selectById(petId);
        if (pet == null || !pet.getUserId().equals(uid)) throw new BusinessException("宠物不存在");
        if (start == null || end == null || !end.isAfter(start)) throw new BusinessException("离店日期需晚于入住日期");

        List<Room> rooms = listByMerchant(merchantId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Room room : rooms) {
            // 硬约束 1：物种（房间限定猫/狗，ALL 不限）
            if (!"ALL".equals(room.getSpecies()) && !room.getSpecies().equals(pet.getSpecies())) continue;
            // 硬约束 2：体重上限
            if (pet.getWeight() != null && room.getMaxWeight() != null
                    && pet.getWeight().compareTo(room.getMaxWeight()) > 0) continue;
            // 硬约束 3：绝育要求（猫舍混住环境）
            if (room.getNeedSterilized() != null && room.getNeedSterilized() == 1
                    && (pet.getSterilized() == null || pet.getSterilized() != 1)) continue;
            // 硬约束 4：特殊性格（有攻击性的宠物只能住可接收的房型）
            if (pet.getAggressive() != null && pet.getAggressive() == 1
                    && (room.getAllowAggressive() == null || room.getAllowAggressive() != 1)) continue;
            // 硬约束 5：日期区间库存
            int available = room.getTotalCount() - orderMapper.countOverlap(room.getId(), start, end);
            if (available <= 0) continue;

            List<String> reasons = new ArrayList<>();
            if (pet.getWeight() != null && room.getMaxWeight() != null
                    && pet.getWeight().compareTo(room.getMaxWeight().multiply(new BigDecimal("0.6"))) <= 0) {
                reasons.add("体型宽裕适配");
            } else {
                reasons.add("体型可容纳");
            }
            if (room.getNeedSterilized() != null && room.getNeedSterilized() == 1) reasons.add("已绝育专属环境");
            if (room.getAllowAggressive() != null && room.getAllowAggressive() == 1) reasons.add("可照看特殊性格");

            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("room", room);
            vo.put("available", available);
            vo.put("reasons", reasons);
            vo.put("recommend", result.isEmpty()); // 价格升序下首个可订房型即推荐解
            result.add(vo);
        }
        return result;
    }
}
