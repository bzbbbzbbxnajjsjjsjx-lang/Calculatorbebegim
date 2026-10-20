package com.example.calculatorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.Calculator.Operation
import com.example.calculatorapp.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[CalculatorViewModel::class.java]
        setContent {
            val state by viewModel.uiState.collectAsState()
            CalculatorTheme(darkTheme = state.isDarkTheme) {
                val haptics = LocalHapticFeedback.current
                val systemDark = isSystemInDarkTheme()
                LaunchedEffect(state.hapticTick) {
                    if (state.hapticTick != 0L) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                CalculatorScreen(
                    state = state,
                    onDigitClick = viewModel::onDigitClick,
                    onDecimalClick = viewModel::onDecimalClick,
                    onSignClick = viewModel::onSignClick,
                    onPercentClick = viewModel::onPercentClick,
                    onOperationClick = viewModel::onOperationClick,
                    onEqualsClick = viewModel::onEqualsClick,
                    onClearClick = viewModel::onClearClick,
                    onBackspaceClick = viewModel::onBackspaceClick,
                    onThemeClick = { viewModel.toggleTheme(systemDark) },
                )
            }
        }
    }
}

@Composable
fun CalculatorScreen(
    state: CalculatorViewModel.CalculatorUiState = CalculatorViewModel.CalculatorUiState(),
    onDigitClick: (Int) -> Unit = {},
    onDecimalClick: () -> Unit = {},
    onSignClick: () -> Unit = {},
    onPercentClick: () -> Unit = {},
    onOperationClick: (Operation) -> Unit = {},
    onEqualsClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onBackspaceClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val themeDescription = stringResource(R.string.calculator_theme)
    val backspaceDescription = stringResource(R.string.calculator_backspace_description)
    val signDescription = stringResource(R.string.calculator_sign_description)
    val rows = listOf(
        listOf("AC", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("±", "0", ".", "="),
    )
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(colors.background)
            .statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        val compact = maxHeight < 620.dp
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("NUMI", style = MaterialTheme.typography.labelLarge, color = colors.primary,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 3.sp)
                    Text(stringResource(com.example.calculatorapp.R.string.calculator_tagline),
                        style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
                }
                FilledTonalButton(
                    onClick = onThemeClick,
                    modifier = Modifier.semantics { contentDescription = themeDescription },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) { Text("☼") }
            }
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = if (compact) 130.dp else 170.dp),
                shape = RoundedCornerShape(32.dp), color = colors.surfaceVariant, tonalElevation = 1.dp,
            ) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End) {
                    Text(state.previousExpression.ifEmpty { stringResource(R.string.calculator_ready) },
                        style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(10.dp))
                    AnimatedContent(state.displayText, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "display") { value ->
                        val displayDescription = stringResource(R.string.calculator_display_description, value)
                        Text(value, style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = when { value.length > 12 -> 34.sp; value.length > 8 -> 46.sp; else -> 64.sp },
                            fontWeight = FontWeight.Medium, letterSpacing = (-2).sp),
                            color = if (state.isError) colors.error else colors.onSurface,
                            maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
                            textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth()
                                .semantics { contentDescription = displayDescription })
                    }
                }
            }
            rows.forEachIndexed { rowIndex, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { key ->
                        val operation = when (key) {
                            "÷" -> Operation.DIVIDE; "×" -> Operation.MULTIPLY
                            "−" -> Operation.SUBTRACT; "+" -> Operation.ADD; else -> Operation.NONE
                        }
                        val isOperator = operation != Operation.NONE
                        val utility = rowIndex == 0 || key == "±" || key == "."
                        val selected = isOperator && operation == state.currentOperation
                        val container = when { key == "=" -> colors.primary; selected -> colors.primaryContainer
                            isOperator -> colors.tertiaryContainer; utility -> colors.secondaryContainer; else -> colors.surface }
                        val content = when { key == "=" -> colors.onPrimary; selected -> colors.onPrimaryContainer
                            isOperator -> colors.onTertiaryContainer; utility -> colors.onSecondaryContainer; else -> colors.onSurface }
                        FilledTonalButton(
                            onClick = { when (key) {
                                "AC" -> onClearClick(); "⌫" -> onBackspaceClick(); "%" -> onPercentClick()
                                "±" -> onSignClick(); "." -> onDecimalClick(); "=" -> onEqualsClick()
                                "÷", "×", "−", "+" -> onOperationClick(operation)
                                else -> onDigitClick(key.toInt())
                            } },
                            modifier = Modifier.weight(1f).heightIn(min = if (compact) 52.dp else 60.dp)
                                .aspectRatio(1.08f).semantics { contentDescription = when (key) {
                                    "⌫" -> backspaceDescription
                                    "±" -> signDescription
                                    else -> key
                                } },
                            shape = RoundedCornerShape(if (key == "=") 28.dp else 20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(container, content),
                            contentPadding = PaddingValues(0.dp),
                        ) { Text(key, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium) }
                    }
                }
            }
            Text(stringResource(R.string.calculator_footer), Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
