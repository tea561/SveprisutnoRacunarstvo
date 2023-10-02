mosquitto_pub -d -q 1 -h "$HOST_NAME" -p "$MQTT_PORT" -t "v1/devices/me/telemetry" -u "$ACCESS_TOKEN" -m {"temperature":25}
mosquitto_pub -d -q 1 -h "localhost" -p "1883" -t "v1/devices/me/telemetry" -u "fogj2nYHgg4b1OAUYVs6" -m {"temperature":25}
mosquitto_pub -d -q 1 -h "localhost" -p "1884" -t "topic1" -m {"temperature":25}