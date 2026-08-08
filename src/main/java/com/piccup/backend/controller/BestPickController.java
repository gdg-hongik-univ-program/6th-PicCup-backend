package com.piccup.backend.controller;

import com.piccup.backend.dto.BestPickResponse;
import com.piccup.backend.service.BestPickService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.YearMonth;
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
            @RequestParam("yearMonth") @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {

        return ResponseEntity.ok(bestPickService.getCalendar(userId, yearMonth));
    }


    @GetMapping("/{id}")
    public ResponseEntity<BestPickResponse.Detail> getBestPickDetail(
            @SessionAttribute(name = "LOGIN_USER_ID") Long userId,
            @PathVariable("id") Long id) {

        BestPickResponse.Detail res = bestPickService.getBestPickDetail(userId, id);
        return ResponseEntity.ok(res);
    }
}