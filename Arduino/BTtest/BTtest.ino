#include <SoftwareSerial.h>

// SoftwareSerial BTSerial(7, 8);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  // BTSerial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  Serial.write("DADA\n");
  delay(1000);
}
