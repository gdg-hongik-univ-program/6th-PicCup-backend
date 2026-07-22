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

@RestController
@RequestMapping("/api/best-picks")
@RequiredArgsConstructor
public class BestPickController {

    private final BestPickService bestPickService;

    @PostMapping
    public ResponseEntity<BestPickResponse> upload(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("capturedDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate capturedDate,
            @RequestParam("candidateCount")
            int candidateCount) {

        BestPickResponse res =
                bestPickService.upload(userId, file, categoryId, capturedDate, candidateCount);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
