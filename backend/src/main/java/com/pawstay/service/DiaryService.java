package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawstay.common.BusinessException;
import com.pawstay.dto.DiaryDTO;
import com.pawstay.entity.BoardingOrder;
import com.pawstay.entity.CareRecord;
import com.pawstay.mapper.CareRecordMapper;
import com.pawstay.mapper.OrderMapper;
import com.pawstay.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiaryService {

    @Autowired
    private CareRecordMapper careRecordMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private MessageService messageService;

    /** 商家每日护理打卡：仅寄养中的订单可打卡，打卡后通知主人 */
    public void save(Long orderId, DiaryDTO dto) {
        Long merchantId = merchantService.myShop().getId();
        BoardingOrder o = orderMapper.selectById(orderId);
        if (o == null || !o.getMerchantId().equals(merchantId)) throw new BusinessException("订单不存在");
        if (!"BOARDING".equals(o.getStatus())) throw new BusinessException("仅寄养中的订单可以更新寄养日记");

        CareRecord r = new CareRecord();
        r.setOrderId(orderId);
        r.setMerchantId(merchantId);
        r.setRecordDate(dto.recordDate() == null ? LocalDate.now() : dto.recordDate());
        r.setMealDone(dto.mealDone() == null ? 0 : dto.mealDone());
        r.setWalkDone(dto.walkDone() == null ? 0 : dto.walkDone());
        r.setMood(dto.mood());
        r.setContent(dto.content());
        r.setPhotos(dto.photos());
        r.setWeight(dto.weight());
        r.setCreateTime(LocalDateTime.now());
        careRecordMapper.insert(r);

        messageService.send(o.getUserId(), "寄养日记更新", "毛孩子今天（" + r.getRecordDate() + "）的寄养日记已更新，快去看看它吧～", "DIARY", orderId);
    }

    /** 日记列表：订单主人与接单商家可见 */
    public List<CareRecord> listByOrder(Long orderId) {
        BoardingOrder o = orderMapper.selectById(orderId);
        if (o == null) throw new BusinessException("订单不存在");
        var lu = UserContext.requireLogin();
        boolean owner = o.getUserId().equals(lu.getId());
        boolean merchant = "MERCHANT".equals(lu.getRole()) && o.getMerchantId().equals(merchantService.myShop().getId());
        if (!owner && !merchant && !"ADMIN".equals(lu.getRole())) throw new BusinessException(403, "无权查看该日记");
        return careRecordMapper.selectList(new LambdaQueryWrapper<CareRecord>()
                .eq(CareRecord::getOrderId, orderId)
                .orderByDesc(CareRecord::getRecordDate).orderByDesc(CareRecord::getId));
    }
}
