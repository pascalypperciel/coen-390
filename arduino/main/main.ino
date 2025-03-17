#include <Wire.h>
#include <Adafruit_MLX90614.h>
#include "HX711.h"
#include <HCSR04.h>
#include <BluetoothSerial.h>
#include <cmath>

// Temperature Sensor
#define TEMP_I2C_SDA      21
#define TEMP_I2C_SCL      22

// Distance Sensor
#define DIST_TRIG_PIN     13
#define DIST_ECHO_PIN     12

// Weight Sensor
#define LOADCELL_DOUT_PIN 4
#define LOADCELL_SCK_PIN  5

// Motor Control
#define FWD 2
#define BWD 15

#define BAUD    115200

Adafruit_MLX90614 mlx = Adafruit_MLX90614();
HX711 scale;
BluetoothSerial Bluetooth;

void TaskBluetooth(void *pvParameters) {
  while (1) {
    // Distance Sensor
    double* distances = HCSR04.measureDistanceCm(); // in cm
    float distance = (distances != nullptr) ? distances[0] : NAN;

    // Temperature Sensor
    float temperature = mlx.readObjectTempC(); // in C

    // Weight Sensor
    float weight = scale.get_units(5); // averages 5 readings, tweak with it. In g.

    // Format message in standardized format
    char message[128];
    snprintf(message, sizeof(message), "%.2f;%.2f;%.2f", distance, temperature, weight);
    Bluetooth.println(message);

    vTaskDelay(200 / portTICK_PERIOD_MS); // non-blocking delay
  }
}

void TaskIOControl(void *pvParameters) {
  while (1) {
    if (Bluetooth.available()) {
      String command = Bluetooth.readStringUntil('\n');
      command.trim();
      if (command == "Motor_FWD") {
        digitalWrite(FWD, HIGH);
        digitalWrite(BWD, LOW);
      }else if(command == "Motor_BWD"){
        digitalWrite(BWD, HIGH);
        digitalWrite(FWD, LOW);
      }
      else if (command == "Motor_OFF") {
        digitalWrite(FWD, LOW);
        digitalWrite(BWD, LOW);
      }
    }
    vTaskDelay(100 / portTICK_PERIOD_MS); // non-blocking delay
  }
}

void setup() {
  // Bluetooth
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

  //Motor Controller
  pinMode(FWD, OUTPUT);
  digitalWrite(FWD, LOW);
  pinMode(BWD, OUTPUT);
  digitalWrite(BWD, LOW);

  // Dual-Threading
  xTaskCreatePinnedToCore(TaskBluetooth, "Bluetooth Task", 10000, NULL, 1, NULL, 1);
  xTaskCreatePinnedToCore(TaskIOControl, "I/O Control Task", 10000, NULL, 1, NULL, 0);
}

void loop() {
  //unneeded
}
