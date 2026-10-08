package com.petcare.story.controller;

import com.petcare.common.Result;
import com.petcare.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @GetMapping("/stories")
    public Result<List<Map<String, Object>>> published() {
        return Result.ok(storyService.listPublished());
    }

    @PostMapping("/stories")
    public Result<Void> publish(@RequestBody Map<String, Object> body) {
        storyService.publish(body);
        return Result.ok();
    }

    @PostMapping("/stories/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        storyService.like(id);
        return Result.ok();
    }

    @GetMapping("/admin/stories")
    @PreAuthorize("hasRole('ADMIN')")
    @com.petcare.security.RequirePermission("story:manage")
    public Result<List<Map<String, Object>>> all() {
        return Result.ok(storyService.listAll());
    }

    @PutMapping("/admin/stories/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @com.petcare.security.RequirePermission("story:manage")
    public Result<Void> toggle(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        storyService.toggleStatus(id, body.get("status"));
        return Result.ok();
    }
}
