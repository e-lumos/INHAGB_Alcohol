#include <DFPlayer_Mini_Mp3.h>  // MP3 제어 라이브러리 
#include <Adafruit_NeoPixel.h>  // LED 제어 라이브러리
#include <SoftwareSerial.h>     // RX TX 2개 사용

#define NEOPIXEL_PIN 4  // 디지털 입력 핀
#define NUMPIXELS 24    // LED 소자 개수
#define BRIGHTNESS 255  // 밝기 0 ~ 255

// NeoPixel 초기화 
Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUMPIXELS, NEOPIXEL_PIN, NEO_GRB + NEO_KHZ800);

// SoftwareSerial btSerial(0, 1); // 블루투스 모듈 RX TX
// 하드웨어 시리얼 0, 1 핀 Bluetooth로 사용 
SoftwareSerial musicModule(7, 8); // MP3 모듈 RX TX

int fsrSensor = A0;     // 압력센서 아날로그입력 핀번호
int fsrValue = 0;       // 압력센서 값 저장용

byte buffer[1024];      // 데이터 저장 버퍼
int bufferIndex;

int peopleNum = 4;      // 사람 수

String btMessage;       // 전송될 메세지

uint32_t colorValue = (255, 0, 0);  // 색상 지정 
uint32_t colorOFF = (0, 0, 0);

bool ifCup = false;

void initVar();         // 변수 초기화 함수
void colorSetting();    // LED 동작 제어
void btControl();       // 블루투스 통신 제어
void btProtocol();      // 안드로이드에 전송할 데이터 전처리
void sendByte(String);  // String 형식의 데이터 inputStream 형식인 Byte로 전송

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  // btSerial.begin(9600);
  musicModule.begin(9600);

  strip.setBrightness(BRIGHTNESS);  // LED 밝기 설정 
  strip.begin();                    // LED 제어 시작
  strip.show();                     // LED 동작 초기

  randomSeed(analogRead(1));

  bufferIndex = 0;                  // 버퍼 위치 초기화
  
  mp3_set_serial(musicModule);      // MP3 모듈 시리얼 설정
  mp3_set_volume(15);               // 볼륨 0 ~ 30
  delay(10);                        // 볼륨 설정 위한 딜레이 1
}

void loop() {
  // put your main code here, to run repeatedly:
  initVar();
  btControl();
  
  fsrValue = analogRead(fsrSensor);
  if (fsrValue >= 100 && ifCup == false){
    for(int i = 0; i < 24; i++){
      strip.setPixelColor(i, 255, 0, 0);
      strip.show();
      delay(50);
    }
    for(int i = 0; i < 24; i++){
      strip.setPixelColor(i, 0, 255, 0);
      strip.show();
      delay(50);
    }
    for(int i = 0; i < 24; i++){
      strip.setPixelColor(i, 0, 0, 255);
      strip.show();
      delay(50);
    }
    delay(1000);

    for(int i = 0 ; i < 24; i++){
      strip.setPixelColor(i, 0, 0, 0);
      strip.show();
      delay(50);
    }
    
    ifCup = true;
  }
  // btProtocol();
  // sendByte(btMessage);  
  delay(10);
}

// 변수 초기화
void initVar(){
  fsrValue = 0;
  btMessage = "";
}

// LED 패턴 관리
void colorSetting(){
  int numC = 24 / peopleNum;
  int ledStart = 0;
  int ledEnd = numC - 1;
  int turn = 3;
  for (int i = 0; i < numC; i++){
    strip.setPixelColor(i, 255, 0, 0);
  }
  strip.show();
  delay(500);
  // mp3_play(1);
  delay(10);
  while(turn > 0){
    for (int i = 0; i < 24 + numC; i++){
      strip.setPixelColor(ledStart++, 0, 0, 0);
      strip.setPixelColor(ledEnd++, 255, 0, 0);
      strip.show();
      if (turn > 1) delay(100);
      else delay(150);
      if (ledEnd >= 24) ledEnd %= 24;
      if (ledStart >= 24) ledStart %= 24; 
    }
    turn--;
  }

  int res = random(0, 24);
  int dtime = 100;
  while(true){
    if (ledStart == res) break;
    strip.setPixelColor(ledStart++, 0, 0, 0);
    strip.setPixelColor(ledEnd++, 255, 0, 0);
    strip.show();
    delay(150);
    if (ledEnd >= 24) ledEnd %= 24;
    if (ledStart >= 24) ledStart %= 24; 
  }
  delay(1000);
  
  int ledNow = ledStart;
  while(true){
    if (ledNow >= ledEnd) break;
    strip.setPixelColor(ledNow++, 0, 255, 0);
    if (ledNow >= 24) ledNow %= 24;
    strip.show();
    delay(50);
  }
  delay(1000);

  ledNow = ledStart;
  while(true){
    if (ledNow >= ledEnd) break;
    strip.setPixelColor(ledNow++, 0, 0, 255);
    strip.show();
    delay(50);
    if (ledNow >= 24) ledNow %= 24;
  }
  delay(1000);

  ledNow = ledStart;
  while(true){
    if (ledNow >= ledEnd) break;
    strip.setPixelColor(ledNow++, random(0, 255), random(0, 255), random(0, 255));
    strip.show();
    delay(50);
    if (ledNow >= 24) ledNow %= 24;
  }
  delay(1000);

  ledNow = ledStart;
  while(true){
    if (ledNow >= ledEnd) break;
    strip.setPixelColor(ledNow++, 0, 0, 0);
    if (ledNow >= 24) ledNow %= 24;
  }
  strip.show();
  delay(100);

  ledNow = ledStart;
  while(true){
    if (ledNow >= ledEnd) break;
    strip.setPixelColor(ledNow++, random(0, 255), random(0, 255), random(0, 255));
    strip.show();
    delay(50);
    if (ledNow >= 24) ledNow %= 24;
  }
  delay(1000);

  
  for (int i = 0; i < 24; i++){
    strip.setPixelColor(i, 0, 0, 0);
  }
  strip.show();
  delay(100);
  // mp3_play(2);
  delay(10);
}

// Bluetooth 데이터 들어오면 읽어들임 
void btControl(){
  if (Serial.available()){
    String str = Serial.readString();
    Serial.println("111");                                             
    if (str.startsWith("AND_MAIN_")){
      // 돌림판 돌리기
      // mp3_play(1);
      // delay(10);
      colorSetting();
      delay(10);
    }
    else if (str.startsWith("AND_PNUM_")){
      peopleNum = str.substring(9).toInt();
    }
  }
}

// Bluetooth로 전송하는 메세지 프로토콜 
void btProtocol(){
  btMessage = "ARD_FSR_" + String(fsrValue);
}

void ifCupOn(){
  // 컵 무게값 확인기
  if (fsrValue > 30){
    // LED 빛내서 표시하
  }
}

// String 형식의 데이터 Byte로 전송 
void sendByte(String inputString){
  byte *tmp = new byte[inputString.length() + 1];
  inputString.getBytes(tmp, inputString.length() + 1);

  for(int i = 0; i < inputString.length(); i++){
    Serial.print(*(tmp + i));
  }

  Serial.print("\n");
  free(tmp);
}
