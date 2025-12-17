package com.example.opencv_flutter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.Surface
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.TextureRegistry
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import kotlin.concurrent.thread

class MainActivity : FlutterActivity() {
    private val CHANNEL_METHODS = "com.example.opencv/methods"
    private val CHANNEL_EVENTS = "com.example.opencv/events"

    private var eventSink: EventChannel.EventSink? = null
    private var isRunning = false
    private var textureEntry: TextureRegistry.SurfaceTextureEntry? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // 1. MethodChannel для команд (Start/Stop)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_METHODS).setMethodCallHandler { call, result ->
            if (call.method == "startOpenCV") {
                if (OpenCVLoader.initDebug()) {
                    // Создаем Flutter Texture
                    textureEntry = flutterEngine.renderer.textures.createSurfaceTexture()
                    val textureId = textureEntry!!.id()
                    
                    // Запускаем отрисовку
                    startRendering(textureEntry!!)
                    
                    // Возвращаем ID текстуры во Flutter
                    result.success(textureId)
                } else {
                    result.error("OPENCV_ERR", "Failed to init OpenCV", null)
                }
            } else if (call.method == "stopOpenCV") {
                isRunning = false
                textureEntry?.release()
                result.success(null)
            } else {
                result.notImplemented()
            }
        }

        // 2. EventChannel для потока данных
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL_EVENTS).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                }
                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }
            }
        )
    }

    // Логика отрисовки (Демонстрация)
    private fun startRendering(entry: TextureRegistry.SurfaceTextureEntry) {
        if (isRunning) return
        isRunning = true

        val surfaceTexture = entry.surfaceTexture()
        val width = 640
        val height = 480
        surfaceTexture.setDefaultBufferSize(width, height)
        val surface = Surface(surfaceTexture)

        thread {
            // Создаем OpenCV матрицу (изображение)
            val mat = Mat(height, width, CvType.CV_8UC4)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            var x = 0.0
            var step = 15.0

            while (isRunning) {
                try {
                    // 1. Рисуем что-то с помощью OpenCV (симуляция обработки)
                    mat.setTo(Scalar(20.0, 20.0, 20.0, 255.0)) // Темный фон

                    x += step
                    if (x > width || x < 0) step = -step
                    
                    // Рисуем красный круг
                    Imgproc.circle(mat, Point(x, height / 2.0), 60, Scalar(255.0, 0.0, 0.0, 255.0), -1)
                    
                    // Рисуем текст
                    Imgproc.putText(mat, "OpenCV + Flutter", Point(50.0, 100.0), 
                        Imgproc.FONT_HERSHEY_SIMPLEX, 1.5, Scalar(255.0, 255.0, 255.0, 255.0), 2)

                    // 2. Конвертируем Mat -> Bitmap
                    Utils.matToBitmap(mat, bitmap)

                    // 3. Отправляем Bitmap во Flutter через Surface
                    val canvas: Canvas? = surface.lockCanvas(null)
                    if (canvas != null) {
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                        surface.unlockCanvasAndPost(canvas)
                    }

                    // Ограничиваем FPS
                    Thread.sleep(30) 

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            mat.release()
            surface.release()
        }
    }
}
