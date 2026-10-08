package com.tourism.service;

import com.tourism.dto.MerchantProfileDTO;
import com.tourism.entity.Merchant;
import com.tourism.vo.MerchantPublicVO;

/**
 * 商家服务(身份与审核)
 */
public interface MerchantService {

    /** 当前登录用户对应的商家资料;若不存在抛出业务异常 */
    Merchant currentMerchant();

    /** 指定 userId 对应的商家资料;可能为 null */
    Merchant getByUserId(Long userId);

    /** 校验商家已通过审核,否则抛出异常(发布民宿/房型等经营操作前调用) */
    void requireApproved();

    /** 更新商家资料(资料变更后需重新审核?否:资料可即时生效,名称修改需重新审核) */
    Merchant updateProfile(MerchantProfileDTO dto);

    /** 商家公开主页(访客可见):主体信息 + 掌柜账号 + 名下在营民宿 */
    MerchantPublicVO publicDetail(Long merchantId);
}
