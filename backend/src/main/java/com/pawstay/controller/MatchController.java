package com.pawstay.controller;

import com.pawstay.common.Result;
import com.pawstay.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** 智能排笼：根据宠物档案与日期推荐可订房型（创新点） */
@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/rooms")
    public Result<List<Map<String, Object>>> match(@RequestParam Long petId,
                                                   @RequestParam Long merchantId,
                                                   @RequestParam LocalDate start,
                                                   @RequestParam LocalDate end) {
        return Result.ok(roomService.matchRooms(petId, merchantId, start, end));
    }
}
