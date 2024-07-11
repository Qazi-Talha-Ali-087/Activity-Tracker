package com.example.new_project

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.GRAVITY_EARTH
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.new_project.ui.theme.New_ProjectTheme

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var tfliteModel: TFLiteModel
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gyroscope: Sensor? = null

    var accelerometerValues by mutableStateOf(floatArrayOf(0f, 0f, 0f))
    var magnetometerValues by mutableStateOf(floatArrayOf(0f, 0f, 0f))
    var gyroscopeValues by mutableStateOf(floatArrayOf(0f, 0f, 0f))
    var predictionResult by mutableStateOf(floatArrayOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        tfliteModel = TFLiteModel(this)

        // Initialize sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Check for sensor availability
        if (accelerometer == null || magnetometer == null || gyroscope == null) {
            Log.e("Sensors", "One or more required sensors are not available on this device.")
        }

        setContent {
            New_ProjectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SensorDataDisplay(accelerometerValues, magnetometerValues, gyroscopeValues, predictionResult)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        magnetometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerValues = event.values.clone()
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroscopeValues = event.values.clone()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetometerValues = event.values.clone()
            }
        }
        val input = prepareInput(accelerometerValues, gyroscopeValues, magnetometerValues)
        predictionResult = tfliteModel.predict(input)
    }

    private fun prepareInput(acc: FloatArray, gyro: FloatArray, mag: FloatArray): FloatArray {
        // Assuming the model was trained on normalized data
        return floatArrayOf(
            // Normalize accelerometer data (example normalization)
            (acc[0] / GRAVITY_EARTH),
            (acc[1] / GRAVITY_EARTH),
            (acc[2] / GRAVITY_EARTH),
            // Normalize gyroscope data (if necessary)
            gyro[0],
            gyro[1],
            gyro[2],
            // Normalize magnetometer data (if necessary)
            mag[0],
            mag[1],
            mag[2]
        )
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Can be implemented if necessary
    }
}

@Composable
fun SensorDataDisplay(accelerometerValues: FloatArray, magnetometerValues: FloatArray, gyroscopeValues: FloatArray, predictions: FloatArray) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Accelerometer Data:", style = MaterialTheme.typography.headlineMedium)
        Text(text = "X: ${accelerometerValues[0]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Y: ${accelerometerValues[1]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Z: ${accelerometerValues[2]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Magnetometer Data:", style = MaterialTheme.typography.headlineMedium)
        Text(text = "X: ${magnetometerValues[0]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Y: ${magnetometerValues[1]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Z: ${magnetometerValues[2]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Gyroscope Data:", style = MaterialTheme.typography.headlineMedium)
        Text(text = "X: ${gyroscopeValues[0]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Y: ${gyroscopeValues[1]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Z: ${gyroscopeValues[2]}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Prediction: ${predictions.joinToString(", ")}", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    New_ProjectTheme {
        SensorDataDisplay(
            floatArrayOf(1.0f, 2.0f, 3.0f),
            floatArrayOf(4.0f, 5.0f, 6.0f),
            floatArrayOf(7.0f, 8.0f, 9.0f),
            floatArrayOf(0.0f)
        )
    }
}
