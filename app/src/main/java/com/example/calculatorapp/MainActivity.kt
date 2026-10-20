package com.example.calculatorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.Calculator
import com.example.Calculator.Operation
import com.example.calculatorapp.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CalculatorTheme { CalculatorScreen() } }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val calculator = remember { Calculator() }
    var display by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("Ready when you are") }
    var isError by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme

    fun update() {
        display = calculator.displayText
        expression = when (calculator.currentOperation) {
            Operation.ADD -> "${calculator.displayText}  +"
            Operation.SUBTRACT -> "${calculator.displayText}  −"
            Operation.MULTIPLY -> "${calculator.displayText}  ×"
            Operation.DIVIDE -> "${calculator.displayText}  ÷"
            Operation.NONE -> "Your result"
        }
    }
    fun perform(action: () -> Unit) {
        try {
            action()
            isError = false
            update()
        } catch (_: ArithmeticException) {
            display = "Cannot divide by zero"
            expression = "Check the operation"
            isError = true
            calculator.clear()
        } catch (_: Exception) {
            display = "Error"
            expression = "Try again"
            isError = true
            calculator.clear()
        }
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    val rows = listOf(
        listOf("AC", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("±", "0", ".", "=")
    )
    Column(
        modifier = modifier.fillMaxSize().background(colors.background)
            .statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NUMI", style = MaterialTheme.typography.labelLarge, color = colors.primary,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
                Text("Numbers, with feeling.", style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
            Surface(shape = CircleShape, color = colors.tertiaryContainer) {
                Text("✳", modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp),
                    color = colors.onTertiaryContainer, style = MaterialTheme.typography.titleLarge)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = true).heightIn(min = 150.dp),
            shape = RoundedCornerShape(36.dp),
            color = colors.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(expression, style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(14.dp))
                AnimatedContent(targetState = display, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "display") { value ->
                    Text(value,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = when { value.length > 12 -> 34.sp; value.length > 8 -> 46.sp; else -> 64.sp },
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-2).sp
                        ),
                        color = if (isError) colors.error else colors.onSurface,
                        maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Display $value" }
                    )
                }
            }
        }

        rows.forEachIndexed { rowIndex, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    val isOperator = key in listOf("÷", "×", "−", "+")
                    val utility = rowIndex == 0 || key == "±" || key == "."
                    val selected = key == when (calculator.currentOperation) {
                        Operation.ADD -> "+"
                        Operation.SUBTRACT -> "−"
                        Operation.MULTIPLY -> "×"
                        Operation.DIVIDE -> "÷"
                        Operation.NONE -> ""
                    }
                    val container = when {
                        key == "=" -> colors.primary
                        selected -> colors.primaryContainer
                        isOperator -> colors.tertiaryContainer
                        utility -> colors.secondaryContainer
                        else -> colors.surface
                    }
                    val content = when {
                        key == "=" -> colors.onPrimary
                        selected -> colors.onPrimaryContainer
                        isOperator -> colors.onTertiaryContainer
                        utility -> colors.onSecondaryContainer
                        else -> colors.onSurface
                    }
                    FilledTonalButton(
                        onClick = { perform {
                            when (key) {
                                "AC" -> calculator.clear()
                                "⌫" -> calculator.backspace()
                                "%" -> calculator.applyPercent()
                                "±" -> calculator.toggleSign()
                                "." -> calculator.appendDecimal()
                                "=" -> calculator.compute()
                                "÷" -> calculator.setOperation(Operation.DIVIDE)
                                "×" -> calculator.setOperation(Operation.MULTIPLY)
                                "−" -> calculator.setOperation(Operation.SUBTRACT)
                                "+" -> calculator.setOperation(Operation.ADD)
                                else -> calculator.appendDigit(key.toInt())
                            }
                        } },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp).aspectRatio(1.08f)
                            .semantics { contentDescription = when (key) { "⌫" -> "Backspace"; "±" -> "Change sign"; else -> key } },
                        shape = RoundedCornerShape(if (key == "=") 28.dp else 22.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = container, contentColor = content),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(key, style = MaterialTheme.typography.headlineSmall,
                            fontWeight = if (key in listOf("=", "+", "−", "×", "÷")) FontWeight.SemiBold else FontWeight.Medium)
                    }
                }
            }
        }
        Text("A little clarity goes a long way.", modifier = Modifier.fillMaxWidth().padding(top = 1.dp),
            style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true)
@Composable
private fun CalculatorPreview() { CalculatorTheme { CalculatorScreen() } }
