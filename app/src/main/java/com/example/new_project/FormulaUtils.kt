package com.example.new_project

import kotlin.math.pow
import kotlin.math.sqrt

object FormulaUtils {

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
