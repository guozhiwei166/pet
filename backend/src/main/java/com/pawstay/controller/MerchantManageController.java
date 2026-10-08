package com.pawstay.controller;

import com.pawstay.common.Result;
import com.pawstay.dto.RoomDTO;
import com.pawstay.dto.ShopDTO;
import com.pawstay.entity.Merchant;
import com.pawstay.entity.Room;
import com.pawstay.service.MerchantService;
import com.pawstay.service.OrderService;
import com.pawstay.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 商家端：房型管理、店铺信息、今日经营看板 */
@RestController
@RequestMapping("/api/merchant")
public class MerchantManageController {

    @Autowired
    private RoomService roomService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/rooms")
    public Result<List<Room>> myRooms() {
        return Result.ok(roomService.myRooms());
    }

    @PostMapping("/rooms")
    public Result<Void> addRoom(@RequestBody RoomDTO dto) {
        roomService.save(dto);
        return Result.ok();
    }

    @PutMapping("/rooms")
    public Result<Void> updateRoom(@RequestBody RoomDTO dto) {
        roomService.save(dto);
        return Result.ok();
    }

    @DeleteMapping("/rooms/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
        return Result.ok();
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.ok(orderService.merchantDashboard());
    }

    @GetMapping("/shop")
    public Result<Merchant> shop() {
        return Result.ok(merchantService.myShop());
    }

    @PutMapping("/shop")
    public Result<Void> updateShop(@RequestBody ShopDTO dto) {
        merchantService.updateShop(dto);
        return Result.ok();
    }
}
