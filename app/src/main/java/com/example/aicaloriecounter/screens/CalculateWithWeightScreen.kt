package com.example.aicaloriecounter.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aicaloriecounter.R
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculateWithWeightScreen(navController: NavController) {
    var caloriesPer100g by remember { mutableStateOf("") }
    var weightInGrams by remember { mutableStateOf("") }
    var totalCalories by remember { mutableStateOf<Double?>(null) }

    // Separate error states for each field
    var caloriesErrorType by remember { mutableStateOf<ErrorType?>(null) }
    var weightErrorType by remember { mutableStateOf<ErrorType?>(null) }

    val maxInputLength = 5

    // Extract the input validation logic to reduce cognitive complexity
    fun validateAndUpdateInput(
        currentValue: String,
        newValue: String,
        onUpdate: (String) -> Unit,
        onError: (ErrorType?) -> Unit
    ) {
        // Case 1: Length exceeded
        if (currentValue.length >= maxInputLength && newValue.length > currentValue.length) {
            onError(ErrorType.LENGTH_EXCEEDED)
            return
        }

        // Case 2: Invalid format
        if (!newValue.matches(INPUT_REGEX) && newValue.isNotEmpty()) {
            onError(ErrorType.INVALID_FORMAT)
            return
        }

        if (newValue.startsWith(".")) {
            onError(ErrorType.FIRST_MUST_BE_NUMBER)
            return
        }

        if (newValue.length >= 5 && newValue[4] == '.') {
            onError(ErrorType.FIFTH_MUST_BE_NUMBER)
            return
        }

        // Case 3: Valid input
        onUpdate(newValue)
        onError(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Calories input with compact error message
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            InputErrorMessage(
                errorType = caloriesErrorType,
                onDismiss = { caloriesErrorType = null }
            )

            OutlinedTextField(
                value = caloriesPer100g,
                onValueChange = { newValue ->
                    validateAndUpdateInput(
                        caloriesPer100g,
                        newValue,
                        { caloriesPer100g = it },
                        { caloriesErrorType = it }
                    )
                },
                label = { Text("Calories per 100g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weight input with compact error message
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            InputErrorMessage(
                errorType = weightErrorType,
                onDismiss = { weightErrorType = null }
            )

            OutlinedTextField(
                value = weightInGrams,
                onValueChange = { newValue ->
                    validateAndUpdateInput(
                        weightInGrams,
                        newValue,
                        { weightInGrams = it },
                        { weightErrorType = it }
                    )
                },
                label = { Text("Weight (in grams)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            onCalculateClicked(
                caloriesPer100g,
                weightInGrams
            ) { totalCalories = it }
        }) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        totalCalories?.let {
            val formattedCalories = if (it % 1.0 == 0.0) {
                String.format(Locale.getDefault(), "%.0f", it) // Format as whole number
            } else {
                String.format(Locale.getDefault(), "%.2f", it) // Format with two decimal places
            }
            Text(stringResource(R.string.total_calories_result, formattedCalories))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.navigateUp() }) {
            Text("Back to Main")
        }
    }
}

// Error types enum
enum class ErrorType {
    INVALID_FORMAT,
    LENGTH_EXCEEDED,
    FIRST_MUST_BE_NUMBER,
    FIFTH_MUST_BE_NUMBER
}

private fun onCalculateClicked(
    caloriesPer100g: String,
    weightInGrams: String,
    onTotalCaloriesChanged: (Double?) -> Unit
) {
    val totalCalories = calculateTotalCalories(caloriesPer100g, weightInGrams)
    onTotalCaloriesChanged(totalCalories)
}

private fun calculateTotalCalories(caloriesPer100g: String, weightInGrams: String): Double? {
    if (caloriesPer100g.matches(INPUT_REGEX) && weightInGrams.matches(INPUT_REGEX)) {
        val calories = caloriesPer100g.toDoubleOrNull()
        val weight = weightInGrams.toDoubleOrNull()
        return if (calories != null && weight != null) {
            (calories / 100) * weight
        } else {
            null
        }
    }
    return null
}

private val INPUT_REGEX = Regex("[0-9]*\\.?[0-9]*")

@Composable
fun InputErrorMessage(errorType: ErrorType?, onDismiss: () -> Unit) {
    // Only create the state if there's an error
    val isVisible = errorType != null

    // Clean removal of the message after timeout
    LaunchedEffect(errorType) {
        if (errorType != null) {
            delay(3000) // 3 seconds delay
            onDismiss()
        }
    }

    if (isVisible) {
        val errorText = when (errorType) {
            ErrorType.INVALID_FORMAT -> "Numbers only"
            ErrorType.LENGTH_EXCEEDED -> "The limit is 5"
            ErrorType.FIRST_MUST_BE_NUMBER -> "1st must be a number"
            ErrorType.FIFTH_MUST_BE_NUMBER -> "5th must be a number"
        }

        Text(
            text = errorText,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .padding(start = 4.dp),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                background = Color.Black
            ),
            color = Color.White
        )
    }
}