import os
import json
from time import time
import csv
import serial
from dotenv import load_dotenv
import paho.mqtt.client as mqtt
import influxdb_client
from influxdb_client import InfluxDBClient, Point, WritePrecision
from influxdb_client.client.write_api import SYNCHRONOUS



load_dotenv()

ser = serial.Serial('/dev/ttyACM0', timeout=1)


lps_topic = os.environ['LPS_TOPIC']
apds_topic = os.environ['APDS_TOPIC']
imu_topic = os.environ['IMU_TOPIC']
mqtt_broker = os.environ['MQTT_BROKER']
mqtt_port = os.environ['MQTT_PORT']

token = os.environ['INFLUXDB_TOKEN']
org = os.environ['INFLUXDB_ORG']
influxdb_url = os.environ['INFLUXDB_URL']
bucket = 'default'

write_client = influxdb_client.InfluxDBClient(url=influxdb_url, token=token, org=org)
write_api = write_client.write_api(write_options=SYNCHRONOUS)


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected to MQTT broker")
    else:
        print("Failed to connect to MQTT broker")

client = mqtt.Client()
client.on_connect = on_connect
client.connect(mqtt_broker, int(mqtt_port), 60)

try:
    while True:
        s = ser.readline().decode()
        if s != "":
            rows = [float(x) for x in s.split(',')]
            print(rows)
            
            timestamp = int(time())

            lps_data = {
                "timestamp": timestamp,
                "pressure": rows[0],
                "temperature": rows[1],
                "sensorId": "3ff65fcb-36ee-4098-ab41-7263ac56fb23"
            }

            apds_data = {
                "timestamp": timestamp,
                "proximity": rows[2],
                "gesture": rows[3],
                "r": rows[4],
                "g": rows[5],
                "b": rows[6],
                "a": rows[7],
                "sensordId": "26ebedaa-f82b-4d4c-8e19-34f36d646578"
            }

            imu_data = {
                "timestamp": timestamp,
                "accX": rows[8],
                "accY": rows[9],
                "accZ": rows[10],
                "magX": rows[11],
                "magY": rows[12],
                "magZ": rows[13],
                "gyrX": rows[14],
                "gyrY": rows[15],
                "gyrZ": rows[16],
                "sensordId": "a55589be-9533-4fcd-afc2-57c0166e7134"
            }

            client.publish(lps_topic, json.dumps(lps_data))
            print(f"Published to {lps_topic}: {lps_data}")

            point = (
                Point("lps")
                .tag("sensorId", "3ff65fcb-36ee-4098-ab41-7263ac56fb23")
                .field("pressure", lps_data["pressure"])
                .field("temperature", lps_data["temperature"])
            )
            write_api.write(bucket=bucket, org=org, record=point)

            client.publish(apds_topic, json.dumps(apds_data))
            print(f"Published to {apds_topic}: {apds_data}")

            point = (
                Point("apds")
                .tag("sensorId", "26ebedaa-f82b-4d4c-8e19-34f36d646578")
                .field("proximity", apds_data["proximity"])
                .field("gesture", apds_data["gesture"])
                .field("r", apds_data["r"])
                .field("g", apds_data["g"])
                .field("b", apds_data["b"])
                .field("a", apds_data["a"])
            )
            write_api.write(bucket=bucket, org=org, record=point)


            client.publish(imu_topic, json.dumps(imu_data))
            print(f"Published to {imu_topic}: {imu_data}")

            point = (
                Point("imu")
                .tag("sensorId", "a55589be-9533-4fcd-afc2-57c0166e7134")
                .field("accX", imu_data["accX"])
                .field("accY", imu_data["accY"])
                .field("accZ", imu_data["accZ"])
                .field("magX", imu_data["magX"])
                .field("magY", imu_data["magY"])
                .field("magZ", imu_data["magZ"])
                .field("gyrX", imu_data["gyrX"])
                .field("gyrY", imu_data["gyrY"])
                .field("gyrZ", imu_data["gyrZ"])
            )
            write_api.write(bucket=bucket, org=org, record=point)



except KeyboardInterrupt:
    print("Disconnecting from MQTT broker")
    client.disconnect()
