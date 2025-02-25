package com.project.LibManager.criteria;

import lombok.Data;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LocalDateFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

@Data
public class UserCriteria {
    private LongFilter id;
    private StringFilter email;
    private StringFilter fullName;
    private LocalDateFilter birthDate;
    private BooleanFilter isVerified;
    private BooleanFilter isDeleted;
    private IntegerFilter lateReturnCount;
    private StringFilter bookIsbn;
    private StringFilter bookTitle;
    private LocalDateFilter borrowDate;
    private LocalDateFilter returnDate;
}
