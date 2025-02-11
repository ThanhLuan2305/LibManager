package com.project.LibManager.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.project.LibManager.entity.Book;
import com.project.LibManager.entity.BookType;
import com.project.LibManager.entity.Borrowing;
import com.project.LibManager.entity.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class BookSpecification {
    public static Specification<Book> filterBooks(String title, String author, String typeName, String publisher, LocalDate publishedDateFrom, LocalDate publishedDateTo, int maxBorrowDays, String location, String nameUserBrrow) {
        return (root, query, criteriaBuilder) -> {
            Specification<Book> spec = Specification.where(null);
            
            if (title != null && !title.isEmpty()) {
                spec = spec.and((bookRoot, userQuery, cb) -> 
                cb.like(bookRoot.get("title"), "%" + title + "%"));
            }

            if (author != null && !author.isEmpty()) {
                spec = spec.and((bookRoot, userQuery, cb) -> 
                cb.like(bookRoot.get("author"), "%" + author + "%"));
            }

            if (typeName != null && !typeName.isEmpty()) {
                spec = spec.and((bookRoot, userQuery, cb) -> {
                    Join<Book, BookType> bookTypeJoin = bookRoot.join("type", JoinType.LEFT);
                    return cb.like(bookTypeJoin.get("name"), "%" + typeName + "%");
                });
            }
            
            if (publisher != null && !publisher.isEmpty()) {
                spec = spec.and((bookRoot, userQuery, cb) -> 
                cb.like(bookRoot.get("publisher"), "%" + publisher + "%"));
            }

            if (location != null && !location.isEmpty()) {
                spec = spec.and((bookRoot, userQuery, cb) -> 
                cb.like(bookRoot.get("location"), "%" + location + "%"));
            }

            if (nameUserBrrow != null && !nameUserBrrow.isEmpty()) {
                spec = spec.and((bookRoot, userQuery, cb) -> {
                    Join<Book, Borrowing> borrowJoin = bookRoot.join("borrowings", JoinType.LEFT);
                    Join<Borrowing,User> userJoin = borrowJoin.join("user", JoinType.LEFT);
                    return cb.like(userJoin.get("fullName"), "%" + nameUserBrrow + "%");
                });
            }
            
            if (publishedDateFrom != null) {
                spec = spec.and((bookRoot, userQuery, cb) -> 
                cb.greaterThanOrEqualTo(bookRoot.get("publishedDate"), publishedDateFrom));
            }
            
            if (publishedDateTo != null) {
                spec = spec.and((bookRoot, userQuery, cb) -> 
                cb.lessThanOrEqualTo(bookRoot.get("publishedDate"), publishedDateTo));
            }

            if (publishedDateTo != null) {
                spec = spec.and((bookRoot, userQuery, cb) -> 
                cb.lessThanOrEqualTo(bookRoot.get("publishedDate"), publishedDateTo));
            }
            
            if(maxBorrowDays > 0) {
                spec = spec.and((bookRoot, userQuery, cb) -> 
                cb.equal(bookRoot.get("maxBorrowDays"), maxBorrowDays));
            }
            return spec.toPredicate(root, query, criteriaBuilder);
        };
    }
       
}
