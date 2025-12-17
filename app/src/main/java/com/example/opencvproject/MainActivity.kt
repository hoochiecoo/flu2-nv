package com.example.opencvproject

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // Попытка инициализации OpenCV
        if (OpenCVLoader.initDebug()) {
            // Успех
            Log.d("OPENCV", "✅ OpenCV Loaded Successfully")
            tvStatus.text = "✅ OpenCV Loaded Successfully!"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            // Ошибка
            Log.e("OPENCV", "❌ OpenCV Failed to Load")
            
            // Пытаемся загрузить библиотеку вручную, чтобы поймать точную причину ошибки
            try {
                System.loadLibrary("opencv_java4")
                // Если мы попали сюда, значит библиотека физически есть, но что-то не так с инициализацией
                tvStatus.text = "Library loaded manually, but initDebug failed."
            } catch (e: UnsatisfiedLinkError) {
                // Самая частая ошибка: библиотека не найдена в APK
                tvStatus.text = "LINK ERROR: ${e.message}"
                Log.e("OPENCV", "Link Error: ${e.message}")
            } catch (e: Exception) {
                // Любая другая ошибка
                tvStatus.text = "Error: ${e.localizedMessage}"
                Log.e("OPENCV", "Error: ${e.localizedMessage}")
            }
            
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }
}
