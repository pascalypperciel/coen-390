# Setting Up the Arduino Environment

Follow these steps to get started with the Arduino code for this project.

## 1. Install Arduino IDE
Download and install the **Arduino IDE** by following the official instructions here:  
[**Download Arduino IDE**](https://www.arduino.cc/en/software)

 **Recommended version**: **2.3.4** (This is the version I am using.)

## 2. Open Arduino IDE
Once installed, launch the **Arduino IDE**.

## 3. Open the Project Files
To open the Arduino project files:

1. In the Arduino IDE, navigate to **File** > **Open**.
2. Browse to the directory where you cloned the GitHub repository.
3. Select the desired `.ino` file and open it.

## 4. Board and Serial Monitor Settings
Once the microcontroller is plugged in, you can follow these instructions:

1. Select Board > "ESP32 Dev Module" (It may ask you to download it first, accept) > Select your port.
2. Download and install the [CP2102 drivers](https://www.silabs.com/developer-tools/usb-to-uart-bridge-vcp-drivers?tab=downloads).
3. Click on Tools > Select Serial Monitor > Set it to **115200 baud**. (Or whatever number is in the *Serial.begin(?)* line.)
4. Click on Tools > Make sure that your settings are the same as the following:
![ESP32 Arduino IDE](/docs/images/esp32_arduino_ide.png)

## 5. Libraries Installation
On Arduino IDE, you can click on Tools > Manage Libraries, and then install the following libraries:

1. **HC-SR04** by Dirk Sarodnick
2. **Adafruit MLX90614 Library** by Adafruit
3. **HX711** by Rob Tillart

Download any requisites that these libraries required if the IDE prompts you.