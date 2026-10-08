package com.petcare.exception.controller;

import com.petcare.common.Result;
import com.petcare.exception.service.PetExceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PetExceptionController {

    private final PetExceptionService exceptionService;

    @PostMapping("/exceptions")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public Result<Void> report(@RequestBody Map<String, Object> body) {
        exceptionService.report(
                com.petcare.common.Params.requireLong(body, "petId", "宠物"),
                com.petcare.common.Params.longOrNull(body, "orderId", "订单"),
                com.petcare.common.Params.str(body, "type"),
                com.petcare.common.Params.str(body, "level"),
                com.petcare.common.Params.str(body, "description"),
                com.petcare.common.Params.str(body, "photos"));
        return Result.ok();
    }

    @GetMapping("/exceptions")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "ALL") String status) {
        return Result.ok(exceptionService.list(status));
    }

    @PostMapping("/exceptions/{id}/process")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public Result<Void> process(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        exceptionService.transition(id, "PROCESSING", body == null ? null : body.get("note"));
        return Result.ok();
    }

    @PostMapping("/exceptions/{id}/observe")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public Result<Void> observe(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        exceptionService.transition(id, "OBSERVING", body == null ? null : body.get("note"));
        return Result.ok();
    }

    @PostMapping("/exceptions/{id}/resolve")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public Result<Void> resolve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        exceptionService.transition(id, "RESOLVED", body == null ? null : body.get("note"));
        return Result.ok();
    }

    @PostMapping("/exceptions/{id}/close")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public Result<Void> close(@PathVariable Long id) {
        exceptionService.transition(id, "CLOSED", null);
        return Result.ok();
    }
}
