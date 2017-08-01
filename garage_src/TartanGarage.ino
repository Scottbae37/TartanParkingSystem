/****************************************************************
 * File: Tartan Garage Controller
 * Project: LG Exec Ed SDET Program
 * Copyright: Copyright (c) 2016 Jeff Gennari
 * Versions:
 * 1.0 Nov 2016 - initial version
 *
 * Description:
 *
 * This program runs on an Arduino processor with a WIFI shield.  This
 * program controls the Tartan Garage hardware. It is a simple
 * client/server design where the garage is the server. The protocol
 * is that after a client connects, this process sends two '\n'
 * terminated strings. Then this process waits for the client to send a
 * string back. This illustrates basic connection and two-way
 * communication.
 *
 * Compilation and Execution Instructions: Must be compiled using 
 * Arduino IDE VERSION 1.0.4
 *
 *
 ****************************************************************/

#include <SPI.h>
#include <WiFi.h>
#include <Servo.h>

#define PORTID 5050            // IP socket port#

char ssid[] = "LGArchi";       // The network SSID
int status = WL_IDLE_STATUS;   // Network connection status
WiFiServer server(PORTID);     // Server connection and port
IPAddress ip;                  // The IP address of the shield
IPAddress subnet;              // The IP address of the shield
long rssi;                     // Wifi shield signal strength
byte mac[6];                   // Wifi shield MAC address
char recvBuffer[1024];         // receive buffer

// The state of the entry/exit beams
int EntryBeamState;
int ExitBeamState;

// parameters for the gates
#define EntryGateServoPin 5
#define ExitGateServoPin 6
#define OpenGate  90
#define CloseGate 0

// Constants for the entry/exit lights
#define OFF 0
#define GREEN 1
#define RED 2
#define ON 3

// Constants for the extry/exit gates
#define OPEN 1
#define CLOSE 0

// Constants for the parking stalls
#define EMPTY 0
#define OCCUPIED 1

// constants for the entry/exit IR beams
#define ARRIVED 1
#define NOT_ARRIVED 0

// The state of the entry/exit lights
int EntryLightState;
int ExitLightState;

// Servos that control the entry/exit gates
Servo EntryGateServo;
Servo ExitGateServo;

// The state of the entry/exit gates
int EntryGateState;
int ExitGateState;

// Parameters for the parking stall sensors
long  Stall1SensorVal;
long  Stall2SensorVal;
long  Stall3SensorVal;
long  Stall4SensorVal;

// Parking stall light state
long ParkingStall1LEDState;
long ParkingStall2LEDState;
long ParkingStall3LEDState;
long ParkingStall4LEDState;

// Pin assignments
#define Stall1SensorPin 30
#define Stall2SensorPin 31
#define Stall3SensorPin 32
#define Stall4SensorPin 33
#define EntryGateGreenLED 26
#define EntryGateRedLED   27
#define ExitGateGreenLED  28
#define ExitGateRedLED    29
#define ParkingStall1LED  22
#define ParkingStall2LED  23
#define ParkingStall3LED  24
#define ParkingStall4LED  25
#define ParkingStall1LED  22
#define ParkingStall2LED  23
#define ParkingStall3LED  24
#define ParkingStall4LED  25

// Parameters for the entry/exit IR sensors
#define EntryBeamRcvr  34 
#define ExitBeamRcvr   35

/**
 * Initialization routine
 */
void setup()
{  
   Init();   

   // Initialize a serial terminal for debug messages.
   Serial.begin(9600);
   Serial.print("Attempting to connect to SSID: ");
   Serial.println(ssid);
   // Attempt to connect to Wifi network.
   while ( status != WL_CONNECTED) 
   { 
      status = WiFi.begin(ssid);
   }  
   
   // Print the basic connection and network information.
   printConnectionStatus();
   
   // Start the server and print and message for the user.
   server.begin();
   Serial.println("The Server is started."); 
   
} // setup

/**
 * The main loop
 */
void loop() { 

   // Wait for the client:
   WiFiClient client = server.available();
   
   if (client)   {
      String req = "";                // make a String to hold incoming data from the client
      while (client.connected()) {    // loop while the client's connected
         if (client.available()) {

            // read a character
            char c = client.read();

            // Are we at end of message?
            if (c == '.') {
               req += c;
               
               Serial.println("Request: '" + req + "'");

               // process the full request and send the response
               String response = ProcessRequest(req);
               
               client.println (response);
               client.flush();
           
               response = "";
               req  = "";
           
            } else {
               req += c;
            }
         }
      }
      client.stop();
      Serial.println("Done!");
      Serial.println(".....................");
     
   }
} // loop

