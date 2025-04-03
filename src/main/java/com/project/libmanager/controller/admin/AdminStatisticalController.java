package com.project.libmanager.controller.admin;

import com.project.libmanager.service.IStatisticalService;
import com.project.libmanager.service.dto.response.ApiResponse;
import com.project.libmanager.service.dto.response.BookResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("admin/statistical")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "JWT Authentication")
@Tag(name = "Admin Statistical Management", description = "Endpoints for managing statisical by admin users")
public class AdminStatisticalController {
    private final IStatisticalService statisticalService;

    @GetMapping("/count-book-active")
    public ResponseEntity<ApiResponse<Long>> countBookActive() {
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .message("Count Book Active Success !!!")
                .result(statisticalService.countBookActive())
                .build());
    }

    @GetMapping("/count-all-borrow-book")
    public ResponseEntity<ApiResponse<Long>> countAllBorrowBookActive() {
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .message("Count All Borrow Book Success !!!")
                .result(statisticalService.countAllBorrowBoook())
                .build());
    }

    @GetMapping("/count-borrow-book-active")
    public ResponseEntity<ApiResponse<Long>> countBorrowBookActive() {
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .message("Count Borrow Book Active Success !!!")
                .result(statisticalService.countBorrowBoookActive())
                .build());
    }

    @GetMapping("/count-user-active")
    public ResponseEntity<ApiResponse<Long>> countUserActive() {
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .message("Count User Active Success !!!")
                .result(statisticalService.countUserActive())
                .build());
    }

    @GetMapping("/get-book-recent")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getBookRecent(@RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.<List<BookResponse>>builder()
                .message("Get Book Recent Success !!!")
                .result(statisticalService.getNewBook(quantity))
                .build());
    }

    @GetMapping("/get-borrow-trend")
    public ResponseEntity<ApiResponse<Map<Integer, Integer>>> countBorrowForMonthOfYear(@RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.<Map<Integer, Integer>>builder()
                .message("Get Borrow Trend Success !!!")
                .result(statisticalService.countBorrowForMonthOfYear(year))
                .build());
    }
}
