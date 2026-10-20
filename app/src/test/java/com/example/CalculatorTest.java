package com.example;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CalculatorTest {

    @Test
    public void legacyIntegerOperationsRemainCompatible() {
        Calculator calc = new Calculator();
        assertEquals(5, calc.add(2, 3));
        assertEquals(12, calc.multiply(3, 4));
    }

    @Test
    public void decimalEntryPreservesTrailingZeroAndDecimalPoint() {
        Calculator calc = new Calculator();
        calc.appendDigit(1);
        calc.appendDecimal();
        calc.appendDigit(2);
        calc.appendDigit(0);
        assertEquals("1.20", calc.getDisplayText());
        calc.backspace();
        assertEquals("1.2", calc.getDisplayText());
        calc.backspace();
        assertEquals("1.", calc.getDisplayText());
        calc.backspace();
        assertEquals("1", calc.getDisplayText());
    }

    @Test
    public void decimalStartsWithZeroAndBackspaceDoesNotCreateInvalidNumber() {
        Calculator calc = new Calculator();
        calc.appendDecimal();
        assertEquals("0.", calc.getDisplayText());
        calc.backspace();
        assertEquals("0", calc.getDisplayText());
        calc.appendDigit(7);
        assertEquals("7", calc.getDisplayText());
    }

    @Test
    public void signTogglesRawInput() {
        Calculator calc = new Calculator();
        calc.appendDigit(5);
        calc.toggleSign();
        assertEquals("-5", calc.getDisplayText());
        calc.toggleSign();
        assertEquals("5", calc.getDisplayText());
    }

    @Test
    public void percentUsesCurrentInputAndKeepsSign() {
        Calculator calc = new Calculator();
        calc.appendDigit(5);
        calc.appendDigit(0);
        calc.applyPercent();
        assertEquals("0.5", calc.getDisplayText());
        calc.toggleSign();
        calc.applyPercent();
        assertEquals("-0.005", calc.getDisplayText());
    }

    @Test
    public void chainedOperationsComputeIntermediateResult() {
        Calculator calc = new Calculator();
        calc.appendDigit(2);
        calc.setOperation(Calculator.Operation.ADD);
        calc.appendDigit(3);
        calc.setOperation(Calculator.Operation.MULTIPLY);
        assertEquals(Calculator.Operation.MULTIPLY, calc.getCurrentOperation());
        calc.appendDigit(4);
        calc.compute();
        assertEquals("20", calc.getDisplayText());
        assertTrue(calc.hasInput());
    }

    @Test
    public void computeSupportsAllOperationsAndDecimalResults() {
        Calculator calc = new Calculator();
        calc.appendDigit(7);
        calc.setOperation(Calculator.Operation.SUBTRACT);
        calc.appendDigit(2);
        calc.compute();
        assertEquals("5", calc.getDisplayText());

        calc.clear();
        calc.appendDigit(3);
        calc.setOperation(Calculator.Operation.MULTIPLY);
        calc.appendDigit(2);
        calc.compute();
        assertEquals("6", calc.getDisplayText());

        calc.clear();
        calc.appendDigit(1);
        calc.setOperation(Calculator.Operation.DIVIDE);
        calc.appendDigit(8);
        calc.compute();
        assertEquals("0.125", calc.getDisplayText());
    }

    @Test(expected = ArithmeticException.class)
    public void divisionByZeroThrowsWithoutReturningInfinity() {
        Calculator calc = new Calculator();
        calc.appendDigit(9);
        calc.setOperation(Calculator.Operation.DIVIDE);
        calc.appendDigit(0);
        calc.compute();
    }

    @Test
    public void invalidDigitsAreIgnored() {
        Calculator calc = new Calculator();
        calc.appendDigit(-1);
        calc.appendDigit(10);
        assertEquals("0", calc.getDisplayText());
        assertFalse(calc.hasInput());
    }

    @Test
    public void calculatorCanBeSerializedWithInputState() throws Exception {
        Calculator calc = new Calculator();
        calc.appendDigit(4);
        calc.appendDecimal();
        calc.appendDigit(5);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ObjectOutputStream(bytes).writeObject(calc);
        Calculator restored = (Calculator) new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray())).readObject();

        assertEquals("4.5", restored.getDisplayText());
        assertTrue(restored.hasInput());
    }
}
