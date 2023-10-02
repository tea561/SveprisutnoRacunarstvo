package com.example.ubicompapplication

open class Constants {

    companion object {
        const val PREFERENCE_SMART_HOME = "smart home preference"
        const val CHANNEL_ID = "7"
        const val notificationId = 1
        const val NOTIFICATION_CODE = 100
        const val LOCAL_NETWORK_CODE = 101
        const val TOPIC = "ubicomp/#"
        const val CLIENT_ID = "mqtt_project23"
        const val SERVER_URI = "tcp://192.168.0.12:1884"
        const val TEMPERATURE_HIGH = 35.2
        const val TEMPERATURE_LOW = 27.5
        const val PRESSURE_HIGH = 103.5
        const val PRESSURE_LOW = 98.8
        const val TEMP_STREAM = "temp"
        const val TEMP_STREAM_VALUE = "temperature"
        const val PRESSURE_STREAM = "press"
        const val PRESSURE_STREAM_VALUE = "pressure"
        const val LOW_TEMP_ALARM = "alarmLowTemp"
        const val LOW_TEMP_VALUE = "avg"
        const val HIGH_TEMP_ALARM = "alarmHighTemp"
        const val PRESS_LIMITS_ALARM = "alarmPress"
        const val ACC_GYRO_STREAM = "motion"
        const val ACC_X_VALUE = "accX"
        const val ACC_Y_VALUE = "accY"
        const val ACC_Z_VALUE = "accZ"
        const val GYRO_Z_VALUE = "gyrZ"
        const val GYRO_Y_VALUE = "gyrY"
        const val MAG_Y_VALUE = "magY"
        const val MAG_Z_VALUE = "magZ"
        const val GYRO_X_VALUE = "gyrX"

        const val COLOR_STREAM = "alarmLight"
        const val COLOR_STREAM_VALUE = "brightness"
        const val GESTURE_STREAM = "gesture"
        const val LPS_STREAM = "lps"
        const val PROXIMITY_STREAM = "alarmProximity"
        const val PROXIMITY_STREAM_VALUE = "proximity"
        const val ROAD_CURVE_LIMIT = 0.5
        const val ACC_CURVE_LIMIT = 2.1
        const val LOCATION_PERMISSION_REQUEST_CODE = 2
        const val LED_SERVICE = "19B10010-E8F2-537E-4F6C-D104768A1214"
        const val LED_CHARACTERISTIC = "19B10011-E8F2-537E-4F6C-D104768A1214"
        const val BUTTON_CHARACTERISTIC = "19B10012-E8F2-537E-4F6C-D104768A1214"
    }

}