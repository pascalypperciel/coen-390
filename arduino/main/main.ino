#include <Wire.h>
#include <Adafruit_MLX90614.h>
#include "HX711.h"
#include <HCSR04.h>
#include <BluetoothSerial.h>

// Temperature Sensor
#define TEMP_I2C_SDA      21
#define TEMP_I2C_SCL      22

// Distance Sensor
#define DIST_TRIG_PIN     13
#define DIST_ECHO_PIN     12

// Weight Sensor
#define LOADCELL_DOUT_PIN 4
#define LOADCELL_SCK_PIN  5

#define BAUD    115200

Adafruit_MLX90614 mlx = Adafruit_MLX90614();
HX711 scale;
BluetoothSerial Bluetooth;

void setup() {
  Serial.begin(BAUD);
  Bluetooth.begin("CAT_Tester");

  // Temperature Sensor
  Wire.begin(TEMP_I2C_SDA, TEMP_I2C_SCL);
  mlx.begin();

  // Weight Sensor
  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
  scale.set_scale(8); // scale calibration, tweak with it
  scale.tare();

  // Distance Sensor
  HCSR04.begin(DIST_TRIG_PIN, DIST_ECHO_PIN);

  Serial.println("Sensors are ready to go!");
}

void loop() {
  // Distance Sensor
  double* distances = HCSR04.measureDistanceCm();
  Serial.printf("Distance: %.2f cm\n", distances[0]);
  Bluetooth.printf("Distance: %.2f cm\n", distances[0]);


  // Temperature Sensor
  float temperature = mlx.readObjectTempC();
  Serial.printf("Temperature: %.2f °C\n", temperature);
  Bluetooth.printf("Temperature: %.2f °C\n", temperature);


  // Weight Sensor
  float weight = scale.get_units(5);  // averages X readings, tweak with it
  Serial.printf("Weight: %.2f g\n", weight);
  Bluetooth.printf("Weight: %.2f g\n", weight);

  Serial.println("-------------------------");

  delay(1000);
}
