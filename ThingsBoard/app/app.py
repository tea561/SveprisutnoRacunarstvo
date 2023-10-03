import csv
from time import time
import serial
import os
import json
import paho.mqtt.client as mqtt
from dotenv import load_dotenv

load_dotenv()

ser = serial.Serial('/dev/ttyACM0', timeout=1)
edge_host = os.environ['HOST']
thermostat_access_token = os.environ['THERMOSTAT_ACCESS_TOKEN']
pressure_access_token = os.environ['PRESSURE_ACCESS_TOKEN']
proximity_access_token = os.environ['PROXIMITY_ACCESS_TOKEN']
imu_access_token = os.environ['IMU_ACCESS_TOKEN']
access_token = os.environ['ACCESS_TOKEN']
edge_mqtt_port = os.environ['EDGE_MQTT_PORT']
telemetry_topic = os.environ['TELEMETRY_TOPIC']

f = open("sensor_data.csv", "a+")
writer = csv.writer(f, delimiter=',')

headers = ['Timestamp', 'Temperature', 'Pressure', 'Proximity', 'R', 'B', 'G', 'A', 'xAcc', 'yAcc', 'zAcc']
writer.writerow(headers)

client = mqtt.Client()
temperature_client = mqtt.Client()
pressure_client = mqtt.Client()
proximity_client = mqtt.Client()
imu_client = mqtt.Client()

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("${client}: Connected to MQTT broker")
    else:
        print("${client}: Failed to connect to MQTT broker")
        
def on_disconnect(client, userdate, rc):
    client.connect(edge_host, int(edge_mqtt_port), 60) 

temperature_client.username_pw_set(username=thermostat_access_token)
pressure_client.username_pw_set(username=pressure_access_token)
proximity_client.username_pw_set(username=proximity_access_token)
imu_client.username_pw_set(username=imu_access_token)
client.username_pw_set(username=access_token)

temperature_client.on_connect = on_connect
pressure_client.on_connect = on_connect
proximity_client.on_connect = on_connect
imu_client.on_connect = on_connect
client.on_connect = on_connect

temperature_client.on_disconnect = on_disconnect
pressure_client.on_disconnect = on_disconnect
proximity_client.on_disconnect = on_disconnect
imu_client.on_disconnect = on_disconnect
client.on_disconnect = on_disconnect

temperature_client.connect(edge_host, int(edge_mqtt_port), 60)
pressure_client.connect(edge_host, int(edge_mqtt_port), 60)
proximity_client.connect(edge_host, int(edge_mqtt_port), 60)
imu_client.connect(edge_host, int(edge_mqtt_port), 60)
client.connect(edge_host, int(edge_mqtt_port), 60)

try:
    while True:
        s = ser.readline().decode()
        if s != "":
            rows = [float(x) for x in s.split(',')]
            rows.insert(0, int(time()))
            print(rows)
            writer.writerow(rows)

            timestamp = rows[0]

            pressure = json.dumps({
                "timestamp": timestamp,
                "pressure": rows[1]
            })

            temperature = json.dumps({
                "timestamp": timestamp,
                "temperature": rows[2],
            })

            proximity = json.dumps({
                "timestamp": timestamp,
                "proximity": rows[3],
                "gesture": rows[4],
                "r": rows[5],
                "g": rows[6],
                "b": rows[7],
                "a": rows[8],
            })

            imu = json.dumps({
                "timestamp": timestamp,
                "accX": rows[9],
                "accY": rows[10],
                "accZ": rows[11],
                "magX": rows[12],
                "magY": rows[13],
                "magZ": rows[14],
                "gyrX": rows[15],
                "gyrY": rows[16],
                "gyrZ": rows[17]
            })

            payload = json.dumps({
                "timestamp": rows[0],
                "temperature": rows[1],
                "pressure": rows[2],
                "proximity": rows[3],
                "r": rows[4],
                "g": rows[5],
                "b": rows[6],
                "a": rows[7],
                "xAcc": rows[8],
                "yAcc": rows[9],
                "zAcc": rows[10]
            })

            temperature_client.publish(telemetry_topic, temperature)
            print(f"Published - temperature: {temperature}")
            pressure_client.publish(telemetry_topic, pressure)
            print(f"Published - pressure: {pressure}")
            proximity_client.publish(telemetry_topic, proximity)
            print(f"Published - proximity: {proximity}")
            imu_client.publish(telemetry_topic, imu)
            print(f"Published - imu: {imu}")
            client.publish(telemetry_topic, payload)
            print(f"Published: {payload}")
            f.flush()
except KeyboardInterrupt:
    # Gracefully disconnect from the MQTT broker on Ctrl+C
    print("Disconnecting from MQTT broker")
    temperature_client.disconnect()
    pressure_client.disconnect()
    proximity_client.disconnect()
    imu_client.disconnect()
