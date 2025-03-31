package com.example.aikaloriaszamlalo

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aikaloriaszamlalo.ui.theme.AiKaloriaSzamlaloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiKaloriaSzamlaloTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainContent(
                                modifier = Modifier.padding(innerPadding),
                                navController = navController
                            )
                        }
                        composable("camera") {
                            CameraScreen(
                                modifier = Modifier.padding(innerPadding),
                                navController = navController
                            )
                        }
                        composable("calculate_calories") { // Updated route
                            CalculateWithWeightScreen(navController = navController) // Updated screen
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun MainContent(modifier: Modifier = Modifier, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current as Activity
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Greeting(userName = "Android")
        Spacer(modifier = Modifier.height(16.dp))
        MyButton(buttonText = "Camera", navController = navController, route = "camera")
        MyButton(buttonText = "Calculate with weight", navController = navController, route = "calculate_calories") // Updated button text and route
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showDialog = true }) {
            Text(text = "Exit")
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Exit App") },
                text = { Text(text = "Are you sure you want to exit?") },
                confirmButton = {
                    Button(onClick = {
                        context.finishAndRemoveTask()
                    }) {
                        Text(text = "Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showDialog = false
                    }) {
                        Text(text = "No")
                    }
                }
            )
        }
    }
}

@Composable
fun Greeting(userName: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $userName!",
        modifier = modifier
    )
}

@Composable
fun MyButton(buttonText: String, navController: NavController, route: String) {
    Button(onClick = {
        navController.navigate(route)
    }) {
        Text(text = buttonText)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AiKaloriaSzamlaloTheme {
        val navController = rememberNavController()
        MainContent(navController = navController)
    }
}