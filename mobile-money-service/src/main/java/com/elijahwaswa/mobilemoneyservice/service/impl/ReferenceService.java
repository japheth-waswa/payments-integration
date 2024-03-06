package com.elijahwaswa.mobilemoneyservice.service.impl;

import com.elijahwaswa.basedomains.utils.Helpers;
import com.elijahwaswa.mobilemoneyservice.entity.Reference;
import com.elijahwaswa.mobilemoneyservice.repository.ReferenceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
//@AllArgsConstructor
public class ReferenceService {

    @Autowired
    private ReferenceRepository referenceRepository;

    @Autowired
    private EntityManager entityManager;

    @Value("${reference.default}")
    private String defaultRef;

    @Transactional
    public Reference generateReference() {
        Reference reference = entityManager.find(Reference.class, 1, LockModeType.PESSIMISTIC_WRITE);
        if (reference == null) {
            //prepare new reference object
            reference = new Reference();
            reference.setReference(defaultRef);
        } else {
            //increment reference
            reference.setReference(Helpers.incrementString(reference.getReference()));
        }
        return referenceRepository.saveAndFlush(reference);

    }
}
