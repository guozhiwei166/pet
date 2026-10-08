package com.pawstay.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pawstay.common.BusinessException;
import com.pawstay.dto.AuditDTO;
import com.pawstay.dto.ShopDTO;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.Room;
import com.pawstay.entity.Review;
import com.pawstay.entity.User;
import com.pawstay.mapper.MerchantMapper;
import com.pawstay.mapper.RoomMapper;
import com.pawstay.mapper.ReviewMapper;
import com.pawstay.mapper.UserMapper;
import com.pawstay.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MessageService messageService;

    public IPage<Merchant> pageHome(long page, long size, String keyword, String species) {
        return merchantMapper.pageHome(new Page<>(page, size), keyword, species);
    }

    /** 商家详情（公开）：店铺信息 + 在售房型 + 最新带评价 */
    public Map<String, Object> detail(Long id) {
        Merchant m = merchantMapper.selectById(id);
        if (m == null) throw new BusinessException("商家不存在");
        List<Room> rooms = roomMapper.selectList(new LambdaQueryWrapper<Room>()
                .eq(Room::getMerchantId, id).eq(Room::getStatus, 1).orderByAsc(Room::getPricePerDay));
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getMerchantId, id).orderByDesc(Review::getId).last("LIMIT 10"));

        List<Map<String, Object>> reviewVos = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("id", r.getId());
            vo.put("rating", r.getRating());
            vo.put("content", r.getContent());
            vo.put("photos", r.getPhotos());
            vo.put("reply", r.getReply());
            vo.put("createTime", r.getCreateTime());
            User u = userMapper.selectById(r.getUserId());
            vo.put("nickname", u == null ? "匿名用户" : u.getNickname());
            reviewVos.add(vo);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("merchant", m);
        res.put("rooms", rooms);
        res.put("reviews", reviewVos);
        return res;
    }

    public Merchant myShop() {
        Merchant m = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, UserContext.requireLogin().getId()));
        if (m == null) throw new BusinessException("商家信息不存在");
        return m;
    }

    public void updateShop(ShopDTO dto) {
        Merchant m = myShop();
        if (dto.shopName() != null) m.setShopName(dto.shopName());
        if (dto.address() != null) m.setAddress(dto.address());
        if (dto.phone() != null) m.setPhone(dto.phone());
        if (dto.license() != null) m.setLicense(dto.license());
        if (dto.description() != null) m.setDescription(dto.description());
        if (dto.tags() != null) m.setTags(dto.tags());
        if (dto.serviceSpecies() != null) m.setServiceSpecies(dto.serviceSpecies());
        merchantMapper.updateById(m);
    }

    public void audit(Long id, AuditDTO dto) {
        Merchant m = merchantMapper.selectById(id);
        if (m == null) throw new BusinessException("商家不存在");
        m.setAuditStatus(Boolean.TRUE.equals(dto.pass()) ? "APPROVED" : "REJECTED");
        m.setAuditReason(dto.reason());
        merchantMapper.updateById(m);
        messageService.send(m.getUserId(), Boolean.TRUE.equals(dto.pass()) ? "入驻审核通过" : "入驻审核未通过",
                Boolean.TRUE.equals(dto.pass())
                        ? "恭喜！您的店铺已通过平台审核，现在可以发布房型开始接单了。"
                        : "很遗憾，审核未通过：" + (dto.reason() == null ? "资质信息不完整" : dto.reason()),
                "SYSTEM", null);
    }

    public IPage<Merchant> pageForAdmin(long page, long size, String status) {
        LambdaQueryWrapper<Merchant> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank() && !"ALL".equals(status)) w.eq(Merchant::getAuditStatus, status);
        w.orderByDesc(Merchant::getId);
        return merchantMapper.selectPage(new Page<>(page, size), w);
    }
}
