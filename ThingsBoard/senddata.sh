mosquitto_pub -d -q 1 -h "$HOST_NAME" -p "$MQTT_PORT" -t "v1/devices/me/telemetry" -u "$ACCESS_TOKEN" -m {"temperature":25}
mosquitto_pub -d -q 1 -h "localhost" -p "1883" -t "v1/devices/me/telemetry" -u "24UnIzqHKp65QA6n1zTL" -m {"temperature":25}
mosquitto_pub -d -q 1 -h "localhost" -p "1884" -t "topic1" -m {"temperature":25}
mosquitto_pub -d -q 1 -h "localhost" -p "11883" -t "v1/devices/me/telemetry" -u "24UnIzqHKp65QA6n1zTL" -m {"temperature":50}
mosquitto_pub -d -q 1 -h "localhost" -p "11883" -t "v1/devices/me/telemetry" -u "SOZKk53XCKPEuBFgCE1z" -m {"pressure":50}
