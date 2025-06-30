# 🖼️ Image Labeling App

A modern Android application built with **Jetpack Compose** that uses **ML Kit** for on-device image analysis. The app can identify objects, labels, and provide detailed analysis of any image using Google's powerful machine learning models.

## ✨ Features

### 🔍 **Dual Analysis Modes**
- **Image Labeling**: Identifies general concepts, objects, activities, and scenes in images
- **Object Detection**: Detects and locates specific objects within images with bounding boxes

### 📱 **Image Input Options**
- **Camera Capture**: Take photos directly within the app
- **Gallery Selection**: Choose existing images from device storage
- **Automatic Image Processing**: Handles image rotation and resizing automatically

### 🎨 **Beautiful Modern UI**
- **Material Design 3**: Latest Material You design system
- **Responsive Design**: Optimized for different screen sizes
- **Smooth Animations**: Engaging user interactions with loading states
- **Dark/Light Theme Support**: Automatic theme adaptation

### 🧠 **Advanced ML Capabilities**
- **On-Device Processing**: Fast, private analysis without internet connection
- **High Accuracy**: Google's state-of-the-art ML models
- **Confidence Scores**: Percentage confidence for each detected label/object
- **Multiple Object Detection**: Can identify multiple objects in a single image

## 🛠️ Technical Stack

### **Frontend**
- **Jetpack Compose**: Modern declarative UI toolkit
- **Material Design 3**: Latest Material You components
- **Navigation**: Type-safe navigation with Compose

### **Machine Learning**
- **ML Kit**: Google's on-device ML SDK
  - Image Labeling API
  - Object Detection and Tracking API
- **TensorFlow Lite**: Underlying ML framework (handled by ML Kit)

### **Architecture**
- **MVVM Pattern**: Clean separation of concerns
- **StateFlow**: Reactive state management
- **Coroutines**: Asynchronous programming
- **Clean Architecture**: Maintainable and testable code

### **Permissions & Camera**
- **Camera API**: Direct photo capture
- **Storage Access**: Gallery image selection
- **Runtime Permissions**: Smooth permission handling with Accompanist

## 🚀 Getting Started

### **Prerequisites**
- Android Studio Hedgehog or later
- Android SDK 24+ (Android 7.0 Nougat)
- Kotlin 1.9+

### **Installation**
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on device or emulator

### **Permissions Required**
- `CAMERA`: For taking photos
- `READ_EXTERNAL_STORAGE`: For accessing gallery images
- `READ_MEDIA_IMAGES`: For Android 13+ media access

## 📖 How to Use

### **Step 1: Select an Image**
- Tap "Select Image" on the main screen
- Choose between:
  - 📷 **Take Photo**: Capture with camera
  - 🖼️ **Choose from Gallery**: Select existing image

### **Step 2: Analyze the Image**
- **Get Labels**: General image classification
  - Identifies concepts like "food", "animal", "vehicle"
  - Provides confidence scores for each label
  
- **Detect Objects**: Specific object detection
  - Locates individual objects in the image
  - Shows bounding box information
  - Multiple objects can be detected simultaneously

### **Step 3: View Results**
- **Confidence Scores**: Color-coded percentage confidence
  - 🟢 Green: 80%+ confidence
  - 🟡 Yellow: 60-79% confidence
  - 🔴 Red: Below 60% confidence
- **Multiple Results**: Scroll through all detected items
- **Clear Results**: Reset analysis to try new images

## 🎯 ML Model Details

### **Image Labeling Model**
- **Base Model**: Google's Cloud Vision API model optimized for mobile
- **Categories**: 400+ labels across various categories
- **Confidence Threshold**: 50% minimum confidence
- **Performance**: ~100ms processing time on modern devices

### **Object Detection Model**
- **Architecture**: MobileNet-based SSD detector
- **Objects**: 80+ object categories (COCO dataset)
- **Features**: 
  - Real-time detection
  - Bounding box coordinates
  - Object tracking IDs
  - Multiple object detection

### **Optimization Features**
- **Image Preprocessing**: Automatic resizing to 1024px max dimension
- **Memory Management**: Efficient bitmap handling
- **Battery Optimization**: On-device processing saves battery vs cloud APIs

## 🔧 Architecture Overview

```
┌─ UI Layer (Compose)
│  ├─ ImageLabelScreen
│  ├─ UI Components
│  └─ Theme System
│
├─ ViewModel Layer
│  ├─ ImageLabelViewModel
│  ├─ State Management
│  └─ UI State
│
├─ Domain Layer
│  ├─ ML Processing
│  ├─ Image Utils
│  └─ Data Models
│
└─ Data Layer
   ├─ ML Kit Integration
   ├─ Image Processing
   └─ Permission Handling
```

## 🎨 UI/UX Features

### **Responsive Design**
- Adapts to different screen sizes
- Optimized touch targets
- Proper spacing and typography

### **Accessibility**
- Screen reader support
- High contrast support
- Semantic content descriptions

### **Performance**
- Smooth 60fps animations
- Efficient image loading
- Memory-optimized operations

## 🔒 Privacy & Security

- **On-Device Processing**: Images never leave your device
- **No Network Required**: Works completely offline
- **No Data Collection**: App doesn't collect or store personal data
- **Local Storage Only**: Images processed locally

## 🐛 Troubleshooting

### **Common Issues**

**Camera not working:**
- Ensure camera permissions are granted
- Check if device has a camera
- Restart the app if camera appears frozen

**Low accuracy results:**
- Ensure good lighting conditions
- Try images with clear, distinct objects
- Avoid blurry or low-quality images

**App crashes:**
- Update to latest Android System WebView
- Clear app cache and restart
- Ensure device has sufficient RAM (2GB+ recommended)

## 🔮 Future Enhancements

- **Text Recognition**: OCR capabilities with ML Kit
- **Face Detection**: Facial analysis features
- **Custom Models**: Support for custom TensorFlow Lite models
- **Batch Processing**: Multiple image analysis
- **Export Features**: Save results to files
- **Image Enhancement**: Pre-processing filters

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

**Built with ❤️ using Android Jetpack Compose and ML Kit** 