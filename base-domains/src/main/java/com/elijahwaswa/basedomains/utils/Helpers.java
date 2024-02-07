package com.elijahwaswa.basedomains.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.List;

public class Helpers {
    public static Pageable buildPageable(int pageNumber, int pageSize) {
        return buildPageable( pageNumber,  pageSize, new ArrayList<>());
    }
    public static Pageable buildPageable(int pageNumber, int pageSize, List<Order> orders) {
        if (!orders.isEmpty()) {
            return PageRequest.of(pageNumber, pageSize, Sort.by(orders));
        } else {
            return PageRequest.of(pageNumber, pageSize);
        }
    }
}
