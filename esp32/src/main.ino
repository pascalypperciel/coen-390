#include <Wire.h>
#include <Adafruit_MLX90614.h>
#include "HX711.h"
//#include <HCSR04.h>
#include "Rotary.h"
#include <BluetoothSerial.h>
#include <cmath>

// Temperature Sensor
#define TEMP_I2C_SDA      21
#define TEMP_I2C_SCL      22

// Distance Sensor
// #define DIST_TRIG_PIN     13
// #define DIST_ECHO_PIN     12
#define ROTARY_PIN1	26
#define ROTARY_PIN2	27
#define ROTARY_CM_PER_STEP 0.02692793703

// Weight Sensor
#define LOADCELL_DOUT_PIN 4
#define LOADCELL_SCK_PIN  5

// Motor Control
#define FWD 2
#define BWD 15

#define BAUD    115200

bool weight_cnt = true;

Adafruit_MLX90614 mlx = Adafruit_MLX90614();
HX711 scale;
BluetoothSerial Bluetooth;
Rotary r = Rotary(ROTARY_PIN1, ROTARY_PIN2);

volatile int rotaryPosition = 0;

void updateRotaryPosition(Rotary& r) {
  rotaryPosition = r.getPosition();
}

void TaskBluetooth(void *pvParameters) {
  while (1) {
    // Weight Sensor
    float weight = scale.get_units(5); // averages 5 readings, tweak with it. In g.
    weight = (weight < 5) ? NAN : weight;

    // Distance Sensor
    // double* distances = HCSR04.measureDistanceCm(); // in cm
    // float distance = (distances != nullptr) ? distances[0] : NAN;
    // distance=10.55-distance;
    r.loop();
    float distance = 0;
    if (!isnan(weight)) {
      distance = rotaryPosition * 0.02692793703; // cm
    } else {
      rotaryPosition = 0; // Reset position if no object
      r.setPosition(0);
    }

    // Temperature Sensor
    float temperature = mlx.readObjectTempC(); // in C

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
        if(weight_cnt){
         scale.tare();
         weight_cnt=false; 
        }
        digitalWrite(FWD, HIGH);
        digitalWrite(BWD, LOW);
      }else if(command == "Motor_BWD"){
          if(weight_cnt){
         scale.tare();
         weight_cnt=false; 
        }
        digitalWrite(BWD, HIGH);
        digitalWrite(FWD, LOW);
      }
      else if (command == "Motor_OFF") {
        weight_cnt=true; 
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
  scale.set_scale(102.493248); // scale calibration, tweak with it
  scale.set_offset(51322);
  scale.tare();

  // Distance Sensor
  // HCSR04.begin(DIST_TRIG_PIN, DIST_ECHO_PIN);

  // Rotary Encoder Setup
  r.setPosition(0); // Reset rotary position on startup
  r.setChangedHandler(updateRotaryPosition);

  // Motor Controller
  pinMode(FWD, OUTPUT);
  digitalWrite(FWD, LOW);
  pinMode(BWD, OUTPUT);
  digitalWrite(BWD, LOW);

  // Dual-Threading
  xTaskCreatePinnedToCore(TaskBluetooth, "Bluetooth Task", 10000, NULL, 1, NULL, 1);
  xTaskCreatePinnedToCore(TaskIOControl, "I/O Control Task", 10000, NULL, 1, NULL, 0);
}

void loop() {
}
