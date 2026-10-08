package com.pawstay.controller;

import com.pawstay.common.Result;
import com.pawstay.entity.Message;
import com.pawstay.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 站内消息通知 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public Result<List<Message>> list() {
        return Result.ok(messageService.myMessages());
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(messageService.unreadCount());
    }

    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        messageService.read(id);
        return Result.ok();
    }

    @PostMapping("/read-all")
    public Result<Void> readAll() {
        messageService.readAll();
        return Result.ok();
    }
}
