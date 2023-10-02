package com.example.ubicompapplication

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

interface MqttManager {
    fun init()
    fun connect()
    fun sendMessage(message: String, topic: String)
}

open class MqttConnection(val context: Context, val topics: Array<String>, val topicQos: IntArray): MqttManager {

    private lateinit var mqttAndroid: MqttAndroidClient
    var mqttStatusListener: MqttStatusListener? = null

    override fun init() {
        val serverURI = Constants.SERVER_URI
        mqttAndroid = MqttAndroidClient(context, serverURI, Constants.CLIENT_ID)
        mqttAndroid.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("AndroidMqtt", "Receive message: ${message.toString()} from topic: $topic")
                if(topic != null && message != null) {
                    mqttStatusListener?.onMessageArrived(topic = topic!!, message = message!!)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d("AndroidMqtt", "Connection lost ${cause.toString()}")
                mqttStatusListener?.onConnectionLost(exception = cause!!)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
    }

    override fun connect() {
        val options = MqttConnectOptions()
        try {
            mqttAndroid.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("AndroidMqtt", "Connection success")
                    subscribe()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d("AndroidMqtt", "Connection failure ${exception!!.printStackTrace()}")
                    mqttStatusListener?.onConnectFailure(exception = exception)
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    override fun sendMessage(message: String, topic: String) {
        mqttAndroid.let {
            try {
                val mqttMessage = MqttMessage().apply {
                    payload = message.toByteArray()
                }
                it.publish(topic, mqttMessage)
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    fun subscribe() {
        try {
            mqttAndroid.subscribe(topics, topicQos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    mqttStatusListener?.onTopicSubscriptionSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    mqttStatusListener?.onTopicSubscriptionError(exception)
                }
            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    fun receiveMessages() {
        mqttAndroid.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                //connectionStatus = false
                // Give your callback on failure here
            }
            override fun messageArrived(topic: String, message: MqttMessage) {
                try {
                    val data = String(message.payload, charset("UTF-8"))
//                    response = readRuleValue(data, stream)
                } catch (e: Exception) {
                    // Give your callback on error here
                }
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                // Acknowledgement on delivery complete
            }
        })
    }

    fun disconnect() {
        try {
            val disconnectToken = mqttAndroid.disconnect()
            disconnectToken.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    mqttStatusListener?.onDisconnectService()
                    // Give Callback on disconnection here
                }
                override fun onFailure(
                    asyncActionToken: IMqttToken,
                    exception: Throwable
                ) {
                    // Give Callback on error here
                }
            }
        } catch (e: MqttException) {
            // Give Callback on error here
        }
    }
}