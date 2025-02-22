## HC-SR04 (Ultrasonic Sensor)
| HC-SR04  | ESP32-WROOM-32 |
| ------------- | ------------- |
| VCC  | VIN  |
| GND  | GND  |
| TRIG  | D13  |
| ECHO  | *Read Below*  |

#### ECHO Pin Voltage Divider:
You must do the following connections to convert the ECHO pin to 3.3V.

**ECHO** > **1kΩ Resistor** > **D12**

**D12** > **2kΩ Resistor** > **GND**

## MLX90614 (Temperature Sensor)
| MLX90614  | ESP32-WROOM-32 |
| ------------- | ------------- |
| VCC  | VIN  |
| GND  | GND  |
| SDA  | D21  |
| SCL  | D22  |

## HX711 (Load Cell Amplifier)
| MLX90614  | ESP32-WROOM-32 |
| ------------- | ------------- |
| VCC  | 5V  |
| GND  | GND  |
| DT  | D4  |
| SCK  | D5  |