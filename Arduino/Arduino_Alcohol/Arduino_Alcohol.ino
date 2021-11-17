#include <DFPlayer_Mini_Mp3.h>  // MP3 제어 라이브러리 
#include <Adafruit_NeoPixel.h>  // LED 제어 라이브러리
#include <SoftwareSerial.h>     // RX TX 2개 사용

#define NEOPIXEL_PIN 7  // 디지털 입력 핀
#define NUMPIXELS 24    // LED 소자 개수
#define BRIGHTNESS 180  // 밝기 0 ~ 255

// NeoPixel 초기화 
Adafruit_NeoPixel strip = Adafruit_NeoPixel(NUMPIXELS, NEOPIXEL_PIN, NEO_GRB + NEO_KHZ800);

// SoftwareSerial btSerial(0, 1); // 블루투스 모듈 RX TX
// 하드웨어 시리얼 0, 1 핀 Bluetooth로 사용 
SoftwareSerial musicModule(2, 3); // MP3 모듈 RX TX

int fsrSensor = A0;     // 압력센서 아날로그입력 핀번호
int fsrValue = 0;       // 압력센서 값 저장 용

byte buffer[1024];      // 데이터 저장 버퍼
int bufferIndex;

String btMessage;       // 전송될 메세지

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

  bufferIndex = 0;                  // 버퍼 위치 초기화
  
  mp3_set_serial(musicModule);      // MP3 모듈 시리얼 설정
  delay(1);                         // 볼륨 설정 위한 딜레이 1
  mp3_set_volume(15);               // 볼륨 0 ~ 30
  
  strip.setBrightness(BRIGHTNESS);  // LED 밝기 설정 
  strip.begin();                    // LED 제어 시작
  strip.show();                     // LED 동작 초기
}

void loop() {
  // put your main code here, to run repeatedly:
  initVar();
  btControl();
  
  fsrValue = analogRead(fsrSensor);
  btProtocol();
  sendByte(btMessage);  
}

// 변수 초기화
void initVar(){
  fsrValue = 0;
  btMessage = "";
}

// LED 패턴 관리
void colorSetting(){
  
}

// Bluetooth 데이터 들어오면 읽어들임 
void btControl(){
  if (Serial.available()){
    byte data = Serial.read();
    // Serial.write(data);
  
    while (true){
      buffer[bufferIndex++] = data;
    
      if (data == '\n'){
        buffer[bufferIndex] = '\0';
    
        // Serial.write(buffer, bufferIndex);
        bufferIndex = 0;
      }
    }
  }
}

// Bluetooth로 전송하는 메세지 프로토콜 
void btProtocol(){
  btMessage = "ARD_FSR_" + String(fsrValue);
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
