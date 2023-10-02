package com.example.ubicompapplication

import org.eclipse.paho.client.mqttv3.MqttMessage

interface MqttStatusListener {
    fun onConnectFailure(exception: Throwable)
    fun onConnectionLost(exception: Throwable)
    fun onTopicSubscriptionSuccess()
    fun onTopicSubscriptionError(exception: Throwable)
    fun onMessageArrived(topic: String, message: MqttMessage)
    fun onDisconnectService()
}