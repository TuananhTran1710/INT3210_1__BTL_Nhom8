package com.example.wink.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // Hàm chính: Nhận vào Uri gốc -> Trả về Uri của file ảnh đã nén trong Cache
    fun compressImage(context: Context, imageUri: Uri): Uri {
        // 1. Đọc ảnh từ Uri
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (originalBitmap == null) return imageUri // Nếu lỗi đọc thì trả về ảnh gốc

        // 2. Resize (Thu nhỏ nếu ảnh quá to) - Ví dụ max chiều rộng/cao là 1024px
        val scaledBitmap = scaleBitmap(originalBitmap, 1024)

        // 3. Nén ảnh (JPEG, Chất lượng 60-70% là đủ cho Social)
        val bytes = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes)

        // 4. Ghi ra file tạm trong bộ nhớ đệm (Cache Dir) của app
        // Tên file ngẫu nhiên để không bị trùng
        val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val fos = FileOutputStream(file)
        fos.write(bytes.toByteArray())
        fos.flush()
        fos.close()

        // 5. Trả về Uri của file mới
        return Uri.fromFile(file)
    }

    // Hàm phụ: Tính toán tỷ lệ để resize
    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Nếu ảnh nhỏ hơn maxDimension thì giữ nguyên
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        var newWidth = maxDimension
        var newHeight = maxDimension

        if (width > height) {
            // Ảnh ngang: Giữ chiều rộng max
            newHeight = (newWidth / ratio).toInt()
        } else {
            // Ảnh dọc: Giữ chiều cao max
            newWidth = (newHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}