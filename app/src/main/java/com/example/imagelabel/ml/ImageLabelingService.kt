package com.example.imagelabel.ml

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class LabelResult(
    val text: String,
    val confidence: Float,
    val index: Int
)

data class ObjectResult(
    val labels: List<LabelResult>,
    val boundingBox: Rect,
    val trackingId: Int?
)

class ImageLabelingService {
    
    private val imageLabeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )
    
    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )
    
    suspend fun labelImage(bitmap: Bitmap): Result<List<LabelResult>> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val labels: List<ImageLabel> = imageLabeler.process(inputImage).await()
            
            val results = labels.mapIndexed { index, label ->
                LabelResult(
                    text = label.text,
                    confidence = label.confidence,
                    index = label.index
                )
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun detectObjects(bitmap: Bitmap): Result<List<ObjectResult>> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val objects: List<DetectedObject> = objectDetector.process(inputImage).await()
            
            val results = objects.map { detectedObject ->
                val labels = detectedObject.labels.mapIndexed { index, label ->
                    LabelResult(
                        text = label.text,
                        confidence = label.confidence,
                        index = label.index
                    )
                }
                
                ObjectResult(
                    labels = labels,
                    boundingBox = detectedObject.boundingBox,
                    trackingId = detectedObject.trackingId
                )
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun close() {
        imageLabeler.close()
        objectDetector.close()
    }
} 