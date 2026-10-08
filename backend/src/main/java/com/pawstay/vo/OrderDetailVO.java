package com.pawstay.vo;

import com.pawstay.entity.BoardingOrder;
import com.pawstay.entity.CareRecord;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.Pet;
import com.pawstay.entity.Review;
import com.pawstay.entity.Room;
import lombok.Data;

import java.util.List;

/**
 * 订单详情视图：聚合订单、宠物、商家、房型、寄养日记与评价
 */
@Data
public class OrderDetailVO {
    private BoardingOrder order;
    private Pet pet;
    private Merchant merchant;
    private Room room;
    private List<CareRecord> diaries;
    private Review review;
}
