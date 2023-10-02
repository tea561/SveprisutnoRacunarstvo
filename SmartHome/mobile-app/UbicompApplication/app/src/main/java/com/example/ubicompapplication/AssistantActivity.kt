package com.example.ubicompapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.atan2

class AssistantActivity : AppCompatActivity() {

    private lateinit var tvHomeAssistantValue: TextView
    private lateinit var tvSecurityValue: TextView
    private lateinit var tvMotionTimeValue: TextView
    private lateinit var tvCameraTimeValue: TextView

    private lateinit var mqttClient: MqttConnection
    private lateinit var preferences: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    private lateinit var bottomMenu: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistant)

        bottomMenu = findViewById(R.id.nav_bottom)

        bottomMenu.menu.findItem(R.id.assistantFragment).isChecked = true

        bottomMenu.menu.findItem(R.id.hvacFragment).setOnMenuItemClickListener {
            val intent = Intent(this@AssistantActivity , ControlsActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        bottomMenu.menu.findItem(R.id.homeFragment).setOnMenuItemClickListener {
            val intent = Intent(this@AssistantActivity, HomeActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        bottomMenu.menu.findItem(R.id.lightsFragment).setOnMenuItemClickListener {
            val intent = Intent(this@AssistantActivity, LightsActivity::class.java)
            startActivity(intent)
            return@setOnMenuItemClickListener false
        }

        tvHomeAssistantValue = findViewById(R.id.tv_home_assistant_value)
        tvSecurityValue = findViewById(R.id.tv_security_value)
        tvCameraTimeValue = findViewById(R.id.tv_camera_time_value)
        tvMotionTimeValue = findViewById(R.id.tv_motion_time_value)

        preferences = this.getSharedPreferences(Constants.PREFERENCE_SMART_HOME, Context.MODE_PRIVATE)
        edit = preferences.edit()

        mqttClient = MqttConnection(applicationContext, arrayOf(Constants.TOPIC), IntArray(1) {0})
        mqttClient.init()
        initMqttStatusListener()
        mqttClient.connect()

        if(preferences.getBoolean("doors_locked", false)) {
            changeSecuritySystemValue("Activated")
            changeAssistantValue("Deactivated")
        }
        else {
            changeAssistantValue("Activated")
            changeSecuritySystemValue("Deactivated")
        }
    }

    private fun initMqttStatusListener() {
        mqttClient.mqttStatusListener = object : MqttStatusListener {

            override fun onConnectFailure(exception: Throwable) {
                displayInDebugLog("Failed to connect")
            }

            override fun onConnectionLost(exception: Throwable) {
                displayInDebugLog("The Connection was lost.")
            }

            @SuppressLint("SetTextI18n")
            override fun onMessageArrived(topic: String, message: MqttMessage) {
                if(topic.contains("alarmLight")) {
                    receivedLightAlarm(message)
                }
                if(topic.contains("motion")) {
                    receivedMovement(message)
                }
                if(topic.contains("alarmProximity")) {
                    receivedProximityAlarm(message)
                }
                displayInMessagesList(String(message.payload))
            }

            override fun onDisconnectService() {
                displayInDebugLog("The Connection was stopped.")
            }

            override fun onTopicSubscriptionSuccess() {
                displayInDebugLog("Subscribed!")
            }

            override fun onTopicSubscriptionError(exception: Throwable) {
                displayInDebugLog("Failed to subscribe")
            }
        }
    }

    private fun displayInMessagesList(message: String) {
        Log.d("new received message", message)
    }

    private fun displayInDebugLog(message: String) {
        Log.i("display in debug", message)
    }

    @SuppressLint("SetTextI18n")
    private fun receivedMovement(message: MqttMessage) {
//        val currentAt = readRuleValue(String(message.payload), Constants.ACC_GYRO_STREAM)[1]
//        val currentAn = readRuleValue(String(message.payload), Constants.ACC_GYRO_STREAM)[0]
//        val omega = readRuleValue(String(message.payload), Constants.ACC_GYRO_STREAM)[5]
        val currentAt = readSingleRuleValue(String(message.payload), Constants.ACC_X_VALUE).toDouble()
        val currentAn = readSingleRuleValue(String(message.payload), Constants.ACC_Z_VALUE).toDouble()
        val omega = readSingleRuleValue(String(message.payload), Constants.GYRO_Y_VALUE).toDouble()

        val currentRadius = currentAn.toDouble() / (omega.toDouble() * omega.toDouble())

        val magY = readSingleRuleValue(String(message.payload), Constants.MAG_Y_VALUE).toDouble()
        val magZ = readSingleRuleValue(String(message.payload), Constants.MAG_Z_VALUE).toDouble()
        val inclination = atan2(magY, magZ)
//        if (inclination > 0) {
//            Toast.makeText(this@AssistantActivity, "North!", Toast.LENGTH_SHORT).show()
//        }
//        else {
//            Toast.makeText(this@AssistantActivity, "South!", Toast.LENGTH_SHORT).show()
//        }
//        tvAccelerationValue.text = "$currentAt m/s^2"
//        if(currentAt.toDouble() < 0.0) {
//            tvSpeedValue.text = "speeding up"
//        } else if (currentAt.toDouble() > 0.0) {
//            tvSpeedValue.text = "slowing down"
//        } else {
//            tvSpeedValue.text = "constant speed"
//        }
//        tvGyroscopeValue.text = "${String.format("%.2f", currentRadius)} m"

//        if(currentRadius > Constants.ROAD_CURVE_LIMIT && currentAt.toDouble() > Constants.ACC_CURVE_LIMIT) {
//            tvESC.text = "activated"
//            ivESCActive.visibility = View.VISIBLE
//            //send command for ESC notification
//            sendIntCommand(bluetoothGatt, 3)
//            startForegroundService()
//            val animation = AlphaAnimation(0.5f, 0f)
//            animation.duration = 500
//            animation.interpolator = LinearInterpolator()
//            animation.repeatCount = 4
//            animation.repeatMode = Animation.REVERSE
//            ivESCActive.startAnimation(animation)
//            Handler(Looper.getMainLooper()).postDelayed({
//                ivESCActive.clearAnimation()
//                ivESCActive.visibility = View.INVISIBLE
//                tvESC.text = "deactivated"
//            }, 2_000)
//        }

        if(currentRadius > 0) {
            edit.putBoolean("garage", true)
            edit.commit()
            Toast.makeText(this@AssistantActivity, "Garage doors opened!", Toast.LENGTH_SHORT).show()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun receivedProximityAlarm(message: MqttMessage) {

            if(!preferences.getBoolean("doors_locked", false))
            {
                edit.putBoolean("assistant", true)
                changeAssistantValue("Activated")
                edit.putBoolean("lights", true)
                edit.putBoolean("hvac", true)
                edit.commit()
            }
            else {
                setMotionTimeValue()
            }

    }



    @SuppressLint("SetTextI18n")
    private fun receivedLightAlarm(message: MqttMessage) {
        if(!preferences.getBoolean("doors_locked", false)) {
            if(readSingleRuleValue(String(message.payload), Constants.COLOR_STREAM_VALUE).toDouble() < 0.3) {
                edit.putBoolean("lights", true)
                edit.commit()
                //notify user to turn on the lights
                //sendIntCommand(bluetoothGatt, 5)
                Toast.makeText(this@AssistantActivity, "Lights turned on!", Toast.LENGTH_SHORT).show()
            } else {
                edit.putBoolean("lights", false)
                edit.commit()
                Toast.makeText(this@AssistantActivity, "Lights turned off!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeAssistantValue(text: String)
    {
        tvHomeAssistantValue.text = text
    }

    private fun changeSecuritySystemValue(text: String)
    {
        tvSecurityValue.text = text
    }

    private fun setMotionTimeValue()
    {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val formatted = sdf.format(Date())

        tvMotionTimeValue.text = formatted
    }

    private fun setCameraTimeValue()
    {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val formatted = sdf.format(Date())

        tvCameraTimeValue.text = formatted
    }

    private fun readRuleValue(payload: String, key: String): ArrayList<String> {
        val listOfValues = arrayListOf<String>()
        val jsonObject: JsonObject = Json.decodeFromString(payload)
        val values = jsonObject[key].toString()
        listOfValues.add(values.split(',').toString().replace(" ", ""))
        return listOfValues
    }

    private fun readSingleRuleValue(payload: String, key: String): String {
        val jsonObject: JsonObject = Json.decodeFromString(payload)
        return jsonObject[key].toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttClient.disconnect()
    }

}