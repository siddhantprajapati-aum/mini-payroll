package com.example.minipayrollsystem.utils;

import com.example.minipayrollsystem.constants.AppConstants;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationUtilTest {

    @Test
    void testBuildPageable_CapsMaxSize() {
        Pageable pageable = PaginationUtil.buildPageable(0, 500);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(AppConstants.MAX_PAGE_SIZE, pageable.getPageSize());
        assertEquals("id: ASC", pageable.getSort().toString());
    }

    @Test
    void testBuildPageable_NormalizesNegativeValues() {
        Pageable pageable = PaginationUtil.buildPageable(-2, 0);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(1, pageable.getPageSize());
        assertEquals("id: ASC", pageable.getSort().toString());
    }
}
