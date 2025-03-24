package com.example.aikaloriaszamlalo

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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                        composable("main") { MainContent(modifier = Modifier.padding(innerPadding), navController = navController) }
                        composable("screen1") { EmptyScreen(modifier = Modifier.padding(innerPadding),text = "Screen 1", navController = navController) }
                        composable("screen2") { EmptyScreen(modifier = Modifier.padding(innerPadding), text = "Screen 2", navController = navController) }
                    }
                }
            }
        }
    }
}
@Composable
fun EmptyScreen(modifier: Modifier = Modifier, text: String, navController: NavController) {
    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(text = text)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigateUp()}) {
            Text(text = "Back to Main")
        }
    }

}
@Composable
fun MainContent(modifier: Modifier = Modifier, navController: NavController) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Greeting(userName = "Android")
        Spacer(modifier = Modifier.height(16.dp))
        MyButton(buttonText = "Button 1", navController = navController, route = "screen1")
        MyButton(buttonText = "Button 2", navController = navController, route = "screen2")
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
        // This is where you'll add the button's action
        println("$buttonText clicked!")
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