/**
 * void Init()
 *
 * Parameters: None           
 * 
 * Description:
 *
 * The entry and exit LEDs are 3 way LEDs with a common annode. This means
 * that you pull the other legs low to lite the appropriate colored LED.
 * The problem is that when you turn on the CPU, the pins are typically low
 * meaning that the LEDs will be on. This method, simply ensures they are 
 * off.
 */
void Init()
{
   int i;
   for (i=26; i<=29; i++)
   {
      pinMode(i, OUTPUT);
      digitalWrite(i, HIGH);
   }

   pinMode(EntryBeamRcvr, INPUT);     // Make entry IR rcvr an input
   digitalWrite(EntryBeamRcvr, HIGH); // enable the built-in pullup

   pinMode(ExitBeamRcvr, INPUT);      // Make exit IR rcvr an input
   digitalWrite(ExitBeamRcvr, HIGH);  // enable the built-in pullup

   // Map servo to pin and initialize the gates
   EntryGateServo.attach(EntryGateServoPin);
   ExitGateServo.attach(ExitGateServoPin);
  
   EntryGateServo.write(CloseGate);
   EntryGateState = CLOSE;
   ExitGateServo.write(CloseGate);
   ExitGateState = CLOSE;

   pinMode(EntryGateGreenLED, OUTPUT);    // This section makes all the LED pins outputs.
   pinMode(EntryGateRedLED, OUTPUT);
   pinMode(ExitGateGreenLED, OUTPUT);
   pinMode(ExitGateRedLED, OUTPUT);
   pinMode(ParkingStall1LED, OUTPUT);
   pinMode(ParkingStall2LED, OUTPUT);
   pinMode(ParkingStall3LED, OUTPUT);
   pinMode(ParkingStall4LED, OUTPUT);

   digitalWrite(EntryGateGreenLED, HIGH);  // The gate LEDs are turned off by setting their pins
   digitalWrite(EntryGateRedLED, HIGH);    // high. The reason for this is that they are
   digitalWrite(ExitGateGreenLED, HIGH);   // 3 color LEDs with a common annode (+). So setting
   digitalWrite(ExitGateRedLED, HIGH);     // any of the other 3 legs low turns on the LED.
  
   digitalWrite(ParkingStall1LED, LOW);    // Standard LEDs are used for the parking stall LEDs. Set the pin high and they light.
   ParkingStall1LEDState = OFF;
  
   digitalWrite(ParkingStall2LED, LOW);
   ParkingStall2LEDState = OFF;
  
   digitalWrite(ParkingStall3LED, LOW);
   ParkingStall3LEDState = OFF;
  
   digitalWrite(ParkingStall4LED, LOW);
   ParkingStall4LEDState = OFF;

   Serial.println("System Initialized");
}


/**
 *
 * String ProcessRequest()
 *
 * Parameters: the incoming request           
 *
 * Description:
 * 
 * This method processes the received message to determine if it is a 
 * set state request or a get state request. The response is returned as a 
 * String
 */
String  ProcessRequest(String request) {

   int hdrPosition = request.indexOf('=');
   int endPosition = request.indexOf('.');
   String response = "";

   String hdr = request.substring(0,2);
   String body = request.substring(hdrPosition+1,endPosition);  
  
   if (hdr == "GS") {
      // a set state request has a body
    
      response =  HandleGetState();
   }
   else if (hdr == "XL") {
      response = ToggleExitLight(body);
   }
   else if (hdr == "NL") {
      response = ToggleEntryLight(body); 
   }
   else if (hdr == "NG") {
      response = ToggleEntryGate(body); 
   }  
   else if (hdr == "XG") {
      response = ToggleExitGate(body); 
   }
   else if (hdr == "PL") {
      response = ToggleParkingLights(body); 
   }
  
   return response;
}


/**
 * void HandleGetState()
 *
 * Parameters: None           
 *
 * Description:
 * 
 * This method gets the current state and retuns it as a String.
 */
