package com.example.aikaloriaszamlalo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CalculatorScreen(navController: NavController) {
    var currentInput by remember { mutableStateOf("") }
    var previousInput by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom, // Align content to the bottom
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Adjust height as needed
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = currentInput.ifEmpty { "0" },
                style = TextStyle(fontSize = 36.sp, textAlign = TextAlign.End),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Buttons
        val buttons = listOf(
            "C", "+/-", "%", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "Del", "="
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            buttons.chunked(4).forEach { rowButtons ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth() // Fill the available width
                ) {
                    rowButtons.forEach { symbol ->
                        CalculatorButton(
                            symbol = symbol,
                            onClick = {
                                handleButtonClick(
                                    symbol,
                                    { currentInput = it },
                                    { previousInput = it },
                                    { operation = it },
                                    currentInput,
                                    previousInput,
                                    operation
                                )
                            },
                            modifier = Modifier.weight(1f) // Apply weight correctly here
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigateUp() }) {
            Text("Back to Main")
        }
    }
}

@Composable
fun CalculatorButton(symbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f), // Ensures square shape
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (symbol in listOf("/", "*", "-", "+", "="))
                MaterialTheme.colorScheme.primary
            else Color.LightGray,
            contentColor = if (symbol in listOf("/", "*", "-", "+", "="))
                Color.White
            else Color.Black
        )
    ) {
        Text(
            text = symbol,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}




private fun handleButtonClick(
    symbol: String,
    onCurrentInputChanged: (String) -> Unit,
    onPreviousInputChanged: (String) -> Unit,
    onOperationChanged: (String) -> Unit,
    currentInput: String,
    previousInput: String,
    operation: String
) {
    when (symbol) {
        "C" -> {
            onCurrentInputChanged("")
            onPreviousInputChanged("")
            onOperationChanged("")
        }
        "+/-" -> onCurrentInputChanged(toggleSign(currentInput))
        "Del" -> onCurrentInputChanged(currentInput.dropLast(1))
        "/", "*", "-", "+" -> handleOperation(
            symbol,
            onCurrentInputChanged,
            onPreviousInputChanged,
            onOperationChanged,
            currentInput,
            previousInput,
            operation
        )
        "=" -> performCalculation(
            onCurrentInputChanged,
            onPreviousInputChanged,
            onOperationChanged,
            currentInput,
            previousInput,
            operation
        )
        "%" -> onCurrentInputChanged(convertToPercentage(currentInput))
        else -> onCurrentInputChanged(currentInput + symbol)
    }
}

private fun toggleSign(input: String): String {
    return if (input.startsWith("-")) {
        input.drop(1)
    } else {
        "-$input"
    }
}

private fun handleOperation(
    symbol: String,
    onCurrentInputChanged: (String) -> Unit,
    onPreviousInputChanged: (String) -> Unit,
    onOperationChanged: (String) -> Unit,
    currentInput: String,
    previousInput: String,
    operation: String
) {
    if (currentInput.isNotEmpty()) {
        if (previousInput.isNotEmpty()) {
            calculate(previousInput, currentInput, operation) { res ->
                onPreviousInputChanged(res)
                onCurrentInputChanged("")
                onOperationChanged(symbol)
            }
        } else {
            onPreviousInputChanged(currentInput)
            onCurrentInputChanged("")
            onOperationChanged(symbol)
        }
    }
}

private fun performCalculation(
    onCurrentInputChanged: (String) -> Unit,
    onPreviousInputChanged: (String) -> Unit,
    onOperationChanged: (String) -> Unit,
    currentInput: String,
    previousInput: String,
    operation: String
) {
    if (currentInput.isNotEmpty() && previousInput.isNotEmpty()) {
        calculate(previousInput, currentInput, operation) { res ->
            onCurrentInputChanged(res)
            onPreviousInputChanged("")
            onOperationChanged("")
        }
    }
}

private fun convertToPercentage(input: String): String {
    return if (input.isNotEmpty()) {
        (input.toDouble() / 100).toString()
    } else {
        "0"
    }
}

fun calculate(num1Str: String, num2Str: String, operation: String, onResult: (String) -> Unit) {
    try {
        val num1 = num1Str.toDouble()
        val num2 = num2Str.toDouble()
        val res = when (operation) {
            "+" -> (num1 + num2).toString()
            "-" -> (num1 - num2).toString()
            "*" -> (num1 * num2).toString()
            "/" -> if (num2 != 0.0) (num1 / num2).toString() else "Division by zero"
            else -> num1Str // Handle cases where operation is not yet selected.
        }
        onResult(res)
    } catch (e: NumberFormatException) {
        onResult("Invalid Input")
    }
}