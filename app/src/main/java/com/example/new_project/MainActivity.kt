package com.example.new_project

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.KeyboardType
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

    private var height by mutableStateOf(0f)
    private var age by mutableStateOf(0)
    private var weight by mutableStateOf(0f)

    private val coefficients = floatArrayOf(1f, 0.5f, 0.1f, 0.05f, 0.01f) // Example coefficients

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
                    Column {
                        UserInputForm { heightValue, ageValue, weightValue ->
                            height = heightValue
                            age = ageValue
                            weight = weightValue
                            Log.d("UserInput", "Height: $height, Age: $age, Weight: $weight")
                        }
                        SensorDataDisplay(accelerometerValues, magnetometerValues, gyroscopeValues, predictionResult)
                    }
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

        // Compute EEAC and EE
        val eeacValues = tfliteModel.computeEEAC(accelerometerValues.copyOfRange(0, 1), accelerometerValues.copyOfRange(1, 2), accelerometerValues.copyOfRange(2, 3))
        val eeValues = tfliteModel.computeEE(eeacValues, age.toFloat(), height, weight, coefficients)
        Log.d("EE Values", "EE Values: ${eeValues.joinToString(", ")}")
    }

    private fun prepareInput(acc: FloatArray, gyro: FloatArray, mag: FloatArray): FloatArray {
        // Assuming the model was trained on normalized data
        return floatArrayOf(
            // Normalize accelerometer data (example normalization)
            (acc[0] / SensorManager.GRAVITY_EARTH),
            (acc[1] / SensorManager.GRAVITY_EARTH),
            (acc[2] / SensorManager.GRAVITY_EARTH),
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
fun UserInputForm(onSubmit: (Float, Int, Float) -> Unit) {
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    Column {
        TextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(onClick = {
            onSubmit(height.toFloatOrNull() ?: 0f, age.toIntOrNull() ?: 0, weight.toFloatOrNull() ?: 0f)
        }) {
            Text("Submit")
        }
    }
}

@Composable
fun SensorDataDisplay(accelerometerValues: FloatArray, magnetometerValues: FloatArray, gyroscopeValues: FloatArray, predictions: FloatArray) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Accelerometer Data: ${accelerometerValues.joinToString(", ")}", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Magnetometer Data: ${magnetometerValues.joinToString(", ")}", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Gyroscope Data: ${gyroscopeValues.joinToString(", ")}", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Predictions: ${predictions.joinToString(", ")}", style = MaterialTheme.typography.headlineMedium)
    }
}
