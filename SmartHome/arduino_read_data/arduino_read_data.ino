#include <Arduino_LPS22HB.h> // Air pressure and temperature sensor
#include <Arduino_APDS9960.h> // Color, gesture, and proximity sensor
#include <Arduino_LSM9DS1.h> // Accelerometer
#include <ArduinoBLE.h>

const int ledPin = LED_BUILTIN;
const int buttonPin = 4;

BLEService ledService("19B10010-E8F2-537E-4F6C-D104768A1214");
BLEByteCharacteristic ledCharacteristic("19B10011-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLEByteCharacteristic buttonCharacteristic("19B10012-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify);

void blinkLED(int pin)
{
  for (int i = 0; i < 5; i++)
  {
    digitalWrite(pin, HIGH);
    delay(500);
    digitalWrite(pin, LOW);
    delay(500);
  }
}

void offLED()
{
  digitalWrite(LEDG, HIGH);
  digitalWrite(LEDR, HIGH);
  digitalWrite(LEDB, HIGH);
}

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

  pinMode(ledPin, OUTPUT); // use the LED as an output
  pinMode(buttonPin, INPUT); // use button pin as an input

  // begin initialization
  if (!BLE.begin()) {
    //Serial.println("starting Bluetooth® Low Energy module failed!");

    while (1);
  }

  BLE.setLocalName("NANO 33 BLE");
  BLE.setAdvertisedService(ledService);
  
  ledService.addCharacteristic(ledCharacteristic);
  ledService.addCharacteristic(buttonCharacteristic);

  BLE.addService(ledService);

  ledCharacteristic.writeValue(0);
  buttonCharacteristic.writeValue(0);

  BLE.advertise();
  //Serial.println("Bluetooth® device active, waiting for connections...");

}

void loop() {

  BLE.poll();

  char buttonValue = digitalRead(buttonPin);
  bool buttonChanged = (buttonCharacteristic.value() != buttonValue);

  if (buttonChanged) {
    ledCharacteristic.writeValue(buttonValue);
    buttonCharacteristic.writeValue(buttonValue);
  }

  if (ledCharacteristic.written() || buttonChanged)
  {
    if (ledCharacteristic.value()) 
    {
      if (ledCharacteristic.value() == 2) 
      {
         blinkLED(ledPin);
      } else if (ledCharacteristic.value() == 3) 
      {
         digitalWrite(LEDR, LOW);
         digitalWrite(LEDG, HIGH);
         digitalWrite(LEDB, HIGH);
         delay(500);
         blinkLED(LEDR);
         offLED();
      } else if (ledCharacteristic.value() == 4)
      {
        digitalWrite(LEDG, LOW);
        digitalWrite(LEDR, HIGH);
        digitalWrite(LEDB, HIGH);
        delay(500);
        blinkLED(LEDG);
        offLED();
      } else if (ledCharacteristic.value() == 5)
      {
        digitalWrite(LEDB, LOW);
        digitalWrite(LEDR, HIGH);
        digitalWrite(LEDG, HIGH);
        delay(500);
        blinkLED(LEDB);
        offLED();
      }
      
      //Serial.println("LED on");
      digitalWrite(ledPin, HIGH);
    } else {
      //Serial.println("LED off");
      digitalWrite(ledPin, LOW);
    }
  }
  
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
  Serial.print(',');ryr0
  Serial.print(gyrY);
  Serial.print(',');
  Serial.print(gyrZ);
  Serial.println();
  
}
