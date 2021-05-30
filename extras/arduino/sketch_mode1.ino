/*
 Arduino (ProMini 5V) Sketch, connected to 4 JSN-SR04T 2.0, Mode1.
 Reading distance measurement one by one and sending the measured 4 bytes with
 SPI (as slave) to an Raspberry Pi (as master).
 @author Istvan Szilagyi
*/

#include <SPI.h>

// Define Trig (TX) and Echo (RX)pins for the 4 JSN-SRN04T sensors.
#define S1_TX_Pin 2
#define S1_RX_Pin 3
#define S2_TX_Pin 4
#define S2_RX_Pin 5
#define S3_TX_Pin 7
#define S3_RX_Pin 6
#define S4_TX_Pin 8
#define S4_RX_Pin 9

#define HELLO_SPI_BYTE 0xAB; // first byte send to SPI after reset
byte recBuf[4] = {0, 0, 0, 0}; // received byte values are not used 
byte lastMeasure[] = {0xFB, 0xFC, 0xFD, 0xFE};

const boolean serialDebug = false;

byte trigPins[] = {S1_TX_Pin, S2_TX_Pin, S3_TX_Pin, S4_TX_Pin};
byte echoPins[] = {S1_RX_Pin, S2_RX_Pin, S3_RX_Pin, S4_RX_Pin};


void setup() {
  // put your setup code here, to run once:
  
  // Define inputs and outputs
   for (byte i = 0; i<=3; i++) {
     pinMode(trigPins[i], OUTPUT);
     pinMode(echoPins[i], INPUT);
  }
  
  pinMode(LED_BUILTIN, OUTPUT);
  if (serialDebug) {
    Serial.begin(9600);
  }

  // SPI INITIALISATION
  pinMode(MISO, OUTPUT);

  /* Enable SPI */
  SPCR = (1<<SPE);
  SPDR = HELLO_SPI_BYTE; // any byte
  //noInterrupts();
}

void loop() {
  for (byte i = 0; i<=3; i++) {
    int sens = measure(i);
    lastMeasure[i] = sens > 255 ? 250 : measure(i);
  }
  if (serialDebug) {
    printSerial();
  }
  spi4Bytes();
}

/* returns measured distance in cm. Max 600cm, Min 20cm */
int measure(byte sensorIndex) {
  byte trigPin = trigPins[sensorIndex];
  byte echoPin = echoPins[sensorIndex];

  unsigned long duration;
  int distance = 0;

   // Clear the trigPin by setting it LOW:
  digitalWrite(trigPin, LOW);
  delayMicroseconds(10);
  
 // Trigger the sensor by setting the trigPin high for 10 microseconds:
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(15);
  digitalWrite(trigPin, LOW);
  delayMicroseconds(150); //!!
  
  // Read the echoPin. pulseIn() returns the duration (length of the pulse) in microseconds( 50ms timeout):
  duration = pulseIn(echoPin, HIGH, 50000);
  distance = duration / 58; // from JSN 2.0 Datasheet.
  return distance;
}

void printSerial() {
  // Print the distance on the Serial Monitor (Ctrl+Shift+M):
  for (byte i = 0; i<=3; i++) {
    Serial.print(i);
    Serial.print(" : ");
    Serial.print(lastMeasure[i]);
    Serial.print(" ; ");
  }
  Serial.println("");
}

void spi4Bytes() { 
   byte valFromSPIMaster = 0;
   byte bCounter = 0;
   /* Wait till a four byte EXCHANGE is done */
   while (bCounter <= 3)  {
     while(!(SPSR & (1<<SPIF)))
     ;
     recBuf[bCounter] = SPDR;
     SPDR = lastMeasure[bCounter++];
   }
 }
 
