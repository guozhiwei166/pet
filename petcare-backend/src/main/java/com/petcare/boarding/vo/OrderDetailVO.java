package com.petcare.boarding.vo;

import com.petcare.boarding.entity.BoardingOrder;
import com.petcare.boarding.entity.BoardingOrderServiceItem;
import com.petcare.payment.entity.Payment;
import com.petcare.pet.entity.Pet;
import com.petcare.room.entity.Room;
import com.petcare.room.entity.RoomType;
import com.petcare.review.entity.Review;
import lombok.Data;

import java.util.List;

/**
 * 订单详情聚合视图
 */
@Data
public class OrderDetailVO {
    private BoardingOrder order;
    private Pet pet;
    private RoomType roomType;
    private Room room;
    private List<BoardingOrderServiceItem> services;
    private List<Payment> payments;
    private Review review;
}
