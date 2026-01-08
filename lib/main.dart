import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const OpenCVPage(),
    );
  }
}

class OpenCVPage extends StatefulWidget {
  const OpenCVPage({super.key});

  @override
  State<OpenCVPage> createState() => _OpenCVPageState();
}

class _OpenCVPageState extends State<OpenCVPage> {
  // Каналы связи с Native Kotlin
  static const methodChannel = MethodChannel('com.example.opencv/methods');
  static const eventChannel = EventChannel('com.example.opencv/events');

  int? _textureId;
  String _status = "Нажмите Start для запуска OpenCV";
  StreamSubscription? _subscription;

  Future<void> _startOpenCV() async {
    try {
      setState(() => _status = "Инициализация...");
      
      // Запрашиваем создание текстуры у Android
      final int textureId = await methodChannel.invokeMethod('startOpenCV');
      
      setState(() {
        _textureId = textureId;
        _status = "OpenCV работает. Texture ID: $textureId";
      });

      // Слушаем данные от OpenCV
      _subscription = eventChannel.receiveBroadcastStream().listen((event) {
        // Обновляем UI данными из натива (например, координаты)
        // (Для оптимизации лучше не делать setState слишком часто)
      });

    } catch (e) {
      setState(() => _status = "Ошибка: $e");
    }
  }

  Future<void> _stopOpenCV() async {
    await methodChannel.invokeMethod('stopOpenCV');
    _subscription?.cancel();
    setState(() {
      _textureId = null;
      _status = "OpenCV остановлен";
    });
  }

  @override
  void dispose() {
    _stopOpenCV();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Flutter + OpenCV Texture")),
      body: Column(
        children: [
          Expanded(
            child: Container(
              margin: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                border: Border.all(color: Colors.blueAccent, width: 2),
                color: Colors.black,
              ),
              child: _textureId == null
                  ? const Center(child: Text("Камера выключена", style: TextStyle(color: Colors.white)))
                  : Texture(textureId: _textureId!), // 🔥 Рендер видео из OpenCV
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Text(_status, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              ElevatedButton.icon(
                onPressed: _textureId == null ? _startOpenCV : null,
                icon: const Icon(Icons.play_arrow),
                label: const Text("Start OpenCV"),
              ),
              ElevatedButton.icon(
                onPressed: _textureId != null ? _stopOpenCV : null,
                icon: const Icon(Icons.stop),
                style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                label: const Text("Stop"),
              ),
            ],
          ),
          const SizedBox(height: 20),
        ],
      ),
    );
  }
}
