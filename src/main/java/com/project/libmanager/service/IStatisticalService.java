package com.project.libmanager.service;

import com.project.libmanager.service.dto.response.BookResponse;

import java.util.List;
import java.util.Map;

public interface IStatisticalService {
    long countUserActive();

    long countBookActive();

    long countBorrowBoookActive();

    long countAllBorrowBoook();

    Map<Integer, Integer> countBorrowForMonthOfYear(int year);

    List<BookResponse> getNewBook(int quantity);
}
