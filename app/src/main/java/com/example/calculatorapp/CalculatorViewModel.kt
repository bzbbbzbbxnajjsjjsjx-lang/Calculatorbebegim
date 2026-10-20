package com.example.calculatorapp

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.Calculator
import com.example.Calculator.Operation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel(
    private val context: Context,
) : ViewModel() {

    private val calculator = Calculator()

    data class CalculatorUiState(
        val displayText: String = "0",
        val previousExpression: String = "",
        val currentOperation: Operation = Operation.NONE,
        val justEvaluated: Boolean = false,
        val hasInput: Boolean = false,
        val isError: Boolean = false,
        val isDarkTheme: Boolean = false,
    )

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var hapticEnabled by mutableStateOf(true)

    private fun getVibrator(): Vibrator? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }
    }

    private fun vibrate(durationMs: Long = 15L) {
        if (!hapticEnabled) return
        val vibrator = getVibrator() ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.AMplitude_DEFAULT))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    fun onDigitClick(digit: Int) {
        _uiState.value = _uiState.value.copy(isError = false)
        try {
            calculator.appendDigit(digit)
            updateUiState()
            vibrate(10)
        } catch (e: Exception) {
            showError()
        }
    }

    fun onDecimalClick() {
        _uiState.value = _uiState.value.copy(isError = false)
        try {
            calculator.appendDecimal()
            updateUiState()
            vibrate(10)
        } catch (e: Exception) {
            showError()
        }
    }

    fun onSignClick() {
        try {
            calculator.toggleSign()
            updateUiState()
            vibrate(10)
        } catch (e: Exception) {
            showError()
        }
    }

    fun onPercentClick() {
        try {
            calculator.applyPercent()
            updateUiState()
            vibrate(10)
        } catch (e: Exception) {
            showError()
        }
    }

    fun onOperationClick(operation: Operation) {
        _uiState.value = _uiState.value.copy(isError = false)
        try {
            calculator.setOperation(operation)
            updateUiState()
            vibrate(12)
        } catch (e: Exception) {
            showError()
        }
    }

    fun onEqualsClick() {
        try {
            calculator.compute()
            updateUiState()
            vibrate(20)
        } catch (e: ArithmeticException) {
            showError()
        } catch (e: Exception) {
            showError()
        }
    }

    fun onClearClick() {
        calculator.clear()
        _uiState.value = _uiState.value.copy(isError = false)
        updateUiState()
        vibrate(15)
    }

    fun onBackspaceClick() {
        try {
            calculator.backspace()
            updateUiState()
            vibrate(10)
        } catch (e: Exception) {
            showError()
        }
    }

    private fun updateUiState() {
        val display = calculator.getDisplayText()
        val prevExpr = buildPreviousExpression()
        _uiState.value = _uiState.value.copy(
            displayText = display,
            previousExpression = prevExpr,
            currentOperation = calculator.getCurrentOperation(),
            justEvaluated = false,
            hasInput = calculator.hasInput(),
            isError = false,
        )
    }

    private fun buildPreviousExpression(): String {
        val op = calculator.getCurrentOperation()
        if (op == Operation.NONE) return ""
        val opSymbol = when (op) {
            Operation.ADD -> "+"
            Operation.SUBTRACT -> "-"
            Operation.MULTIPLY -> "x"
            Operation.DIVIDE -> "/"
            Operation.NONE -> ""
        }
        val displayVal = calculator.getDisplayText()
        return "$opSymbol $displayVal"
    }

    private fun showError() {
        _uiState.value = _uiState.value.copy(
            displayText = "Error",
            isError = true,
        )
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(
            isDarkTheme = !_uiState.value.isDarkTheme,
        )
    }

    fun setHapticEnabled(enabled: Boolean) {
        hapticEnabled = enabled
    }
}
