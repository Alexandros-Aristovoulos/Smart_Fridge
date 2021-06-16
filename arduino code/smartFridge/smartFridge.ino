/*
   You can find the DHT Library from Arduino official website
   https://playground.arduino.cc/Main/DHTLib
*/
#include <LiquidCrystal.h> // includes the LiquidCrystal Library
#include <dht.h>

#define dataPin 8

boolean prevStatus;  //true for off

float targetTemp;
float offset;

LiquidCrystal lcd(2, 3, 4, 5, 6, 7); // Creates an LCD object. Parameters: (rs, enable, d4, d5, d6, d7)
dht DHT;

int power = 12;     //pin that controls the fridge

void setup() {
  // Initializes the interface to the LCD screen, and specifies the dimensions (width and height) of the display
  lcd.begin(16, 2);

  //pin that controls the fridge
  pinMode(power, OUTPUT);

  prevStatus = false; //true for off

  // opens serial port, sets data rate to 9600 bps
  Serial.begin(9600);

  //temp
  targetTemp = 3.0;
  offset = 0.5;
}

void loop() {
  lcd.clear(); //clear the display
  int readData = DHT.read22(dataPin);
  float t = DHT.temperature;
  float h = DHT.humidity;

  Serial.print("Current target: ");
  Serial.println(targetTemp);

  //get input (if we have any)
  if (Serial.available() > 0) {
    // read the incoming string:
    String incomingString = Serial.readString();

    // prints the received data
    Serial.print("I received: ");
    Serial.println(incomingString);

    //convert to float
    targetTemp =  incomingString.toFloat();
    Serial.print("I converted that to: ");
    Serial.println(targetTemp);
  }

  lcd.setCursor(0, 0); // Sets the location at which subsequent text written to the LCD will be displayed
  lcd.print("Current: ");
  lcd.print(t); // Prints the temperature value from the sensor
  Serial.println("The temperature is: ");
  lcd.print("C");
  lcd.setCursor(0, 1);
  lcd.print("Target: ");
  lcd.print(targetTemp); // Prints the target temperature
  lcd.print("C ");
  Serial.print("Fridge is: ");

  //turn off
  if (t < targetTemp - offset) {
    digitalWrite(power, HIGH);
    prevStatus = true;
    lcd.print("(OFF)");
    Serial.println("OFF");
  } //turn on
  else if (t > targetTemp + offset) {
    digitalWrite(power, LOW);
    prevStatus = false;
    lcd.print("(ON)");
    Serial.println("ON");
  } else {
    //continue what you were doing
    if (prevStatus) {
      digitalWrite(power, HIGH);
      prevStatus = true;
      lcd.print("(OFF)");
      Serial.println("OFF");
    } else {
      digitalWrite(power, LOW);
      prevStatus = false;
      lcd.print("(ON)");
      Serial.println("ON");
    }
  }

  delay(2000);
}
