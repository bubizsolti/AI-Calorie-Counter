package com.example.aicaloriecounter.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Converts an ImageProxy (from CameraX) to a Bitmap.
 *
 * @param image The ImageProxy captured by CameraX.
 * @return A Bitmap object that you can use for AI image processing.
 */
fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    // Convert the image planes (Y, U, V) into NV21 format (standard YUV)
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    // Y goes first
    yBuffer.get(nv21, 0, ySize)
    // V and U are swapped for NV21
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    // Create a YuvImage from the NV21 byte array
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()

    // Compress YUV to JPEG so we can decode it to a Bitmap
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
    val jpegBytes = out.toByteArray()

    // Decode the JPEG byte array to a Bitmap
    return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
}
