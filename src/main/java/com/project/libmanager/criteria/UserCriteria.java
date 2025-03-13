package com.project.libmanager.criteria;

import lombok.Data;
import tech.jhipster.service.filter.InstantFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.StringFilter;

@Data
public class UserCriteria {
    private StringFilter email;

    private StringFilter fullName;

    private InstantFilter birthDate;

    private IntegerFilter lateReturnCount;

    private StringFilter bookIsbn;

    private StringFilter bookTitle;

    private InstantFilter borrowDate;
    
    private InstantFilter returnDate;
}
