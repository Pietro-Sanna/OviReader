#include <SoftwareSerial.h>

const int BUFFER_SIZE = 13;       // RFID DATA FRAME FORMAT: 1byte head (AA=170), 10byte data (3byte fixed + 2byte country + 5byte tag), 1byte checksum, 1byte tail (BB=187)
const int DATA_SIZE = 10;         // 3 byte fixed (0F 08 00) + 7byte data (2byte country + 5byte tag) (03 84 + 12 DB FA E7 D5)
const int DATA_FIXED_SIZE = 3;    // 3byte fixed (0F 08 00)
const int DATA_COUNTRY_SIZE = 2;  // 2byte country (example 03 84)
const int DATA_TAG_SIZE = 5;      // 5byte tag (example 12 DB FA E7 D5)
const int CHECKSUM_SIZE = 1;      // 1byte checksum (example 81)

SoftwareSerial ssrfid = SoftwareSerial(6, 5); // Rx, Tx

uint8_t buffer[BUFFER_SIZE]; // used to store an incoming data frame

int buffer_index = 0;
int buttonState=0;
const int buttonPin = 12; //Pin for button

const int LedPin = 13;      // led is connected to pin D13
long previousMillis = 0;    // backlight timer
long interval = 120000;     // interval to turn off the backlight

void setup() {
  Serial.begin(9600);

  ssrfid.begin(9600);
  ssrfid.listen();

  pinMode(LedPin, OUTPUT);
  pinMode(buttonPin,INPUT);
  
  Serial.println("INIT DONE");
  
}

void loop() {
  buttonState = digitalRead(buttonPin);
  if(buttonState == HIGH){
    
    if (ssrfid.available() > 0) {     // als data aanwezig
      bool call_extract_tag = false;
  
      int ssvalue = ssrfid.read(); // lees data
  
      if (ssvalue == -1) {         // no data was read
        return;
      }
  
      if (ssvalue == 170 && buffer_index == 0) {           // EM4305RFID found a tag => tag incoming
        //      buffer_index = 0;
        digitalWrite(LedPin, HIGH);   // led aan
      } else if (ssvalue == 187 && buffer_index == BUFFER_SIZE - 1) {    // tag has been fully transmitted
        call_extract_tag = true;      // extract tag at the end of the function call
      }
  
      if (buffer_index >= BUFFER_SIZE) {  // checking for a buffer overflow (It's very unlikely that an buffer overflow comes up!)
        Serial.println("Error: Buffer overflow detected!");
       
        previousMillis = millis();   // Start backlight timer
        buffer_index = 0;
        return;
      }
  
      buffer[buffer_index++] = ssvalue; // everything is alright => copy current value to buffer
  
      if (call_extract_tag == true) {
        if (buffer_index == BUFFER_SIZE) {
          String cod = extract_tag();
          Serial.print(cod);
          digitalWrite(LedPin, LOW);
          buffer_index = 0;
        } else { // something is wrong... start again looking for preamble (value: 170)
          buffer_index = 0;
          return;
        }
      }
    }
  } 


}

String extract_tag() {          // analiseer data
  
  uint8_t msg_checksum = buffer[11]; // 1 byte
  uint8_t msg_data[DATA_SIZE];
  uint8_t msg_data_country[DATA_COUNTRY_SIZE];
  uint8_t msg_data_tag[DATA_TAG_SIZE];
  uint8_t countrymessage[2 * DATA_COUNTRY_SIZE]; // used to store ASCII code of hex data of country part
  uint8_t tagmessage[2 * DATA_TAG_SIZE]; // used tot store ASCII code of hex data of tag part
  long checksum = 0;
  long country = 0;
  long long tag = 0;
  long s;                 // second half of tag to show and determine leading 0
  int expo;               //exponent counter

  // put buffer country in reverse into country message
  for (int i = 0; i < DATA_COUNTRY_SIZE ; i++) {
    String str = String(buffer[i + 4], HEX);
    if (str.length() == 1 && buffer[i + 6] - 48 < 10) {  // add leading 0 if < 10
      str = "0" + str;
    }
    str.toUpperCase();
    for (int k = 0; k < str.length(); k++) {
      byte x = str.charAt(k);
      countrymessage[2 * DATA_COUNTRY_SIZE - 1 - i * 2 - k] = x;
    }
  }

  country = hexInDec(countrymessage, 0, 2 * DATA_COUNTRY_SIZE);

  // Buffer TAG in reverse to tagmessage
  for (int i = 0; i < DATA_TAG_SIZE ; i++) {
    String str = String(buffer[i + 6], HEX);
    if (str.length() == 1 && buffer[i + 6] - 48 < 10) {  // add leading 0 if < 10
      str = "0" + str;
    }
    str.toUpperCase();
    for (int k = 0; k < str.length(); k++) {
      byte x = str.charAt(k);
      tagmessage[2 * DATA_TAG_SIZE - 1 - i * 2 - k] = x;
    }
  }

  tag = hexInDec(tagmessage, 0, 2 * DATA_TAG_SIZE);
  String ret;
  ret = (String) long(tag / 1000000);
  s = long(tag % 1000000);
  for (long i = 100000; s < i && i > 1; i = i / 10) { // print leading 0
    ret = ret + "0";
  }
  ret = country+ ret +s;
  return ret;
  

}

long long hexInDec(char message[], int beg , int len) {
  long long mult = 1;
  long long nbr = 0;
  byte nextInt;
  for (int i = beg; i < beg + len; i++) {
    nextInt = message[i];
    if (nextInt >= 48 && nextInt <= 57) nextInt = map(nextInt, 48, 57, 0, 9);
    if (nextInt >= 65 && nextInt <= 70) nextInt = map(nextInt, 65, 70, 10, 15);
    if (nextInt >= 97 && nextInt <= 102) nextInt = map(nextInt, 97, 102, 10, 15);
    nextInt = constrain(nextInt, 0, 15);
    nbr = nbr + (mult * nextInt);
    mult = mult * 16;
  }
  return nbr;
}
