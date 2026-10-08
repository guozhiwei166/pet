package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.common.BizException;
import com.tourism.common.Constants;
import com.tourism.common.ResultCode;
import com.tourism.dto.MerchantProfileDTO;
import com.tourism.entity.Destination;
import com.tourism.entity.Homestay;
import com.tourism.entity.Merchant;
import com.tourism.entity.User;
import com.tourism.mapper.DestinationMapper;
import com.tourism.mapper.HomestayMapper;
import com.tourism.mapper.MerchantMapper;
import com.tourism.mapper.UserMapper;
import com.tourism.security.LoginUser;
import com.tourism.service.MerchantService;
import com.tourism.vo.MerchantPublicVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商家服务实现
 */
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final HomestayMapper homestayMapper;
    private final DestinationMapper destinationMapper;

    @Override
    public Merchant currentMerchant() {
        User user = userMapper.selectById(LoginUser.userId());
        if (user == null || !Constants.ROLE_MERCHANT.equals(user.getRole())) {
            throw new BizException(ResultCode.FORBIDDEN, "当前账号不是商家");
        }
        Merchant merchant = getByUserId(user.getId());
        if (merchant == null) {
            throw new BizException("商家资料不存在,请重新提交入驻申请");
        }
        return merchant;
    }

    @Override
    public Merchant getByUserId(Long userId) {
        return merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId)
                .last("limit 1"));
    }

    @Override
    public void requireApproved() {
        Merchant merchant = currentMerchant();
        if (!Constants.AUDIT_APPROVED.equals(merchant.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "商家尚未通过审核,无法进行经营操作");
        }
    }

    @Override
    public Merchant updateProfile(MerchantProfileDTO dto) {
        Merchant merchant = currentMerchant();
        boolean nameChanged = !Objects.equals(merchant.getMerchantName(), dto.getMerchantName());
        // 联系人/电话/简介为日常维护信息,即时生效;
        // 仅修改商家名称(对外经营主体标识)时需重新审核,防止绕开审核改名经营
        merchant.setMerchantName(dto.getMerchantName());
        merchant.setContactName(dto.getContactName());
        merchant.setContactPhone(dto.getContactPhone());
        merchant.setDescription(dto.getDescription());
        if (nameChanged && Constants.AUDIT_APPROVED.equals(merchant.getStatus())) {
            merchant.setStatus(Constants.AUDIT_PENDING);
            merchant.setAuditTime(null);
            merchant.setAuditRemark("商家名称变更,重新审核");
        }
        merchantMapper.updateById(merchant);
        return merchant;
    }

    @Override
    public MerchantPublicVO publicDetail(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商家不存在");
        }
        MerchantPublicVO vo = new MerchantPublicVO();
        vo.setId(merchant.getId());
        vo.setMerchantName(merchant.getMerchantName());
        vo.setContactName(merchant.getContactName());
        vo.setContactPhone(merchant.getContactPhone());
        vo.setLicenseNo(merchant.getLicenseNo());
        vo.setDescription(merchant.getDescription());
        vo.setStatus(merchant.getStatus());
        // 掌柜账号(昵称/头像)
        User owner = userMapper.selectById(merchant.getUserId());
        if (owner != null) {
            vo.setNickname(owner.getNickname());
            vo.setAvatar(owner.getAvatar());
        }
        // 名下在营民宿(审核通过且营业中)
        List<Homestay> list = homestayMapper.selectList(new LambdaQueryWrapper<Homestay>()
                .eq(Homestay::getMerchantId, merchantId)
                .eq(Homestay::getAuditStatus, Constants.AUDIT_APPROVED)
                .eq(Homestay::getBusinessStatus, Constants.BIZ_OPEN)
                .orderByDesc(Homestay::getSortOrder));
        vo.setHomestayCount((long) list.size());
        // 目的地名(批量;列表为空时跳过,避免 IN () 语法错误)
        Map<Long, String> destNames = Map.of();
        List<Long> destIds = list.stream().map(Homestay::getDestinationId).filter(Objects::nonNull).distinct().toList();
        if (!destIds.isEmpty()) {
            destNames = destinationMapper.selectBatchIds(destIds).stream()
                    .collect(Collectors.toMap(Destination::getId, Destination::getName, (a, b) -> a));
        }
        final Map<Long, String> names = destNames;
        vo.setHomestays(list.stream().map(h -> {
            MerchantPublicVO.HomestayItem item = new MerchantPublicVO.HomestayItem();
            item.setId(h.getId());
            item.setName(h.getName());
            item.setCoverImage(h.getCoverImage());
            item.setDestinationName(names.get(h.getDestinationId()));
            item.setAddress(h.getAddress());
            item.setIntro(h.getIntro());
            item.setFeatures(h.getFeatures());
            item.setRating(h.getRating());
            item.setRatingCount(h.getRatingCount());
            item.setMinPrice(h.getMinPrice());
            return item;
        }).toList());
        return vo;
    }
}
