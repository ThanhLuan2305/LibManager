package com.project.libmanager.specification;

import org.springframework.stereotype.Service;

import com.project.libmanager.criteria.UserCriteria;
import com.project.libmanager.entity.User;
import com.project.libmanager.repository.UserRepository;

import jakarta.persistence.criteria.JoinType;

import com.project.libmanager.entity.User_;
import com.project.libmanager.entity.Borrowing_;
import com.project.libmanager.entity.Book_;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import tech.jhipster.service.QueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserQueryService extends QueryService<User> {
    private final UserRepository userRepository;

    public Page<User> findByCriteria(UserCriteria criteria, Pageable pageable) {
        Specification<User> specification = createSpecification(criteria);
        return userRepository.findAll(specification, pageable);
    }

    private Specification<User> createSpecification(UserCriteria criteria) {
        Specification<User> specification = Specification.where(null);
        if (criteria.getEmail() != null) {
            specification = specification.and(buildStringSpecification(criteria.getEmail(), User_.email));
        }

        if (criteria.getFullName() != null) {
            specification = specification.and(buildStringSpecification(criteria.getFullName(), User_.fullName));
        }

        if (criteria.getBirthDate() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getBirthDate(), User_.birthDate));
        }

        if (criteria.getIsVerified() != null) {
            specification = specification.and(buildSpecification(criteria.getIsVerified(), User_.verified));
        }

        if (criteria.getIsDeleted() != null) {
            specification = specification.and(buildSpecification(criteria.getIsDeleted(), User_.deleted));
        }

        if (criteria.getLateReturnCount() != null) {
            specification = specification
                    .and(buildRangeSpecification(criteria.getLateReturnCount(), User_.lateReturnCount));
        }

        if (criteria.getBookIsbn() != null) {
            specification = specification.and(buildSpecification(criteria.getBookIsbn(),
                    root -> root.join(User_.borrowings, JoinType.INNER)
                            .join(Borrowing_.book, JoinType.INNER)
                            .get(Book_.isbn)));
        }

        if (criteria.getBookTitle() != null) {
            specification = specification.and(buildSpecification(criteria.getBookTitle(),
                    root -> root.join(User_.borrowings, JoinType.INNER)
                            .join(Borrowing_.book, JoinType.INNER)
                            .get(Book_.title)));
        }

        if (criteria.getBorrowDate() != null) {
            specification = specification.and(buildSpecification(criteria.getBorrowDate(),
                    root -> root.join(User_.borrowings, JoinType.INNER).get(Borrowing_.borrowDate)));
        }

        if (criteria.getReturnDate() != null) {
            specification = specification.and(buildSpecification(criteria.getReturnDate(),
                    root -> root.join(User_.borrowings, JoinType.INNER).get(Borrowing_.returnDate)));
        }

        return specification;
    }
}
