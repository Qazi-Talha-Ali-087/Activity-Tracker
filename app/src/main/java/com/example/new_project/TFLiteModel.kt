package com.example.new_project

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteModel(context: Context) {
    private var interpreter: Interpreter? = null

    init {
        try {
            val assetFileDescriptor = context.assets.openFd("model_flex.tflite")
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            interpreter = Interpreter(modelBuffer)
            Log.d("TFLiteModel", "Model loaded successfully.")
        } catch (e: Exception) {
            Log.e("TFLiteModel", "Failed to load model.", e)
        }
    }


    fun predict(input: FloatArray): FloatArray {
        val output = FloatArray(1)
        interpreter?.run(input, output)
        return output
    }
}
