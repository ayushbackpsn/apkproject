# Shoe Catalog App

Full-stack Android application with backend API for generating product catalog PDFs for shoe/slipper shopkeepers.

## Project Structure

```
├── backend/          # Node.js Express API server
└── android/          # Android Kotlin application
```

## Tech Stack

### Backend
- Node.js + Express
- MongoDB (Mongoose)
- PDFKit for PDF generation
- Sharp for image processing
- Multer for file uploads

### Frontend
- Kotlin (Native Android)
- Retrofit for API calls
- Glide for image loading
- Material Components for UI

## Quick Start

### Backend Setup

1. Navigate to backend directory:
```bash
cd backend
```

2. Install dependencies:
```bash
npm install
```

3. Create `.env` file with your configuration (see `backend/.env.example`)

4. Start MongoDB and run the server:
```bash
npm start
```

### Android Setup

1. Open `android` folder in Android Studio

2. Update backend URL in `RetrofitClient.kt`:
   - Emulator: `http://10.0.2.2:3000/`
   - Physical device: `http://YOUR_COMPUTER_IP:3000/`

3. Sync and build the project

4. Run on emulator or device

## Features

✅ Brand listing  
✅ Product listing by brand  
✅ Product selection with checkboxes  
✅ PDF generation with custom layout  
✅ PDF download and WhatsApp sharing  
✅ Image upload and processing  
✅ Persistent storage  

## API Endpoints

- `POST /auth/login` - User authentication
- `GET /brands` - List all brands
- `GET /brands/:id/products` - Get products by brand
- `POST /products` - Create product (with image)
- `POST /pdf/generate` - Generate PDF catalog
- `GET /pdf/:filename` - Download PDF

## PDF Layout

Each PDF page contains:
- Brand name (top)
- Product name (below brand)
- Product image (70% page height, centered)
- White background
- A4 portrait format

## Requirements

- Node.js 14+
- MongoDB
- Android Studio
- Android SDK API 24+

## Documentation

See individual README files:
- [Backend README](backend/README.md)
- [Android README](android/README.md)

## License

ISC

