package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Material 3 Expressive Calculator core logic.
 * Handles arithmetic operations with BigDecimal for precision.
 */
public final class Calculator {

    public enum Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE, NONE
    }

    private BigDecimal displayValue = BigDecimal.ZERO;
    private BigDecimal previousValue = BigDecimal.ZERO;
    private Operation currentOperation = Operation.NONE;
    private boolean justEvaluated = false;
    private boolean hasDecimal = false;
    private boolean hasInput = false;

    /**
     * Appends a digit to the display value.
     */
    public void appendDigit(int digit) {
        if (justEvaluated) {
            reset();
            justEvaluated = false;
        }
        if (digit < 0 || digit > 9) return;

        if (!hasInput) {
            displayValue = BigDecimal.valueOf(digit);
            hasInput = true;
            hasDecimal = false;
        } else {
            // Prevent extremely long numbers
            if (displayValue.toString().replace("-", "").replace(".", "").length() >= 15) {
                return;
            }
            displayValue = new BigDecimal(displayValue.toString() + digit);
        }
    }

    /**
     * Appends a decimal point to the display value.
     */
    public void appendDecimal() {
        if (justEvaluated) {
            reset();
            justEvaluated = false;
        }
        if (!hasDecimal) {
            if (!hasInput) {
                displayValue = BigDecimal.ZERO;
                hasInput = true;
            }
            displayValue = new BigDecimal(displayValue.toString() + ".");
            hasDecimal = true;
        }
    }

    /**
     * Toggles the sign of the display value.
     */
    public void toggleSign() {
        displayValue = displayValue.negate();
    }

    /**
     * Applies a percentage operation to the display value.
     */
    public void applyPercent() {
        displayValue = displayValue.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }

    /**
     * Sets the current operation for the next calculation.
     */
    public void setOperation(Operation op) {
        if (currentOperation != Operation.NONE && hasInput && !justEvaluated) {
            compute();
        }
        previousValue = displayValue;
        currentOperation = op;
        justEvaluated = false;
        hasInput = false;
        hasDecimal = false;
    }

    /**
     * Computes the result of the current operation.
     */
    public void compute() {
        if (currentOperation == Operation.NONE || !hasInput) return;

        BigDecimal result;
        switch (currentOperation) {
            case ADD:
                result = previousValue.add(displayValue);
                break;
            case SUBTRACT:
                result = previousValue.subtract(displayValue);
                break;
            case MULTIPLY:
                result = previousValue.multiply(displayValue);
                break;
            case DIVIDE:
                if (displayValue.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                result = previousValue.divide(displayValue, 10, RoundingMode.HALF_UP);
                break;
            default:
                return;
        }

        // Round to 10 decimal places to avoid floating point artifacts
        result = result.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();

        displayValue = result;
        previousValue = result;
        currentOperation = Operation.NONE;
        justEvaluated = true;
        hasInput = true;
        hasDecimal = result.toString().contains(".");
    }

    /**
     * Clears all state.
     */
    public void clear() {
        reset();
    }

    private void reset() {
        displayValue = BigDecimal.ZERO;
        previousValue = BigDecimal.ZERO;
        currentOperation = Operation.NONE;
        justEvaluated = false;
        hasDecimal = false;
        hasInput = false;
    }

    /**
     * Deletes the last digit (backspace).
     */
    public void backspace() {
        if (justEvaluated) {
            reset();
            return;
        }
        String str = displayValue.toString();
        if (str.length() <= 1 || (str.length() == 2 && str.startsWith("-"))) {
            displayValue = BigDecimal.ZERO;
            hasInput = false;
            hasDecimal = false;
            return;
        }
        if (str.endsWith(".")) {
            str = str.substring(0, str.length() - 2);
            hasDecimal = false;
        } else {
            str = str.substring(0, str.length() - 1);
        }
        displayValue = new BigDecimal(str);
        if (displayValue.compareTo(BigDecimal.ZERO) == 0 && !str.contains(".")) {
            hasInput = false;
        }
    }

    /**
     * Formats the display value for display.
     */
    public String getDisplayText() {
        String text = displayValue.toPlainString();
        // Remove trailing zeros after decimal point
        if (text.contains(".")) {
            text = text.replaceAll("0*$", "");
            if (text.endsWith(".")) {
                text = text.substring(0, text.length() - 1);
            }
        }
        // Limit display length
        if (text.length() > 15) {
            text = String.format("%.6e", displayValue.doubleValue());
        }
        return text.isEmpty() ? "0" : text;
    }

    /**
     * Returns the current operation for UI display.
     */
    public Operation getCurrentOperation() {
        return currentOperation;
    }

    /**
     * Returns true if the calculator has been used (has input).
     */
    public boolean hasInput() {
        return hasInput;
    }

    // Legacy methods for backward compatibility
    public int add(int a, int b) {
        return a + b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }
}