String HandleGetState() {

   Serial.println("HandleGetState");
  
   String response = "SU:"; // state update

   // Get entry gate state
   int ng = EntryGateState;
   if (ng == OPEN) {
      response += "NG=1";
   } else {
      response += "NG=0";
   }  
   response += ";";

   // get exit gate state
   int xg = ExitGateState;
   if (xg == OPEN) {
      response += "XG=1";
   } else {
      response += "XG=0";
   }  
   response += ";";

   // get exit gate IR beam
   int xir = GetExitIRState();
   if (xir == ARRIVED) {
      response += "XIR=1";
   } else {
      response += "XIR=0";
   }  
  
   response += ";";

   // get entry gate IR beam
   int nir = GetEntryIRState();
   if (nir == ARRIVED) {
      response += "NIR=1";
   } else {
      response += "NIR=0";
   } 
   
   response += ";";

   int nl = EntryLightState;
   if (nl == GREEN) {
      response += "NL=G";
   }
   else if (nl == RED) {
      response += "NL=R";
   }
   else if (nl == OFF) {
      response += "NL=0";
   }
  
   response += ";";
  
   int xl = ExitLightState;
   if (xl == GREEN) {
      response += "XL=G";
   }
   else if (xl == RED) {
      response += "XL=R";
   }
   else if (xl == OFF) {
      response += "XL=0";
   }
 
   response += ";";

   // get the parking spot states.
   String ps = "PO=[";
   if (GetParkingStall1State() == OCCUPIED) {
      ps += "1=1,";
   }
   else {
      ps += "1=0,";
   }
   if (GetParkingStall2State() == OCCUPIED) {
      ps += "2=1,";
   }
   else {
      ps += "2=0,";
   }
   if (GetParkingStall3State() == OCCUPIED) {
      ps += "3=1,";
   }
   else {
      ps += "3=0,";
   }
   if (GetParkingStall4State() == OCCUPIED) {
      ps += "4=1";
   }
   else {
      ps += "4=0";
   }
   ps += "]";

   response += ps + ";";

   // get the parking light state

   String pl = "PL=[";
   if (ParkingStall1LEDState == ON) {
      pl += "1=1,";
   }
   else {
      pl += "1=0,";
   }
   if (ParkingStall2LEDState == ON) {
      pl += "2=1,";
   }
   else {
      pl += "2=0,";
   }
   if (ParkingStall3LEDState == ON) {
      pl += "3=1,";
   }
   else {
      pl += "3=0,";
   }
   if (ParkingStall4LEDState == ON) {
      pl += "4=1";
   }
   else {
      pl += "4=0";
   }
   pl += "]";
   response += pl + ".";
   
   Serial.println("HandleGetState response: " + response);
  
   return response;
}

/**
 * int GetEntryIRState()
 * 
 * Parameters: None
 *
 * Description: 
 * returns ARRIVED if the entry IR beam is broken, NOT_ARRIVED otherwise
 */

int GetEntryIRState() 
{
   int EntryBeamState = digitalRead(EntryBeamRcvr);

   // if EntryBeamState is LOW the beam is broken
   if (EntryBeamState == LOW)  {
      Serial.println("Entry beam broken");
      return ARRIVED;
   }
   Serial.println("Exit beam is not broken.");
   return NOT_ARRIVED;
}

/**
 * int GetExitIRState()
 * 
 * Parameters: None
 *
 * Description: 
 * 
 * Returns ARRIVED if the exit IR beam is broken, NOT_ARRIVED otherwise
 */
int GetExitIRState() 
{
   int ExitBeamState = digitalRead(ExitBeamRcvr);

   // if EntryBeamState is LOW the beam is broken
   if (ExitBeamState == LOW)  {
      Serial.println("Entry beam broken");
      return ARRIVED;
   }
   Serial.println("Entry beam is not broken.");
   return NOT_ARRIVED;
}

/**
 * int GetParkingStall1State()
 *
 * Parameters: None
 *
 * Description:
 *
 * Return the state of parking stall 1. The routine evaluates whether
 * the current parking stall 1 proximity sensor has changed
 * significantly. If a car is detected, then OCCUPIED is returned,
 * otherwise EMPTY is returned
 */
int PrevStall1Val = -1; // the previous parking stall 1 reading
int GetParkingStall1State() {
   
   long Stall1SensorVal = ProximityVal(Stall1SensorPin); //Check parking space 1
   if (PrevStall1Val == -1) {
      PrevStall1Val = Stall1SensorVal;
   }
   Serial.print("  Stall 1 = ");
   Serial.println(Stall1SensorVal);

   if(abs(PrevStall1Val - Stall1SensorVal) > 5) {
      return OCCUPIED;
   }
    
   return EMPTY;   
}

