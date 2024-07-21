package com.example.new_project

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.pow
import kotlin.math.sqrt

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

    private fun smooth(data: FloatArray, windowSize: Int = 5): FloatArray {
        val smoothed = FloatArray(data.size - windowSize + 1)
        for (i in 0 until data.size - windowSize + 1) {
            var sum = 0f
            for (j in 0 until windowSize) {
                sum += data[i + j]
            }
            smoothed[i] = sum / windowSize
        }
        return smoothed
    }

    fun computeEEAC(ax: FloatArray, ay: FloatArray, az: FloatArray): FloatArray {
        val norms = FloatArray(ax.size)
        for (i in ax.indices) {
            norms[i] = sqrt(ax[i].pow(2) + ay[i].pow(2) + az[i].pow(2))
        }
        return smooth(norms)
    }

    fun computeEE(eeac: FloatArray, age: Float, height: Float, weight: Float, coefficients: FloatArray): FloatArray {
        val (b0, b1, b2, b3, b4) = coefficients
        return eeac.map { eeacValue ->
            b0 + b1 * eeacValue + b2 * age * eeacValue + b3 * height * eeacValue + b4 * weight * eeacValue
        }.toFloatArray()
    }
}
