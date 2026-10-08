package com.petcare.room.controller;

import com.petcare.common.Result;
import com.petcare.room.entity.Room;
import com.petcare.room.entity.RoomType;
import com.petcare.room.entity.ServiceItem;
import com.petcare.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // ---------- 房型 ----------
    @GetMapping("/room-types")
    public Result<List<RoomType>> roomTypes(@RequestParam(defaultValue = "false") boolean all) {
        return Result.ok(roomService.roomTypes(!all));
    }

    @PostMapping("/room-types")
    @com.petcare.security.RequirePermission("room:type:manage")
    public Result<Void> addRoomType(@RequestBody RoomType type) {
        roomService.saveRoomType(type);
        return Result.ok();
    }

    @PutMapping("/room-types/{id}")
    @com.petcare.security.RequirePermission("room:type:manage")
    public Result<Void> updateRoomType(@PathVariable Long id, @RequestBody RoomType type) {
        type.setId(id);
        roomService.saveRoomType(type);
        return Result.ok();
    }

    @DeleteMapping("/room-types/{id}")
    @com.petcare.security.RequirePermission("room:type:manage")
    public Result<Void> deleteRoomType(@PathVariable Long id) {
        roomService.deleteRoomType(id);
        return Result.ok();
    }

    // ---------- 房间 ----------
    @GetMapping("/rooms/map")
    @com.petcare.security.RequirePermission("room:view")
    public Result<List<Map<String, Object>>> roomMap() {
        return Result.ok(roomService.roomMap());
    }

    @GetMapping("/rooms")
    @com.petcare.security.RequirePermission("room:view")
    public Result<List<Map<String, Object>>> rooms(@RequestParam(defaultValue = "ALL") String status) {
        return Result.ok(roomService.rooms(status).stream().map(r -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("roomNo", r.getRoomNo());
            m.put("floor", r.getFloor());
            m.put("roomTypeId", r.getRoomTypeId());
            m.put("status", r.getStatus());
            m.put("note", r.getNote());
            return m;
        }).toList());
    }

    @PostMapping("/rooms")
    @com.petcare.security.RequirePermission("room:manage")
    public Result<Void> addRoom(@RequestBody Room room) {
        roomService.saveRoom(room);
        return Result.ok();
    }

    @PutMapping("/rooms/{id}")
    @com.petcare.security.RequirePermission("room:manage")
    public Result<Void> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        room.setId(id);
        roomService.saveRoom(room);
        return Result.ok();
    }

    @PutMapping("/rooms/{id}/status")
    @com.petcare.security.RequirePermission("room:status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        roomService.changeRoomStatus(id, body.get("status"));
        return Result.ok();
    }

    /** 批量创建房间：{ roomTypeId, floor, startNo, count } → 返回创建的房间号列表（管理员选楼层/房号建房） */
    @PostMapping("/rooms/batch")
    @com.petcare.security.RequirePermission("room:manage")
    public Result<List<String>> batchCreate(@RequestBody Map<String, Object> body) {
        return Result.ok(roomService.batchCreateRooms(
                com.petcare.common.Params.requireLong(body, "roomTypeId", "房型"),
                com.petcare.common.Params.intOrNull(body, "floor", "楼层"),
                com.petcare.common.Params.str(body, "startNo"),
                com.petcare.common.Params.intOrNull(body, "count", "数量")));
    }

    @DeleteMapping("/rooms/{id}")
    @com.petcare.security.RequirePermission("room:manage")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return Result.ok();
    }

    /** 批量删除房间：{ ids: [1,2,3] } → 返回 { deleted: [...], skipped: [...] }（非空闲房间自动跳过） */
    @PostMapping("/rooms/batch-delete")
    @com.petcare.security.RequirePermission("room:manage")
    public Result<Map<String, Object>> batchDelete(@RequestBody Map<String, Object> body) {
        List<Long> ids = new java.util.ArrayList<>();
        Object raw = body.get("ids");
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) ids.add(Long.valueOf(o.toString()));
            }
        }
        return Result.ok(roomService.deleteRooms(ids));
    }

    // ---------- 服务 ----------
    @GetMapping("/services")
    public Result<List<ServiceItem>> services(@RequestParam(defaultValue = "false") boolean all) {
        return Result.ok(roomService.services(!all));
    }

    @PostMapping("/services")
    public Result<Void> addService(@RequestBody ServiceItem svc) {
        roomService.saveService(svc);
        return Result.ok();
    }

    @PutMapping("/services/{id}")
    public Result<Void> updateService(@PathVariable Long id, @RequestBody ServiceItem svc) {
        svc.setId(id);
        roomService.saveService(svc);
        return Result.ok();
    }

    @DeleteMapping("/services/{id}")
    public Result<Void> deleteService(@PathVariable Long id) {
        roomService.deleteService(id);
        return Result.ok();
    }
}
