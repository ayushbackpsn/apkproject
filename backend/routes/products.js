const express = require('express');
const router = express.Router();
const Product = require('../models/Product');
const Brand = require('../models/Brand');
const multer = require('multer');
const path = require('path');
const sharp = require('sharp');

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/');
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, 'product-' + uniqueSuffix + path.extname(file.originalname));
  }
});

const fileFilter = (req, file, cb) => {
  if (file.mimetype.startsWith('image/')) {
    cb(null, true);
  } else {
    cb(new Error('Only image files are allowed'), false);
  }
};

const upload = multer({
  storage: storage,
  fileFilter: fileFilter,
  limits: { fileSize: 10 * 1024 * 1024 } // 10MB limit
});

// POST /products
router.post('/', upload.single('product_image'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: 'Product image is required' });
    }

    const { product_name, brand_name } = req.body;

    if (!product_name || !brand_name) {
      return res.status(400).json({ error: 'Product name and brand name are required' });
    }

    // Find or create brand
    let brand = await Brand.findOne({ brand_name });
    if (!brand) {
      brand = new Brand({ brand_name });
      await brand.save();
    }

    // Process and resize image while maintaining aspect ratio
    const imagePath = req.file.path;
    const resizedImagePath = imagePath.replace(path.extname(imagePath), '_resized' + path.extname(imagePath));
    
    await sharp(imagePath)
      .resize(1200, 1200, {
        fit: 'inside',
        withoutEnlargement: true
      })
      .jpeg({ quality: 90 })
      .toFile(resizedImagePath);

    // Use resized image
    const productImagePath = resizedImagePath.replace('uploads/', '');

    const product = new Product({
      product_name,
      brand_name,
      brand_id: brand._id,
      product_image: productImagePath
    });

    await product.save();

    res.status(201).json({
      message: 'Product created successfully',
      product: {
        id: product._id,
        product_name: product.product_name,
        brand_name: product.brand_name,
        product_image: `/uploads/${product.product_image}`
      }
    });
  } catch (error) {
    console.error('Error creating product:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

module.exports = router;

