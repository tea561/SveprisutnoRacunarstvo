#include <Arduino_LPS22HB.h> // Air pressure and temperature sensor
#include <Arduino_APDS9960.h> // Color, gesture, and proximity sensor
#include <Arduino_LSM9DS1.h> // Accelerometer


void setup() {
  Serial.begin(9600);
  while (!Serial);

  if (!BARO.begin()) {
    Serial.println("Failed to start the LPS22HB sensor.");
    while (1);
  }

  if (!APDS.begin()) {
    Serial.println("Failed to start the APDS9960 sensor.");
    while (1);
  }

  if (!IMU.begin()) {
    Serial.println("Failed to start the LSM9DS sensor.");
    while (1);
  }

}

void loop() {
  float pressure = BARO.readPressure(); // In kPa
  float temp = BARO.readTemperature();
    
  int gesture = APDS.readGesture();
  int proximity = APDS.readProximity();
  
  int r, g, b, a;
  APDS.readColor(r, g, b, a);

  float accX, accY, accZ;
  IMU.readAcceleration(accX, accY, accZ);

  float magX, magY, magZ;
  IMU.readMagneticField(magX, magY, magZ);

  float gyrX, gyrY, gyrZ;
  IMU.readGyroscope(gyrX, gyrY, gyrZ);

  while (!APDS.colorAvailable() || !APDS.proximityAvailable())
  {
  }

  Serial.print(pressure);
  Serial.print(',');
  Serial.print(temp); 
  Serial.print(',');
  Serial.print(proximity);
  Serial.print(',');
  Serial.print(gesture);
  Serial.print(',');
  Serial.print(r);
  Serial.print(',');
  Serial.print(g);
  Serial.print(',');
  Serial.print(b);
  Serial.print(',');
  Serial.print(a);
  Serial.print(',');
  Serial.print(accX);
  Serial.print(',');
  Serial.print(accY);
  Serial.print(',');
  Serial.print(accZ);
  Serial.print(',');
  Serial.print(magX);
  Serial.print(',');
  Serial.print(magY);
  Serial.print(',');
  Serial.print(magZ);
  Serial.print(',');
  Serial.print(gyrX);
  Serial.print(',');
  Serial.print(gyrY);
  Serial.print(',');
  Serial.print(gyrZ);
  Serial.println();

  delay(3000);
}
