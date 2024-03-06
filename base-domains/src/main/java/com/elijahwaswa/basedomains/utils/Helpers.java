package com.elijahwaswa.basedomains.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Helpers {

    private Helpers() {
    }

    public static Pageable buildPageable(int pageNumber, int pageSize) {
        return buildPageable(pageNumber, pageSize, new ArrayList<>());
    }

    public static Pageable buildPageable(int pageNumber, int pageSize, List<Order> orders) {
        if (!orders.isEmpty()) {
            return PageRequest.of(pageNumber, pageSize, Sort.by(orders));
        } else {
            return PageRequest.of(pageNumber, pageSize);
        }
    }

    public static String incrementString(String input) {
        char[] charArray = input.toCharArray();
        boolean elementZeroToInit = false;

        //start from the last char
        for (int i = charArray.length - 1; i >= 0; i--) {
            char currentChar = charArray[i];
            if (Character.isDigit(currentChar)) {
                // If it's a digit, increment it if less than '9', otherwise set to '0'
                if (currentChar < '9') {
                    charArray[i] = (char) (currentChar + 1);
                    break;
                } else {
                    charArray[i] = '0';
                    elementZeroToInit = i == 0;
                }
            } else if (Character.isLetter(currentChar)) {
                // If it's a letter, handle 'z' and 'Z' separately
                if (currentChar == 'z') {
                    charArray[i] = 'a';
                    elementZeroToInit = i == 0;
                } else if (currentChar == 'Z') {
                    charArray[i] = 'A';
                    elementZeroToInit = i == 0;
                } else {
                    // Increment the letter and break the loop
                    charArray[i] = (char) (currentChar + 1);
                    break;
                }
            }
        }

        // If the first character is 'a' or 'A','0' add a new 'a' or 'A' or '0' to the beginning
        if (elementZeroToInit && (charArray[0] == 'a' || charArray[0] == 'A' || charArray[0] == '0')) {
            charArray = Arrays.copyOf(charArray, charArray.length + 1);
            System.arraycopy(charArray, 0, charArray, 1, charArray.length - 1);
        }

        return new String(charArray);
    }

}
