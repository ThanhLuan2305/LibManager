package com.project.libmanager.specification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.project.libmanager.criteria.BookCriteria;
import com.project.libmanager.entity.Book;
import com.project.libmanager.entity.BookType_;
import com.project.libmanager.entity.Book_;
import com.project.libmanager.repository.BookRepository;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import tech.jhipster.service.QueryService;

@Service
@RequiredArgsConstructor
public class BookQueryService extends QueryService<Book> {
    private final BookRepository bookRepository;

    public Page<Book> findByCriteria(BookCriteria criteria, Pageable pageable) {
        Specification<Book> specification = createSpecification(criteria);
        return bookRepository.findAll(specification, pageable);
    }

    private Specification<Book> createSpecification(BookCriteria criteria) {
        Specification<Book> specification = Specification.where(null);

        if (criteria.getIsbn() != null) {
            specification = specification.and(buildStringSpecification(criteria.getIsbn(), Book_.isbn));
        }
        if (criteria.getTitle() != null) {
            specification = specification.and(buildStringSpecification(criteria.getTitle(), Book_.title));
        }
        if (criteria.getAuthor() != null) {
            specification = specification.and(buildStringSpecification(criteria.getAuthor(), Book_.author));
        }
        if (criteria.getTypeName() != null) {
            specification = specification.and(buildSpecification(criteria.getTypeName(),
                    root -> root.join(Book_.type, JoinType.INNER).get(BookType_.name)));
        }
        if (criteria.getStock() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getStock(), Book_.stock));
        }
        if (criteria.getPublisher() != null) {
            specification = specification.and(buildStringSpecification(criteria.getPublisher(), Book_.publisher));
        }
        if (criteria.getPublishedDate() != null) {
            specification = specification
                    .and(buildRangeSpecification(criteria.getPublishedDate(), Book_.publishedDate));
        }
        if (criteria.getMaxBorrowDays() != null) {
            specification = specification
                    .and(buildRangeSpecification(criteria.getMaxBorrowDays(), Book_.maxBorrowDays));
        }
        if (criteria.getLocation() != null) {
            specification = specification.and(buildStringSpecification(criteria.getLocation(), Book_.location));
        }

        return specification;
    }
}
