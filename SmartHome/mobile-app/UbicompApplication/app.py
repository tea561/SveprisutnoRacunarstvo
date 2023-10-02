import paho.mqtt.client as mqtt 
import time

def on_message(client, userdata, message):
    print("message received " ,str(message.payload.decode("utf-8")))
    print("message topic=",message.topic)
    print("message qos=",message.qos)
    print("message retain flag=",message.retain)

broker_address="127.0.0.1"
broker_port=1883
print("creating new instance")
client = mqtt.Client("mqtt-android")
client.on_message=on_message
print("connecting to broker")
client.connect(broker_address, broker_port)
# client.loop_start() #start the loop
print("Subscribing to topic","android/test")
client.subscribe("android/test")
# print("Publishing message to topic","android/test")
# client.publish("android/test", 25)
# time.sleep(1) # wait
client.loop_forever() #stop the loop