package com.example.aikaloriaszamlalo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculateWithWeightScreen(navController: NavController) {
    var caloriesPer100g by remember { mutableStateOf("") }
    var weightInGrams by remember { mutableStateOf("") }
    var totalCalories by remember { mutableStateOf<Double?>(null) }
    var showInputError by remember { mutableStateOf(false) } // Changed to showInputError

    val maxInputLength = 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InputErrorMessage(isVisible = showInputError, message = "Numbers only") // Added Error Message
        OutlinedTextField(
            value = caloriesPer100g,
            onValueChange = {
                if (it.length <= maxInputLength && it.matches(INPUT_REGEX)) {
                    caloriesPer100g = it
                    showInputError = false // Hide error on valid input
                } else if (it.isNotEmpty()) {
                    showInputError = true // Show error on invalid input
                }
            },
            label = { Text("Calories per 100g") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        InputErrorMessage(isVisible = showInputError, message = "Numbers only") // Added Error Message
        OutlinedTextField(
            value = weightInGrams,
            onValueChange = {
                if (it.length <= maxInputLength && it.matches(INPUT_REGEX)) {
                    weightInGrams = it
                    showInputError = false // Hide error on valid input
                } else if (it.isNotEmpty()) {
                    showInputError = true // Show error on invalid input
                }
            },
            label = { Text("Weight (in grams)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
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
private fun onCalculateClicked(
    caloriesPer100g: String,
    weightInGrams: String,
    onTotalCaloriesChanged: (Double?) -> Unit
) {
    val totalCalories = calculateTotalCalories(caloriesPer100g, weightInGrams)
    onTotalCaloriesChanged(totalCalories)
    // Removed error dialog logic as it's handled inline now
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
fun InputErrorMessage(isVisible: Boolean, message: String) {
    var isCurrentlyVisible by remember { mutableStateOf(isVisible) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isCurrentlyVisible = true
            delay(5000) // 5 seconds delay
            isCurrentlyVisible = false
        }
    }
    AnimatedVisibility(
        visible = isCurrentlyVisible,
        exit = slideOutVertically() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}