/**
 * int GetParkingStall2State()
 *
 * Parameters: None
 *
 * Description:
 *
 * Return the state of parking stall 2. The routine evaluates whether
 * the current parking stall 2 proximity sensor has changed
 * significantly. If a car is detected, then OCCUPIED is returned,
 * otherwise EMPTY is returned
 */
int PrevStall2Val = -1; // previous state of parking stall 2 reading 
int GetParkingStall2State() {
   
   long Stall2SensorVal = ProximityVal(Stall2SensorPin); //Check parking space 2
   if (PrevStall2Val == -1) {
      PrevStall2Val = Stall2SensorVal;
   }
   Serial.print("  Stall 2 = ");

   Serial.println(Stall2SensorVal);

   if(abs(PrevStall2Val - Stall2SensorVal) > 5) {
      return OCCUPIED;
   }
    
   return EMPTY;
}

/**
 * int GetParkingStall3tate()
 *
 * Parameters: None
 *
 * Description:
 *
 * Return the state of parking stall 3. The routine evaluates whether
 * the current parking stall 3 proximity sensor has changed
 * significantly. If a car is detected, then OCCUPIED is returned,
 * otherwise EMPTY is returned
 */
int PrevStall3Val = -1; // the previous parking stall 3 reading
int GetParkingStall3State() {
   
   long Stall3SensorVal = ProximityVal(Stall3SensorPin); //Check parking space 3
   if (PrevStall3Val == -1) {
      PrevStall3Val = Stall3SensorVal;
   }
   Serial.print("  Stall 3 = ");

   Serial.println(Stall3SensorVal);

   if(abs(PrevStall3Val - Stall3SensorVal) > 5) {
      return OCCUPIED;
   }
    
   return EMPTY;
}

/**
 * int GetParkingStall4tate()
 *
 * Parameters: None
 *
 * Description:
 *
 * Return the state of parking stall 4. The routine evaluates whether
 * the current parking stall 4 proximity sensor has changed
 * significantly. If a car is detected, then OCCUPIED is returned,
 * otherwise EMPTY is returned
 */
int PrevStall4Val = -1; // the previous parking stall 4 reading
int GetParkingStall4State() {
   
   long Stall4SensorVal = ProximityVal(Stall4SensorPin); //Check parking space 4
   if (PrevStall4Val == -1) {
      PrevStall4Val = Stall4SensorVal;
   }
    
   Serial.print("  Stall 4 = ");
   Serial.println(Stall4SensorVal);
    
   if(abs(PrevStall4Val - Stall4SensorVal) > 5) {
      return 1;
   }
   return 0;
}       

/**
 * long ProximityVal(int Pin)
 *
 * Parameters:            
 * int pin - the pin on the Arduino where the QTI sensor is connected.
 *
 * Description:
 *
 * QTI schematics and specs: http://www.parallax.com/product/555-27401
 * This method initalizes the QTI sensor pin as output and charges the
 * capacitor on the QTI. The QTI emits IR light which is reflected off 
 * of any surface in front of the sensor. The amount of IR light 
 * reflected back is detected by the IR resistor on the QTI. This is 
 * the resistor that the capacitor discharges through. The amount of 
 * time it takes to discharge determines how much light, and therefore 
 * the lightness or darkness of the material in front of the QTI sensor.
 * Given the closeness of the object in this application you will get
 * 0 if the sensor is covered
 */
long ProximityVal(int Pin)
{
   long duration = 0;
   pinMode(Pin, OUTPUT);         // Sets pin as OUTPUT
   digitalWrite(Pin, HIGH);      // Pin HIGH
   delay(1);                     // Wait for the capacitor to stabilize

   pinMode(Pin, INPUT);          // Sets pin as INPUT
   digitalWrite(Pin, LOW);       // Pin LOW
   while(digitalRead(Pin))       // Count until the pin goes
   {                             // LOW (cap discharges)
      duration++;                
   }   
   return duration;              // Returns the duration of the pulse
}

/**
 * void EntryLightGreen()
 *
 * Parameters: None
 *
 * Description:
 *
 * Set the entry light to green.
 */
void EntryLightGreen()
{
   Serial.println( "Turn on entry green LED" );
   digitalWrite(EntryGateGreenLED, LOW);
   digitalWrite(EntryGateRedLED, HIGH);
   
   EntryLightState = GREEN;
}

