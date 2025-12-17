package com.example.opencvproject

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация (мы знаем, что она работает, но проверка нужна)
        OpenCVLoader.initDebug()

        val imageView = findViewById<ImageView>(R.id.imageView)
        val btnProcess = findViewById<Button>(R.id.btnProcess)

        // 1. Создаем тестовую картинку (Bitmap) программно
        // (Желтый квадрат на синем фоне)
        val originalBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(originalBitmap)
        canvas.drawColor(Color.BLUE) // Фон
        val paint = android.graphics.Paint().apply { color = Color.YELLOW }
        canvas.drawRect(100f, 100f, 400f, 400f, paint) // Квадрат

        // Показываем оригинал
        imageView.setImageBitmap(originalBitmap)

        btnProcess.setOnClickListener {
            // --- МАГИЯ OPENCV НАЧИНАЕТСЯ ЗДЕСЬ ---
            
            // 1. Конвертируем Android Bitmap -> OpenCV Mat
            val src = Mat()
            Utils.bitmapToMat(originalBitmap, src)

            // 2. Делаем черно-белым (RGB -> Gray)
            val gray = Mat()
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY)

            // 3. Размываем, чтобы убрать шум (Gaussian Blur)
            Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

            // 4. Находим границы (Canny Edge Detection)
            val edges = Mat()
            Imgproc.Canny(gray, edges, 80.0, 100.0)

            // 5. Конвертируем обратно OpenCV Mat -> Android Bitmap
            // (edges - это одноканальное изображение, создадим для него Bitmap)
            val resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(edges, resultBitmap)

            // Показываем результат
            imageView.setImageBitmap(resultBitmap)
            
            // Освобождаем память (в C++ это важно!)
            src.release()
            gray.release()
            edges.release()
        }
    }
}
