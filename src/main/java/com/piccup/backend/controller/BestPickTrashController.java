package com.piccup.backend.controller;

import com.piccup.backend.dto.BestPickRequest;
import com.piccup.backend.dto.BestPickResponse;
import com.piccup.backend.service.BestPickTrashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/best-picks")
@RequiredArgsConstructor
public class BestPickTrashController {

    private final BestPickTrashService bestPickTrashService;

    @PostMapping("/delete")
    public ResponseEntity<BestPickResponse.Delete> softDelete(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @RequestBody @Valid BestPickRequest.Ids request) {
        return ResponseEntity.ok(bestPickTrashService.softDelete(userId, request.ids()));
    }

    @GetMapping("/trash")
    public ResponseEntity<List<BestPickResponse.Trash>> getTrash(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId) {
        return ResponseEntity.ok(bestPickTrashService.getTrash(userId));
    }

    @PostMapping("/trash/restore")
    public ResponseEntity<BestPickResponse.Restore> restore(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @RequestBody @Valid BestPickRequest.Ids request) {
        return ResponseEntity.ok(bestPickTrashService.restore(userId, request.ids()));
    }

    @PostMapping("/trash/permanent")
    public ResponseEntity<BestPickResponse.Purge> purge(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @RequestBody @Valid BestPickRequest.Ids request) {
        return ResponseEntity.ok(bestPickTrashService.purge(userId, request.ids()));
    }
}
