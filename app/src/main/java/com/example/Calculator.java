package com.example;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Material 3 Expressive Calculator core logic. */
public final class Calculator implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_INPUT_DIGITS = 15;

    public enum Operation { ADD, SUBTRACT, MULTIPLY, DIVIDE, NONE }

    private BigDecimal displayValue = BigDecimal.ZERO;
    private BigDecimal previousValue = BigDecimal.ZERO;
    private Operation currentOperation = Operation.NONE;
    private boolean justEvaluated = false;
    private boolean hasDecimal = false;
    private boolean hasInput = false;
    /** Raw editable text. BigDecimal alone cannot preserve 1.20 or 0. */
    private String input = "0";

    public void appendDigit(int digit) {
        if (digit < 0 || digit > 9) return;
        if (justEvaluated) reset();
        if (!hasInput) {
            input = Integer.toString(digit);
            hasInput = true;
            hasDecimal = false;
        } else {
            String candidate = input + digit;
            if (digitCount(candidate) > MAX_INPUT_DIGITS) return;
            if ("0".equals(input)) candidate = Integer.toString(digit);
            else if ("-0".equals(input)) candidate = "-" + digit;
            input = candidate;
            hasDecimal = input.indexOf('.') >= 0;
        }
        syncDisplayValue();
    }

    public void appendDecimal() {
        if (justEvaluated) reset();
        if (hasDecimal) return;
        if (!hasInput) {
            input = "0.";
            hasInput = true;
        } else {
            input += ".";
        }
        hasDecimal = true;
        syncDisplayValue();
    }

    public void toggleSign() {
        if (justEvaluated) justEvaluated = false;
        if (!hasInput) return;
        if (input.startsWith("-")) input = input.substring(1);
        else input = "-" + input;
        syncDisplayValue();
    }

    public void applyPercent() {
        BigDecimal value = currentInputValue();
        input = value.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString();
        hasInput = true;
        hasDecimal = input.indexOf('.') >= 0;
        justEvaluated = false;
        syncDisplayValue();
    }

    public void setOperation(Operation op) {
        if (op == null) return;
        if (currentOperation != Operation.NONE && hasInput && !justEvaluated) compute();
        previousValue = currentInputValue();
        currentOperation = op;
        justEvaluated = false;
        hasInput = false;
        hasDecimal = false;
        input = "0";
        displayValue = previousValue;
    }

    public void compute() {
        if (currentOperation == Operation.NONE || !hasInput) return;
        BigDecimal value = currentInputValue();
        BigDecimal result;
        switch (currentOperation) {
            case ADD: result = previousValue.add(value); break;
            case SUBTRACT: result = previousValue.subtract(value); break;
            case MULTIPLY: result = previousValue.multiply(value); break;
            case DIVIDE:
                if (value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                result = previousValue.divide(value, 10, RoundingMode.HALF_UP);
                break;
            default: return;
        }
        result = result.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        displayValue = result;
        previousValue = result;
        input = result.toPlainString();
        hasInput = true;
        hasDecimal = input.indexOf('.') >= 0;
        currentOperation = Operation.NONE;
        justEvaluated = true;
    }

    public void clear() { reset(); }

    private void reset() {
        displayValue = BigDecimal.ZERO;
        previousValue = BigDecimal.ZERO;
        currentOperation = Operation.NONE;
        justEvaluated = false;
        hasDecimal = false;
        hasInput = false;
        input = "0";
    }

    public void backspace() {
        if (justEvaluated) { reset(); return; }
        if (!hasInput) return;
        if (input.length() <= 1 || "-".equals(input)) {
            input = "0";
            hasInput = false;
            hasDecimal = false;
        } else {
            input = input.substring(0, input.length() - 1);
            if ("-".equals(input)) {
                input = "0";
                hasInput = false;
                hasDecimal = false;
            } else {
                hasDecimal = input.indexOf('.') >= 0;
            }
        }
        syncDisplayValue();
    }

    public String getDisplayText() {
        String text = hasInput ? input : displayValue.toPlainString();
        if (text.isEmpty() || "-".equals(text)) return "0";
        if (text.length() > 15 && !text.endsWith(".")) {
            return String.format(java.util.Locale.US, "%.6e", currentInputValue().doubleValue());
        }
        return text;
    }

    public Operation getCurrentOperation() { return currentOperation; }
    public boolean hasInput() { return hasInput; }
    public int add(int a, int b) { return a + b; }
    public int multiply(int a, int b) { return a * b; }

    private BigDecimal currentInputValue() {
        return hasInput ? new BigDecimal(input) : displayValue;
    }

    private void syncDisplayValue() {
        displayValue = currentInputValue();
    }

    private static int digitCount(String value) {
        int count = 0;
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) count++;
        }
        return count;
    }
}
