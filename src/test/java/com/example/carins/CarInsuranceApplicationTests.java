package com.example.carins;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.service.CarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;

    @Test
    void validInsuranceReturnsTrueOrFalse() {
        assertTrue(service.isInsuranceValid(1L, "2024-06-01"));
        assertTrue(service.isInsuranceValid(1L, "2025-06-01"));
        assertFalse(service.isInsuranceValid(2L, "2025-02-01"));
    }

    @Test
    void throwsNotFoundForNonExistentCar() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(999L, "2025-01-01"));
        assertEquals("Car with id 999 not found", ex.getReason());
    }

    @Test
    void throwsBadRequestForNullOrEmptyDate() {
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, null));
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, ""));
        assertEquals("carId and date must not be null or empty", ex1.getReason());
        assertEquals("carId and date must not be null or empty", ex2.getReason());
    }

    @Test
    void throwsBadRequestForInvalidDateFormat() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, "2025/01/01"));
        assertEquals("Invalid date format. Expected ISO format: YYYY-MM-DD", ex.getReason());
    }

    @Test
    void throwsBadRequestForOutOfRangeDate() {
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, "1800-01-01"));
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> service.isInsuranceValid(1L, "2200-01-01"));
        assertEquals("Date is outside supported range (1900–2100)", ex1.getReason());
        assertEquals("Date is outside supported range (1900–2100)", ex2.getReason());
    }
}
