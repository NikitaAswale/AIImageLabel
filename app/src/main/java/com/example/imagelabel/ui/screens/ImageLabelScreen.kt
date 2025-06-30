package com.example.imagelabel.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.imagelabel.ml.LabelResult
import com.example.imagelabel.ml.ObjectResult
import com.example.imagelabel.utils.ImageUtils
import com.example.imagelabel.viewmodel.ImageLabelViewModel
import com.example.imagelabel.viewmodel.ProcessingType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImageLabelScreen(
    modifier: Modifier = Modifier,
    viewModel: ImageLabelViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Permission handling
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Activity result launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            ImageUtils.loadBitmapFromUri(context, it)?.let { bitmap ->
                viewModel.selectImage(bitmap)
            }
        }
        viewModel.hideImageSourceDialog()
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { 
            val resizedBitmap = ImageUtils.resizeBitmap(it, ImageUtils.MAX_IMAGE_SIZE)
            viewModel.selectImage(resizedBitmap)
        }
        viewModel.hideImageSourceDialog()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar()
        
        // Main Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Image Selection Card
                ImageSelectionCard(
                    selectedImage = uiState.selectedImage,
                    onSelectImageClick = { viewModel.showImageSourceDialog() }
                )
            }
            
            if (uiState.selectedImage != null) {
                item {
                    // Action Buttons
                    ActionButtonsCard(
                        isProcessing = uiState.isProcessing,
                        processingType = uiState.processingType,
                        onLabelImageClick = { viewModel.labelImage() },
                        onDetectObjectsClick = { viewModel.detectObjects() },
                        onClearResultsClick = { viewModel.clearResults() }
                    )
                }
                
                // Results Section
                if (uiState.labels.isNotEmpty()) {
                    item {
                        ResultsCard(
                            title = "Image Labels",
                            icon = Icons.Default.Label,
                            labels = uiState.labels
                        )
                    }
                }
                
                if (uiState.objects.isNotEmpty()) {
                    item {
                        ObjectResultsCard(
                            objects = uiState.objects
                        )
                    }
                }
            }
        }
    }
    
    // Image Source Dialog
    if (uiState.showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { viewModel.hideImageSourceDialog() },
            onCameraClick = {
                if (cameraPermissionState.status.isGranted) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            },
            onGalleryClick = {
                galleryLauncher.launch("image/*")
            }
        )
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Handle error display
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                "Image Labeling",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun ImageSelectionCard(
    selectedImage: Bitmap?,
    onSelectImageClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedImage != null) {
                Image(
                    bitmap = selectedImage.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onSelectImageClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Image")
                }
            } else {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                        .padding(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Select an Image",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    "Choose an image from gallery or take a new photo to start labeling",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = onSelectImageClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Image")
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsCard(
    isProcessing: Boolean,
    processingType: ProcessingType,
    onLabelImageClick: () -> Unit,
    onDetectObjectsClick: () -> Unit,
    onClearResultsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "AI Analysis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "Get Labels",
                    icon = Icons.Default.Label,
                    onClick = onLabelImageClick,
                    enabled = !isProcessing,
                    isLoading = isProcessing && processingType == ProcessingType.LABELING,
                    modifier = Modifier.weight(1f)
                )
                
                ActionButton(
                    text = "Detect Objects",
                    icon = Icons.Default.Search,
                    onClick = onDetectObjectsClick,
                    enabled = !isProcessing,
                    isLoading = isProcessing && processingType == ProcessingType.OBJECT_DETECTION,
                    modifier = Modifier.weight(1f)
                )
            }
            
            OutlinedButton(
                onClick = onClearResultsClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Results")
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp)
    }
}

@Composable
private fun ResultsCard(
    title: String,
    icon: ImageVector,
    labels: List<LabelResult>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            labels.forEach { label ->
                LabelItem(label = label)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ObjectResultsCard(
    objects: List<ObjectResult>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Detected Objects",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            objects.forEachIndexed { index, obj ->
                ObjectItem(objectResult = obj, index = index + 1)
                if (index < objects.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun LabelItem(label: LabelResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        ConfidenceChip(confidence = label.confidence)
    }
}

@Composable
private fun ObjectItem(objectResult: ObjectResult, index: Int) {
    Column {
        Text(
            "Object $index",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        objectResult.labels.forEach { label ->
            LabelItem(label = label)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ConfidenceChip(confidence: Float) {
    val percentage = (confidence * 100).toInt()
    val color = when {
        percentage >= 80 -> MaterialTheme.colorScheme.primary
        percentage >= 60 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = "$percentage%",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Image Source",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                DialogOption(
                    icon = Icons.Default.PhotoCamera,
                    text = "Take Photo",
                    onClick = onCameraClick
                )
                Spacer(modifier = Modifier.height(8.dp))
                DialogOption(
                    icon = Icons.Default.PhotoLibrary,
                    text = "Choose from Gallery",
                    onClick = onGalleryClick
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DialogOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 