# Shoe Catalog Android App

Native Android application for generating product catalog PDFs. Built with Kotlin, Retrofit, and Glide.

## Features

- Browse brands and products
- Select multiple products with checkboxes
- Generate PDF catalogs
- Share PDFs via WhatsApp
- Image loading with Glide

## Prerequisites

- Android Studio (latest version recommended)
- Android SDK (API 24 or higher)
- Backend server running (see backend README)

## Setup

1. Open the `android` folder in Android Studio.

2. Update the backend URL in `RetrofitClient.kt`:
   - For Android Emulator: `http://10.0.2.2:3000/`
   - For physical device: Use your computer's IP address (e.g., `http://192.168.1.100:3000/`)

3. Sync Gradle files.

4. Build and run the app on an emulator or physical device.

## Project Structure

```
app/src/main/
├── java/com/shoecatalog/app/
│   ├── data/model/      # Data models (Brand, Product, etc.)
│   ├── api/             # Retrofit API service
│   ├── adapter/         # RecyclerView adapters
│   └── MainActivity.kt  # Brand list screen
│   └── ProductsActivity.kt  # Product selection screen
├── res/
│   ├── layout/          # XML layouts
│   ├── values/          # Strings, colors, themes
│   └── xml/             # File provider configuration
└── AndroidManifest.xml  # App configuration
```

## App Flow

1. **MainActivity**: Displays list of brands in a RecyclerView
2. **ProductsActivity**: Shows products for selected brand with checkboxes
3. **PDF Generation**: User selects products and taps "Generate PDF"
4. **Sharing**: PDF is downloaded and shared via WhatsApp

## Dependencies

- Retrofit 2.9.0 - API calls
- Glide 4.16.0 - Image loading
- Material Components - UI components
- Coroutines - Asynchronous operations

## Configuration

### Backend URL
Edit `RetrofitClient.kt` to change the backend server URL:

```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/" // Emulator
// or
private const val BASE_URL = "http://YOUR_IP:3000/" // Physical device
```

### File Provider
The app uses FileProvider for sharing PDFs. Configuration is in:
- `AndroidManifest.xml`
- `res/xml/file_paths.xml`

## Permissions

The app requires:
- Internet permission (for API calls)
- Read/Write storage (for PDF downloads)
- Network state (for checking connectivity)

## Building APK

1. Build → Generate Signed Bundle / APK
2. Select APK
3. Create a new keystore or use existing
4. Select release build variant
5. Generate APK

## Troubleshooting

- **Connection refused**: Make sure backend is running and URL is correct
- **PDF not downloading**: Check internet permission and storage permissions
- **WhatsApp not opening**: Ensure WhatsApp is installed or app will fall back to share dialog

