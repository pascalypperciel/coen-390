#include <Wire.h>
#include <Adafruit_MLX90614.h>
#include "HX711.h"
#include <BluetoothSerial.h>
#include <cmath>

// Temperature Sensor
#define TEMP_I2C_SDA      21
#define TEMP_I2C_SCL      22

// Distance Sensor
// #define DIST_TRIG_PIN     13
// #define DIST_ECHO_PIN     12
#define CLK_PIN 25 // ESP32 pin GPIO25 connected to the rotary encoder's CLK pin
#define DT_PIN  26 // ESP32 pin GPIO26 connected to the rotary encoder's DT pin
volatile int counter = 0;
volatile int CLK_state;
volatile int new_CLK_state;
volatile int prev_CLK_state;
volatile float distance = 0;
hw_timer_t *My_timer = NULL;

// Weight Sensor
#define LOADCELL_DOUT_PIN 4
#define LOADCELL_SCK_PIN  5
volatile float weight=0;

// Motor Control
#define FWD 2
#define BWD 15

#define BAUD    115200

bool weight_cnt = true;

Adafruit_MLX90614 mlx = Adafruit_MLX90614();
HX711 scale;
BluetoothSerial Bluetooth;

//timer interupt for rotary encoder
void IRAM_ATTR onTimer(){
     CLK_state = digitalRead(CLK_PIN);
    
     if (CLK_state != prev_CLK_state && CLK_state == HIGH) {
     // if the DT state is HIGH
     // the encoder is rotating in counter-clockwise direction => decrease the counter
       if (digitalRead(DT_PIN) == HIGH) {
         counter--;
       } else {
         // the encoder is rotating in clockwise direction => increase the counter
         counter++;
       }
     }
     distance=counter*0.0942;
     prev_CLK_state = CLK_state;
}


void TaskBluetooth(void *pvParameters) {
  while (1) {
    // Weight Sensor
    weight = scale.get_units(5); // averages 5 readings, tweak with it. In g.
    weight = (weight < 5) ? NAN : weight;

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
    if(weight>=10000){//if to much pressure then stop
       digitalWrite(FWD, LOW);
       digitalWrite(BWD, LOW);
    }
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
        }else if (command == "Motor_OFF") {
           weight_cnt=true; 
           digitalWrite(FWD, LOW);
           digitalWrite(BWD, LOW);
        }else if(command == "Starting"){
           //calibrate rotary encoder
           counter=1;
           if(weight_cnt){
               scale.tare();
               weight_cnt=false; 
            }
        digitalWrite(BWD, HIGH);
        digitalWrite(FWD, LOW);
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

  // Rotary Encoder Setup
  pinMode(CLK_PIN, INPUT);
  pinMode(DT_PIN, INPUT);
  prev_CLK_state = digitalRead(CLK_PIN);


  // Set timer frequency to 1Mhz
  My_timer = timerBegin(1000000);

  // Attach onTimer function to our timer.
  timerAttachInterrupt(My_timer, &onTimer);

  // Set alarm to call onTimer function every 1 milliseconds (value in microseconds).
  // Repeat the alarm (third parameter) with unlimited count = 0 (fourth parameter).
  timerAlarm(My_timer, 1000, true, 0);

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
