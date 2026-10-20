package com.example;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CalculatorTest {

    private final Calculator calc = new Calculator();

    @Test
    public void addsTwoNumbers() {
        assertEquals(5, calc.add(2, 3));
    }

    @Test
    public void multipliesTwoNumbers() {
        assertEquals(12, calc.multiply(3, 4));
    }
}
