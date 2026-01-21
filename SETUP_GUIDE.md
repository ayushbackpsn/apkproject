# Setup Guide - Shoe Catalog App

Complete step-by-step guide to set up and run the application.

## Backend Setup

### Step 1: Install MongoDB
- Download and install MongoDB from https://www.mongodb.com/try/download/community
- Start MongoDB service (on Windows: MongoDB runs as a service automatically)
- Verify: `mongod --version`

### Step 2: Install Node.js
- Download and install Node.js (v14+) from https://nodejs.org/
- Verify: `node --version` and `npm --version`

### Step 3: Setup Backend
```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# Create .env file (copy from .env.example)
# Edit .env with your MongoDB URI and JWT secret
```

### Step 4: Seed Initial Data (Optional)
```bash
node scripts/seedData.js
```

This creates:
- Sample brands: FLITE-PU, SPARX, BATA, NIKE, ADIDAS
- Default admin user (username: admin, password: admin123)

### Step 5: Start Backend Server
```bash
npm start
```

Server runs on: `http://localhost:3000`

## Android Setup

### Step 1: Install Android Studio
- Download from https://developer.android.com/studio
- Install Android SDK (API 24+)

### Step 2: Open Project
1. Open Android Studio
2. File → Open → Select `android` folder
3. Wait for Gradle sync to complete

### Step 3: Configure Backend URL

Edit `android/app/src/main/java/com/shoecatalog/app/api/RetrofitClient.kt`:

**For Android Emulator:**
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/"
```

**For Physical Device:**
1. Find your computer's IP address:
   - Windows: `ipconfig` → Look for IPv4 Address
   - Mac/Linux: `ifconfig` → Look for inet address
2. Update BASE_URL:
```kotlin
private const val BASE_URL = "http://192.168.1.XXX:3000/" // Replace XXX with your IP
```
3. Make sure phone and computer are on the same WiFi network

### Step 4: Build and Run
1. Connect Android device or start emulator
2. Click Run (▶️) button in Android Studio
3. Select device/emulator
4. Wait for app to install and launch

## Testing the App

### 1. Add Products via API

Use Postman or curl to add products:

```bash
curl -X POST http://localhost:3000/products \
  -F "product_name=PUL-110-MAROON" \
  -F "brand_name=FLITE-PU" \
  -F "product_image=@/path/to/image.jpg"
```

Or use the `/products` endpoint in Postman with form-data.

### 2. Test in App
1. Open app → See list of brands
2. Tap a brand → See products
3. Select products using checkboxes
4. Tap "Generate PDF"
5. PDF generates and shares via WhatsApp

## Troubleshooting

### Backend Issues

**MongoDB connection error:**
- Make sure MongoDB is running: `mongod --version`
- Check MONGODB_URI in .env file
- Try: `mongodb://127.0.0.1:27017/shoe_catalog`

**Port already in use:**
- Change PORT in .env file
- Or kill process using port 3000

**Module not found:**
- Run `npm install` again
- Delete `node_modules` and reinstall

### Android Issues

**Connection refused:**
- Verify backend is running: `curl http://localhost:3000/health`
- Check BASE_URL in RetrofitClient.kt
- For physical device: ensure same WiFi network

**Build errors:**
- File → Invalidate Caches → Invalidate and Restart
- File → Sync Project with Gradle Files
- Clean and Rebuild Project

**PDF not sharing:**
- Check internet permission in AndroidManifest.xml
- Ensure WhatsApp is installed (or app will use share dialog)

**Image not loading:**
- Verify image URLs in product data
- Check backend serves images from `/uploads` route
- Verify Glide dependencies in build.gradle

## File Structure Quick Reference

```
apkproject/
├── backend/
│   ├── models/          # Database models
│   ├── routes/          # API endpoints
│   ├── scripts/         # Seed data script
│   ├── uploads/         # Product images (auto-created)
│   ├── pdfs/           # Generated PDFs (auto-created)
│   ├── server.js       # Main server file
│   └── package.json
│
└── android/
    └── app/
        ├── src/main/
        │   ├── java/com/shoecatalog/app/
        │   │   ├── data/model/  # Data models
        │   │   ├── api/         # API service
        │   │   ├── adapter/     # RecyclerView adapters
        │   │   └── *.kt         # Activities
        │   ├── res/             # Resources
        │   └── AndroidManifest.xml
        └── build.gradle.kts
```

## Next Steps

1. Add authentication flow in Android app
2. Implement product creation UI in app
3. Add image upload functionality
4. Customize PDF layout if needed
5. Add error handling and user feedback
6. Deploy backend to cloud (Heroku, AWS, etc.)
7. Generate signed APK for distribution

## Support

For issues or questions:
1. Check README files in backend/ and android/
2. Review error logs in console
3. Verify all prerequisites are installed
4. Ensure MongoDB and backend server are running