/**
 * void EntryLightRed()
 *
 * Parameters: None
 *
 * Description:
 *
 * Set the entry light to red.
 */
void EntryLightRed()
{
   Serial.println( "Turn on entry red LED" );
   digitalWrite(EntryGateRedLED, LOW);
   digitalWrite(EntryGateGreenLED, HIGH);
   
   EntryLightState = RED;  
}

/**
 * void EntryLightOff()
 *
 * Parameters: None
 *
 * Description:
 *
 * Turn the entry light off.
 */
void EntryLightOff()
{
   digitalWrite(EntryGateRedLED, HIGH);
   digitalWrite(EntryGateGreenLED, HIGH);

   EntryLightState = OFF;   
}

/**
 * String ToggleEntryLight()
 *
 * Parameters: The new state of the entry light
 *
 * Description:
 *
 * Set the entry light to RED, GREEN, or OFF.
 */
String ToggleEntryLight(String newState)
{
   if (newState == "G") {
      EntryLightGreen();
   }
   else if (newState == "R") {
      EntryLightRed();
   }
   else EntryLightOff();

   return "OK";
}

/**
 * void ExitLightGreen()
 *
 * Parameters: None
 *
 * Description:
 *
 * Turn the exit light green.
 */
void ExitLightGreen()
{
   Serial.println( "Turn on exit green LED" );
   digitalWrite(ExitGateGreenLED, LOW);
   digitalWrite(ExitGateRedLED, HIGH);

   ExitLightState = GREEN;
}

/**
 * void ExitLightRed()
 *
 * Parameters: None
 *
 * Description:
 *
 * Turn the exit light red.
 */
void ExitLightRed()
{
   Serial.println( "Turn on exit red LED" );
   digitalWrite(ExitGateRedLED, LOW);
   digitalWrite(ExitGateGreenLED, HIGH);
   
   ExitLightState = RED;
}

/**
 * void ExitLightOff()
 *
 * Parameters: None
 *
 * Description:
 *
 * Turn the exit light off.
 */
void ExitLightOff()
{
   digitalWrite(ExitGateRedLED, HIGH);
   digitalWrite(ExitGateGreenLED, HIGH);

   ExitLightState = OFF;
}

/**
 * String ToggleExitLight()
 *
 * Parameters: The new state of the exit light
 *
 * Description:
 *
 * Set the exit light to RED, GREEN, or OFF.
 */
String ToggleExitLight(String newState)
{
   if (newState == "G") {
      ExitLightGreen();
   }
   else if (newState == "R") {
      ExitLightRed();
   }
   else ExitLightOff();

   return "OK";
}

/**
 * String ToggleEntryGate()
 *
 * Parameters: The new state of the entry gate
 *
 * Description:
 *
 * Open or close the entry gate. Returns OK regardless of outcome.
 */
String ToggleEntryGate(String newState)
{
   if (newState == "1") {
      OpenEntryGate();
   }
   else if (newState == "0") {
      CloseEntryGate();
   }
   return "OK";
}

/**
 * void OpenEntryGate()
 *
 * Parameters: None.
 *
 * Description:
 *
 * Open the entry gate by actuating the servo.
 */
void OpenEntryGate()
{
   EntryGateServo.write(OpenGate);
   EntryGateState = OPEN;
}

/**
 * void OpenEntryGate()
 *
 * Parameters: None.
 *
 * Description:
 *
 * Close the entry gate by actuating the servo.
 */
void CloseEntryGate()
{
   EntryGateServo.write(CloseGate);
   EntryGateState = CLOSE;
}

/**
 * String ToggleExitGate()
 *
 * Parameters: The new state of the exit gate
 *
 * Description:
 *
 * Open or close the exit gate. Returns OK regardless of outcome.
 */
String ToggleExitGate(String newState)
{
   if (newState == "1") {
      OpenExitGate();
   }
   else if (newState == "0") {
      CloseExitGate();
   }
   return "OK";
}

/**
 * void OpenExitGate()
 *
 * Parameters: None.
 *
 * Description:
 *
 * Open the exit gate by actuating the servo.
 */
void OpenExitGate()
{
   ExitGateServo.write(OpenGate);
   ExitGateState = OPEN;
}

/**
 * void CloseExitGate()
 *
 * Parameters: None.
 *
 * Description:
 *
 * Close the exit gate by actuating the servo.
 */
