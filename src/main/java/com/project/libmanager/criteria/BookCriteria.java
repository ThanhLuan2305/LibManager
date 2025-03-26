package com.project.libmanager.criteria;

import lombok.Data;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.StringFilter;

@Data
public class BookCriteria {
    private StringFilter isbn;

    private StringFilter title;

    private StringFilter author;

    private StringFilter typeName;

    private IntegerFilter stock;  

    private StringFilter publisher;

    private InstantFilter publishedDate;

    private IntegerFilter maxBorrowDays;

    private StringFilter location;

    private BooleanFilter deleted;
}
