package com.example.imagelabel.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImageUtils {
    
    const val MAX_IMAGE_SIZE = 1024
    
    /**
     * Load and process image from URI with proper rotation and size optimization
     */
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            bitmap?.let { 
                val rotatedBitmap = rotateImageIfRequired(context, it, uri)
                resizeBitmap(rotatedBitmap, MAX_IMAGE_SIZE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Rotate image based on EXIF data to ensure correct orientation
     */
    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()
            
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL
            
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: IOException) {
            bitmap
        }
    }
    
    /**
     * Rotate bitmap by given degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Resize bitmap to fit within maximum dimensions while maintaining aspect ratio
     */
    fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (width > height) {
            maxSize to (maxSize / aspectRatio).toInt()
        } else {
            (maxSize * aspectRatio).toInt() to maxSize
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Create a placeholder bitmap for testing purposes
     */
    fun createSampleBitmap(width: Int = 400, height: Int = 300): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.LTGRAY)
        }
    }
    
    /**
     * Get the display size for images in the UI
     */
    fun calculateDisplaySize(bitmap: Bitmap, maxDisplaySize: Int): Pair<Int, Int> {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        
        return if (bitmap.width > bitmap.height) {
            maxDisplaySize to (maxDisplaySize / aspectRatio).toInt()
        } else {
            (maxDisplaySize * aspectRatio).toInt() to maxDisplaySize
        }
    }
} 