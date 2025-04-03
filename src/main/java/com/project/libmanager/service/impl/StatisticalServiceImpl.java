package com.project.libmanager.service.impl;

import com.project.libmanager.constant.ErrorCode;
import com.project.libmanager.entity.Book;
import com.project.libmanager.exception.AppException;
import com.project.libmanager.repository.BookRepository;
import com.project.libmanager.repository.BorrowingRepository;
import com.project.libmanager.repository.UserRepository;
import com.project.libmanager.service.IStatisticalService;
import com.project.libmanager.service.dto.response.BookResponse;
import com.project.libmanager.service.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticalServiceImpl implements IStatisticalService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final BookMapper bookMapper;

    @Override
    public long countUserActive() {
        return userRepository.countVerifiedUsersWithUserRole();
    }

    @Override
    public long countBookActive() {
        return bookRepository.countBookActive();
    }

    @Override
    public long countBorrowBoookActive() {
        return borrowingRepository.countBorrowByReturnDateIsNull();
    }

    @Override
    public long countAllBorrowBoook() {
        return borrowingRepository.countBorrow();
    }

    @Override
    public Map<Integer, Integer> countBorrowForMonthOfYear(int year) {
        //Get the number of books per month
        List<Object[]> results = borrowingRepository.countBorrowingsByMonth(year);
        Map<Integer, Integer> borrowStats = new HashMap<>();

        //Loop results to get object add into borrowStats
        for (Object[] row : results) {
            Integer month = ((Number) row[0]).intValue();
            Integer total = ((Number) row[1]).intValue();
            borrowStats.put(month, total);
        }

        return borrowStats;
    }

    @Override
    public List<BookResponse> getNewBook(int quantity) {
        List<Book> lstBookRecent = (bookRepository.findRecentBooks(quantity));
        return lstBookRecent.stream().map(bookMapper::toBookResponse).toList();
    }
}
