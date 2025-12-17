package com.example.opencvproject

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView
    private val logBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = findViewById(R.id.tvLog)
        
        // --- ЗАПУСК ДИАГНОСТИКИ ---
        runDiagnostics()
    }

    private fun runDiagnostics() {
        log("🟦 STARTING DIAGNOSTICS...")

        // ЧЕКПОИНТ 1: Архитектура устройства
        log("\n--- CHECKPOINT 1: DEVICE INFO ---")
        val abis = Build.SUPPORTED_ABIS.joinToString(", ")
        log("Device ABIs: $abis")
        log("Android Version: SDK ${Build.VERSION.SDK_INT}")

        // ЧЕКПОИНТ 2: Поиск папки с библиотеками
        log("\n--- CHECKPOINT 2: NATIVE LIB PATH ---")
        val libPath = applicationInfo.nativeLibraryDir
        log("Expecting libs at: $libPath")
        
        val libFile = File(libPath, "libopencv_java4.so")
        if (libFile.exists()) {
            log("✅ FILE FOUND: libopencv_java4.so exists!")
            log("File size: ${libFile.length() / 1024} KB")
        } else {
            log("❌ FILE MISSING: libopencv_java4.so NOT found in native path.")
            log("Listing all files in dir:")
            try {
                val dir = File(libPath)
                val files = dir.listFiles()
                if (files.isNullOrEmpty()) {
                    log("  (Directory is empty)")
                } else {
                    files.forEach { log("  - ${it.name}") }
                }
            } catch (e: Exception) {
                log("  Error reading dir: ${e.message}")
            }
        }

        // ЧЕКПОИНТ 3: Стандартная инициализация
        log("\n--- CHECKPOINT 3: OpenCVLoader.initDebug() ---")
        try {
            val success = OpenCVLoader.initDebug()
            if (success) {
                log("✅ SUCCESS: OpenCVLoader initialized!")
            } else {
                log("❌ FAILURE: OpenCVLoader returned false.")
            }
        } catch (e: Exception) {
            log("❌ EXCEPTION in initDebug: ${e.message}")
        }

        // ЧЕКПОИНТ 4: Ручная загрузка (если нужно)
        if (!OpenCVLoader.initDebug()) {
            log("\n--- CHECKPOINT 4: Manual System.loadLibrary ---")
            try {
                System.loadLibrary("opencv_java4")
                log("✅ SUCCESS: System.loadLibrary loaded it manually!")
                log("Warning: initDebug() failed, but library is usable.")
            } catch (e: UnsatisfiedLinkError) {
                log("❌ CRITICAL ERROR: UnsatisfiedLinkError")
                log("Message: ${e.message}")
                log("This usually means the .so file is missing for architecture: ${Build.CPU_ABI}")
            } catch (e: Exception) {
                log("❌ ERROR: ${e.message}")
            }
        }

        log("\n🟦 DIAGNOSTICS FINISHED")
    }

    private fun log(message: String) {
        logBuilder.append(message).append("\n")
        runOnUiThread {
            tvLog.text = logBuilder.toString()
        }
    }
}
