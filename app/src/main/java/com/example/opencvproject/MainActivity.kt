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

        if (OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "✅ OpenCV Loaded Successfully")
            tvStatus.text = "✅ OpenCV Loaded Successfully!"
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            Log.e("OPENCV", "❌ OpenCV Failed to Load")
            tvStatus.text = "❌ OpenCV Failed to Load"
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }
}
