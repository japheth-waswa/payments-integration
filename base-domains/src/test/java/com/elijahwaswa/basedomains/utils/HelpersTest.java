package com.elijahwaswa.basedomains.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HelpersTest {

    @Test
    void incrementString() {
        assertEquals("ghj",Helpers.incrementString("ghi"));
        assertEquals("abd",Helpers.incrementString("abc"));
        assertEquals("abz",Helpers.incrementString("aby"));
        assertEquals("aca",Helpers.incrementString("abz"));
        assertEquals("aaaa",Helpers.incrementString("zzz"));
        assertEquals("aaab",Helpers.incrementString("aaaa"));
        assertEquals("ad0a",Helpers.incrementString("ac9z"));
        assertEquals("ada0",Helpers.incrementString("acz9"));
        assertEquals("0000",Helpers.incrementString("999"));
        assertEquals("00aa",Helpers.incrementString("9zz"));
        assertEquals("0001",Helpers.incrementString("0000"));
    }
}