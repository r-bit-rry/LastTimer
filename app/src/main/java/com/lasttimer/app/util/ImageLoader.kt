package com.lasttimer.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for optimized image loading and caching
 */
object ImageLoader {
    
    // Cache size as 1/8th of available memory
    private val cacheSize = (Runtime.getRuntime().maxMemory() / 8).toInt()
    
    // Image memory cache
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // Return size in kilobytes
            return bitmap.byteCount / 1024
        }
    }
    
    /**
     * Load a bitmap from resources
     * @param context Application context
     * @param resourceId Resource ID of the image
     * @param targetWidth Target width (0 for original size)
     * @param targetHeight Target height (0 for original size)
     * @return Loaded bitmap, or null if loading fails
     */
    suspend fun loadFromResources(
        context: Context,
        resourceId: Int,
        targetWidth: Int = 0,
        targetHeight: Int = 0
    ): Bitmap? = withContext(Dispatchers.IO) {
        // Create cache key
        val cacheKey = "${resourceId}_${targetWidth}_$targetHeight"
        
        // Check memory cache first
        memoryCache.get(cacheKey)?.let { return@withContext it }
        
        try {
            // Load bitmap with options to decode efficiently
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            
            BitmapFactory.decodeResource(context.resources, resourceId, options)
            
            // Calculate sample size if target dimensions provided
            if (targetWidth > 0 && targetHeight > 0) {
                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            }
            
            // Decode actual bitmap
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
            
            // Add to memory cache
            bitmap?.let { memoryCache.put(cacheKey, it) }
            
            return@withContext bitmap
        } catch (e: Exception) {
            android.util.Log.e("ImageLoader", "Error loading resource: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * Calculate optimal sample size for downsampling
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        
        if (height > targetHeight || width > targetWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            // Find largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width
            while ((halfHeight / inSampleSize) >= targetHeight && (halfWidth / inSampleSize) >= targetWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Save bitmap to disk cache
     * @param context Application context
     * @param bitmap Bitmap to save
     * @param filename Filename to save as
     * @param format Bitmap format (default Bitmap.CompressFormat.JPEG)
     * @param quality Compression quality 0-100 (default 85)
     * @return File object of the saved image, or null if saving fails
     */
    suspend fun saveToDiskCache(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 85
    ): File? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, filename)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
            }
            
            return@withContext file
        } catch (e: Exception) {
            android.util.Log.e("ImageLoader", "Error saving to disk cache: ${e.message}")
            return@withContext null
        }
    }
    
    /**
     * Clear the entire image cache
     */
    fun clearCache() {
        memoryCache.evictAll()
    }
    
    /**
     * Composable to load and remember a bitmap
     * @param resourceId Resource ID of the image
     * @param targetWidth Target width (0 for original size)
     * @param targetHeight Target height (0 for original size)
     * @return Loaded bitmap state
     */
    @Composable
    fun rememberBitmap(
        resourceId: Int,
        targetWidth: Int = 0,
        targetHeight: Int = 0
    ): Bitmap? {
        val context = LocalContext.current
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        
        LaunchedEffect(resourceId, targetWidth, targetHeight) {
            bitmap = loadFromResources(context, resourceId, targetWidth, targetHeight)
        }
        
        // Clean up bitmap when not needed
        DisposableEffect(Unit) {
            onDispose {
                // Not calling recycle() as the bitmap might be cached
                // Just removing the reference
                bitmap = null
            }
        }
        
        return bitmap
    }
}
