package com.project.libmanager.service.impl;

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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticalServiceImpl implements IStatisticalService {
    private final UserRepository userRepository;                // Repository for user data persistence
    private final BookRepository bookRepository;                // Repository for book data
    private final BorrowingRepository borrowingRepository;      // Repository for borrow data persistence
    private final BookMapper bookMapper;                        // Mapper for entity-DTO conversion

    /**
     * Counts the number of active users with verified status and user role.
     *
     * @return the total count of active users
     * @implNote This method queries the user repository to count users who are verified
     * and have a user role assigned.
     */
    @Override
    public long countUserActive() {
        return userRepository.countVerifiedUsersWithUserRole();
    }

    /**
     * Counts the number of active books in the library system.
     *
     * @return the total count of active books
     * @implNote This method queries the book repository to count all books marked as active.
     */
    @Override
    public long countBookActive() {
        return bookRepository.countBookActive();
    }

    /**
     * Counts the number of currently borrowed books that have not been returned.
     *
     * @return the total count of active borrowings with no return date
     * @implNote This method queries the borrowing repository to count borrowings where
     * the return date is null.
     */
    @Override
    public long countBorrowBoookActive() {
        return borrowingRepository.countBorrowByReturnDateIsNull();
    }

    /**
     * Counts the total number of all borrowing records in the system.
     *
     * @return the total count of all borrowings, regardless of status
     * @implNote This method queries the borrowing repository to count all borrowing records.
     */
    @Override
    public long countAllBorrowBoook() {
        return borrowingRepository.countBorrow();
    }

    /**
     * Retrieves borrowing statistics for each month of a specified year.
     *
     * @param year the year for which to retrieve borrowing statistics
     * @return a {@link Map} where the key is the month (1-12) and the value is the total
     * number of borrowings in that month
     * @implNote This method queries the borrowing repository for monthly borrowing counts,
     * processes the results into a map, and returns it. The map only includes months with
     * borrowing activity.
     */
    @Override
    public Map<Integer, Integer> countBorrowForMonthOfYear(int year) {
        // Get the number of books borrowed per month
        List<Object[]> results = borrowingRepository.countBorrowingsByMonth(year);
        Map<Integer, Integer> borrowStats = new HashMap<>();

        // Loop through results to populate the borrowStats map
        for (Object[] row : results) {
            Integer month = ((Number) row[0]).intValue();
            Integer total = ((Number) row[1]).intValue();
            borrowStats.put(month, total);
        }

        return borrowStats;
    }

    /**
     * Retrieves a list of the most recently added books.
     *
     * @param quantity the number of recent books to retrieve
     * @return a {@link List} of {@link BookResponse} objects representing the recent books
     * @throws AppException if no books are found or an error occurs during mapping
     * @implNote This method queries the book repository for the specified number of recent books,
     * maps them to BookResponse DTOs using the BookMapper, and returns the list.
     */
    @Override
    public List<BookResponse> getNewBook(int quantity) {
        List<Book> lstBookRecent = bookRepository.findRecentBooks(quantity);
        return lstBookRecent.stream().map(bookMapper::toBookResponse).toList();
    }
}