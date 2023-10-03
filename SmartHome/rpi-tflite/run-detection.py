import sys
import numpy as np
import tflite_runtime.interpreter as tf
import cv2 
import os
import json
from dotenv import load_dotenv
import paho.mqtt.client as mqtt

load_dotenv()

detection_topic = os.environ['DETECTION_TOPIC']
mqtt_broker = os.environ['MQTT_BROKER']
mqtt_port = os.environ['MQTT_PORT']

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected to MQTT broker")
    else:
        print("Failed to connect to MQTT broker")

client = mqtt.Client()
client.on_connect = on_connect
client.connect(mqtt_broker, int(mqtt_port), 60)

if len(sys.argv) != 2:
    print("Usage: python script.py <image_file>")
    sys.exit(1)

try:
    image_path = sys.argv[1]
    #img = tf.keras.utils.load_img(
    #    image_path, target_size=(224, 224)
    #)
    #img_array = tf.keras.utils.img_to_array(img)
    img = cv2.imread(image_path)
    img = cv2.resize(img, (224,224))
    img = img / 255.0
    img_array = np.expand_dims(img, 0)
    img_array = np.float32(img_array)
    #print(img.shape)

    interpreter = tf.Interpreter(model_path='tf_lite_model.tflite')

    #print(interpreter.get_signature_list())


    classify_lite = interpreter.get_signature_runner('serving_default')
    classify_lite

    predictions_lite = classify_lite(input_1=img_array)['dense_3']
    #score_lite = tf.nn.softmax(predictions_lite)
    score_lite = np.argsort(predictions_lite[0])

    class_labels = ['cat', 'dog', 'person']
    predicted_class = class_labels[np.argmax(score_lite)]
    print(score_lite)
    if(predicted_class == 'person'):
        detection_data = {
                    "detected": "true"}
        client.publish(detection_topic, json.dumps(detection_data))
        print(f"Published to {detection_topic}: {detection_data}")

    # print(f'Predicted class: {predicted_class}')
    
except KeyboardInterrupt:
    print("Disconnecting from MQTT broker")
    client.disconnect()
