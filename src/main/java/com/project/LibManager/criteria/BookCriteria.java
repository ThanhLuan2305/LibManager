package com.project.LibManager.criteria;

import lombok.Data;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LocalDateFilter;
import tech.jhipster.service.filter.StringFilter;

@Data
public class BookCriteria {
    private StringFilter isbn;

    private StringFilter title;

    private StringFilter author;

    private StringFilter typeName;

    private IntegerFilter stock;  

    private StringFilter publisher;

    private LocalDateFilter publishedDate;

    private IntegerFilter maxBorrowDays;

    private StringFilter location;
}