void CloseExitGate()
{
   ExitGateServo.write(CloseGate);
   ExitGateState = CLOSE;
}

/**
 * String CloseExitGate()
 *
 * Parameters: None.
 *
 * Description:
 *
 * Close the exit gate by actuating the servo.
 */
String ToggleParkingLights(String newState)
{
   int start = newState.indexOf('[');
   int end = newState.indexOf(']');
   String newSettings = newState.substring(start+1,end);

   String body[20];
   int counter = 0;
  
   int i=0;
   String param = "";
   while (i < newSettings.length()) { 
      char c = newSettings.charAt(i);
      if (c == ',') { 
         body[counter] = String (param);
         counter++;
         param = "";
      }    
      else {
         param += c;
      }
      i++;
   }

   for (int j=0; j < counter; j++) {
      String spot = body[j].substring(0,1);
      String state = body[j].substring(2,3);
    
      if (spot == "1") {
         if (state == "1") {
            TurnOnStall1();
         }
         else {
            TurnOffStall1();
         }
      }
      else if (spot == "2") {
         if (state == "1") {
            TurnOnStall2();
         }
         else {
            TurnOffStall2();
         }
      }
      else if (spot == "3") {
         if (state == "1") {
            TurnOnStall3();
         }
         else {
            TurnOffStall3();
         }
      }
      else if (spot == "4") {
         if (state == "1") {
            TurnOnStall4();
         }
         else {
            TurnOffStall4();
         }
      }
   }
   return "OK";
}

/*********************************************************************
 * void TurnOffStall1()
 * Parameters:  None          
 *
 * Description: 
 * This method turns off the LED for parking stall 1
 ***********************************************************************/
void TurnOffStall1() {
   digitalWrite(ParkingStall1LED, LOW);
   ParkingStall1LEDState = OFF;
}

void TurnOnStall1() {
   digitalWrite(ParkingStall1LED, HIGH);
   ParkingStall1LEDState = ON;
}

void TurnOffStall2() {
   digitalWrite(ParkingStall2LED, LOW);
   ParkingStall2LEDState = OFF;
}

void TurnOnStall2() {
   digitalWrite(ParkingStall2LED, HIGH);
   ParkingStall2LEDState = ON;
}

void TurnOffStall3() {
   digitalWrite(ParkingStall3LED, LOW);
   ParkingStall3LEDState = OFF;
}

void TurnOnStall3() {
   digitalWrite(ParkingStall3LED, HIGH);
   ParkingStall3LEDState = ON;
}

void TurnOffStall4() {
   digitalWrite(ParkingStall4LED, LOW);
   ParkingStall4LEDState = OFF;
}

void TurnOnStall4() {
   digitalWrite(ParkingStall4LED, HIGH);
   ParkingStall4LEDState = ON;
}

void EntryGateOpen() 
{
   Serial.println( "Open Entry Gate" );   //Here we open the entry gate
   EntryGateServo.write(OpenGate);
}

void EntryGateClose() 
{
   Serial.println( "Open Entry Gate" );   //Here we open the entry gate
   EntryGateServo.write(CloseGate);
}
/**
 * void printConnectionStatus()
 *
 * Parameters: None
 *
 * Description:
 *
 * Print out the connection information.
 */
void printConnectionStatus() 
{
   // Print the basic connection and network information: 
   // Network, IP, and Subnet mask
   ip = WiFi.localIP();
   Serial.print("Connected to ");
   Serial.print(ssid);
   Serial.print(" IP Address:: ");
   Serial.println(ip);
   subnet = WiFi.subnetMask();
   Serial.print("Netmask: ");
   Serial.println(subnet);
   
   // Print our MAC address.
   WiFi.macAddress(mac);
   Serial.print("WiFi Shield MAC address: ");
   Serial.print(mac[5],HEX);
   Serial.print(":");
   Serial.print(mac[4],HEX);
   Serial.print(":");
   Serial.print(mac[3],HEX);
   Serial.print(":");
   Serial.print(mac[2],HEX);
   Serial.print(":");
   Serial.print(mac[1],HEX);
   Serial.print(":");
   Serial.println(mac[0],HEX);
   
   // Print the wireless signal strength:
   rssi = WiFi.RSSI();
   Serial.print("Signal strength (RSSI): ");
   Serial.print(rssi);
   Serial.println(" dBm");

}


