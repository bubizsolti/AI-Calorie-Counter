package com.example.aicaloriecounter.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCamPermission by remember { mutableStateOf(checkCameraPermission(context)) }

    var photoFile by remember { mutableStateOf<File?>(null) }
    var aiResult by remember { mutableStateOf("") }
    var isPhotoTaken by remember { mutableStateOf(false) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(Surface.ROTATION_0) // vagy a kijelző rotációja szerint
            .build()

    }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // ✅ Ask for camera permission if not granted
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCamPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!hasCamPermission) {
            showPermissionError(context, navController)
            return@Column
        }

        if (!isPhotoTaken) {
            // 📸 Show camera preview
            CameraPreview(lifecycleOwner, imageCapture) { provider ->
                cameraProvider = provider
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            Log.d("Camera", "✅ Saved photo to: ${file.absolutePath}")
                            photoFile = file
                            aiResult = fakeAIDetection(BitmapFactory.decodeFile(file.absolutePath))

                            // 👉 csak UI state-et frissíts, ne unbindolj itt!
                            isPhotoTaken = true
                        }


                        override fun onError(exception: ImageCaptureException) {
                            Log.e("Camera", "❌ Capture failed: ${exception.message}", exception)
                        }
                    }
                )
            }) {
                Text("📸 Take Picture")
            }
        } else {
            // ✅ Show full-screen photo preview
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 🖼️ Captured photo takes up most of the screen
                photoFile?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentScale = ContentScale.Crop
                    )
                }

                // 📊 Bottom info area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = aiResult)

                    Spacer(Modifier.height(12.dp))

                    // ✅ Buttons side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = { navController.navigateUp() }) {
                            Text("⬅️ Back")
                        }
                        Button(onClick = {
                            // 🧹 Reset for new capture
                            photoFile?.delete()
                            photoFile = null
                            isPhotoTaken = false
                            aiResult = ""
                        }) {
                            Text("📷 Take Another")
                        }
                    }
                }
            }
        }


        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigateUp() }) { Text("⬅️ Back to Main") }
    }
    LaunchedEffect(isPhotoTaken) {
        if (isPhotoTaken) {
            cameraProvider?.unbindAll()
        }
    }

}

// ✅ Check camera permission
private fun checkCameraPermission(context: android.content.Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

// ✅ Show error and return
private fun showPermissionError(context: android.content.Context, navController: NavController) {
    Toast.makeText(context, "Camera permission not granted", Toast.LENGTH_SHORT).show()
    navController.navigateUp()
}

// ✅ Preview composable
@Composable
private fun CameraPreview(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    imageCapture: ImageCapture,
    onCameraReady: (ProcessCameraProvider) -> Unit
) {
    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
                onCameraReady(cameraProvider)
            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
    )
}

// ✅ Fake AI detection logic (placeholder)
private fun fakeAIDetection(bitmap: Bitmap): String {
    val fakeFood = listOf(
        "🍕 Pizza - 285 kcal",
        "🍎 Apple - 52 kcal",
        "🍔 Burger - 354 kcal",
        "🥗 Salad - 150 kcal",
        "🍩 Donut - 452 kcal"
    ).random()
    return "✅ Detected: $fakeFood"
}
