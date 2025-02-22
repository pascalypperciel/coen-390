#include <Wire.h>
#include <Adafruit_MLX90614.h>
#include "HX711.h"
#include <HCSR04.h>

// Temperature Sensor
#define TEMP_I2C_SDA        21
#define TEMP_I2C_SCL        22

// Distance Sensor
#define DIST_TRIG_PIN       13
#define DIST_ECHO_PIN       12

// Weight Sensor
#define LOADCELL_DOUT_PIN   4
#define LOADCELL_SCK_PIN    5

#define BAUD                115200

Adafruit_MLX90614 mlx = Adafruit_MLX90614();
HX711 scale;

void setup() {
  Serial.begin(BAUD);

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
  Serial.print("Distance: ");
  Serial.print(distances[0]);
  Serial.println(" cm");

  // Temperature Sensor
  float objectTemp = mlx.readObjectTempC();
  Serial.print("Temperature: ");
  Serial.print(objectTemp);
  Serial.println(" °C");

  // Weight Sensor
  float weight = scale.get_units(20); // averages 20 readings, tweak with it
  Serial.print("Weight: ");
  Serial.print(weight);
  Serial.println(" g");

  Serial.println("-------------------------");

  delay(1000);
}
