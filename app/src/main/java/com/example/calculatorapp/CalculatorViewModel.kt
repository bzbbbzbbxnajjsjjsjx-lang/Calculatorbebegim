package com.example.calculatorapp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.Calculator
import com.example.Calculator.Operation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    companion object {
        private const val CALCULATOR_KEY = "calculator"
        private const val DISPLAY_KEY = "display"
        private const val EXPRESSION_KEY = "expression"
        private const val OPERATION_KEY = "operation"
        private const val EVALUATED_KEY = "evaluated"
        private const val INPUT_KEY = "input"
        private const val ERROR_KEY = "error"
        private const val THEME_KEY = "dark_theme"
        private const val HAPTIC_KEY = "haptic_enabled"
    }

    private val calculator = savedStateHandle.get<Calculator>(CALCULATOR_KEY) ?: Calculator()

    data class CalculatorUiState(
        val displayText: String = "0",
        val previousExpression: String = "",
        val currentOperation: Operation = Operation.NONE,
        val justEvaluated: Boolean = false,
        val hasInput: Boolean = false,
        val isError: Boolean = false,
        val isDarkTheme: Boolean? = null,
        val hapticTick: Long = 0L,
    )

    private val _uiState = MutableStateFlow(
        CalculatorUiState(
            displayText = savedStateHandle[DISPLAY_KEY] ?: "0",
            previousExpression = savedStateHandle[EXPRESSION_KEY] ?: "",
            currentOperation = savedStateHandle.get<String>(OPERATION_KEY)
                ?.let { runCatching { Operation.valueOf(it) }.getOrDefault(Operation.NONE) }
                ?: Operation.NONE,
            justEvaluated = savedStateHandle[EVALUATED_KEY] ?: false,
            hasInput = savedStateHandle[INPUT_KEY] ?: false,
            isError = savedStateHandle[ERROR_KEY] ?: false,
            isDarkTheme = savedStateHandle[THEME_KEY],
        ),
    )
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()
    private var hapticEnabled: Boolean
        get() = savedStateHandle[HAPTIC_KEY] ?: true
        set(value) { savedStateHandle[HAPTIC_KEY] = value }

    fun onDigitClick(digit: Int) = perform { calculator.appendDigit(digit) }
    fun onDecimalClick() = perform { calculator.appendDecimal() }
    fun onSignClick() = perform { calculator.toggleSign() }
    fun onPercentClick() = perform { calculator.applyPercent() }
    fun onOperationClick(operation: Operation) = perform { calculator.setOperation(operation) }

    fun onEqualsClick() {
        try {
            calculator.compute()
            updateUiState(justEvaluated = true)
            pulse(20)
        } catch (_: Exception) {
            showError()
        }
    }

    fun onClearClick() {
        calculator.clear()
        updateUiState()
        pulse(15)
    }

    fun onBackspaceClick() = perform { calculator.backspace() }

    fun toggleTheme(systemIsDark: Boolean = false) {
        val next = !(_uiState.value.isDarkTheme ?: systemIsDark)
        savedStateHandle[THEME_KEY] = next
        _uiState.value = _uiState.value.copy(isDarkTheme = next)
    }

    fun setHapticEnabled(enabled: Boolean) { hapticEnabled = enabled }

    private fun perform(action: () -> Unit) {
        try {
            action()
            updateUiState()
            pulse()
        } catch (_: Exception) {
            showError()
        }
    }

    private fun updateUiState(justEvaluated: Boolean = false) {
        val state = _uiState.value.copy(
            displayText = calculator.getDisplayText(),
            previousExpression = buildPreviousExpression(),
            currentOperation = calculator.getCurrentOperation(),
            justEvaluated = justEvaluated,
            hasInput = calculator.hasInput(),
            isError = false,
        )
        _uiState.value = state
        savedStateHandle[CALCULATOR_KEY] = calculator
        savedStateHandle[DISPLAY_KEY] = state.displayText
        savedStateHandle[EXPRESSION_KEY] = state.previousExpression
        savedStateHandle[OPERATION_KEY] = state.currentOperation.name
        savedStateHandle[EVALUATED_KEY] = state.justEvaluated
        savedStateHandle[INPUT_KEY] = state.hasInput
        savedStateHandle[ERROR_KEY] = false
    }

    private fun buildPreviousExpression(): String {
        val symbol = when (calculator.getCurrentOperation()) {
            Operation.ADD -> "+"
            Operation.SUBTRACT -> "−"
            Operation.MULTIPLY -> "×"
            Operation.DIVIDE -> "÷"
            Operation.NONE -> return ""
        }
        return "${calculator.getDisplayText()} $symbol"
    }

    private fun showError() {
        _uiState.value = _uiState.value.copy(displayText = "Error", isError = true)
        savedStateHandle[ERROR_KEY] = true
    }

    private fun pulse(duration: Long = 10L) {
        if (hapticEnabled) {
            _uiState.value = _uiState.value.copy(hapticTick = _uiState.value.hapticTick + duration)
        }
    }
}
