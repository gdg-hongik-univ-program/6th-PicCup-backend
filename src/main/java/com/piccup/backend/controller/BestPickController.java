package com.piccup.backend.controller;

import com.piccup.backend.dto.BestPickRequest;
import com.piccup.backend.dto.BestPickResponse;
import com.piccup.backend.service.BestPickService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/best-picks")
@RequiredArgsConstructor
public class BestPickController {

    private final BestPickService bestPickService;

    @PostMapping
    public ResponseEntity<BestPickResponse.Upload> upload(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("capturedDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate capturedDate,
            @RequestParam("candidateCount") int candidateCount) {

        BestPickResponse.Upload res =
                bestPickService.upload(userId, file, categoryId, capturedDate, candidateCount);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<BestPickResponse.Calendar>> getCalendar(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @RequestParam("yearMonth") String yearMonth) {

        List<BestPickResponse.Calendar> res = bestPickService.getCalendar(userId, yearMonth);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BestPickResponse.Detail> getBestPickDetail(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @PathVariable("id") Long id) {

        BestPickResponse.Detail res = bestPickService.getBestPickDetail(userId, id);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<List<BestPickResponse.Album>> getBestPicks(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {

        List<BestPickResponse.Album> res = bestPickService.getBestPicks(userId, categoryId);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/move")
    public ResponseEntity<BestPickResponse.MoveResult> moveCategories(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @RequestBody BestPickRequest.MoveCategory request) {

        BestPickResponse.MoveResult res = bestPickService.moveCategories(userId, request);
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/{id}/like")
    public ResponseEntity<BestPickResponse.LikeResult> updateLike(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @PathVariable("id") Long id,
            @RequestBody BestPickRequest.UpdateLike request) {

        BestPickResponse.LikeResult res = bestPickService.updateLike(userId, id, request);
        return ResponseEntity.ok(res);
    }
}