#  feasibility study. Bicycle Speedometer
Hardware and software for measuring distance of a bike with a mobil phone.
The needed information will get thru the sine wave of the hub dynamo of the bike.
A hardware converts the sine wave to a TTL signal. 
A microcontroller counts the pulses
The mobil gets the data thru Bluetooth LE
A java app receive the Bluetooth LE data and shows the value on a GUI
The hardware must be simple and small for covering in a bicycle lamp.


#The Application
The microcontroller sends the 

count of pulses (distance), 
the count of pulses per second (velocity)
the count of pulses per second average
the count of pulses per second maximum
The count of pulses will save in the microcontrollers EEPROM

The Android App calculates the informations to Km/h and Km with settings like pulses per revolution and wheel radius,
this data will show in the GUI.

In the first Version i implemented the nordic UART Service and send and receive the data as string.
In the further versions i used a custom service and characteristics to exchange the data.So i spent code and was more comfortable to handle it 

The next idea is to use the standard Bluetooth LE service "Cycling Speed and Cadence" to exchange the data.
So i can use my speedometer with other android Apps.
Therfore i have to reimplement the the pulse measuring.

further steps are :
- Porting the software to a seed XIAO ESP32C3 is lot smaller and has akku load functions
- Combine the hardware with LED Lightning and Supercaps
- extend the android app with "Tours" functionality. So i can set several tours.

##Hardware Komponents
ATMEGA 32u4 Arduino Leonard0
Adafruit nrf80001 breakout
Samsung X-Cover Pro Smartphone Android SDK 30

#Developer Tools
Microchip studio for ATMEGA 32u4
Android studio for Android App developing
LT Spice for developing the Converter Hardware


## Hardware for pulse converting
The big Problem is to handle the voltage on unloaded state.
The voltage can gos up to > 20 Volts.

I'm still looking for a good and sinmplesolution for this problem

##Software
###Microcontroller
Project : CipedTronicMCU
The Software uses fololowing sources:
the nordic nrf80001 sdk
The USB CDC from J. Fiene & J. Romano https://github.com/PennRobotics/acrobot 

USBSerial:
For debugging i implemented a Modul for receive and send string and chars thru the usb interface of the ATMEGA 32u4.
Therfore i'm using the USBDCD source.

Bluetooth LE:
The Atmega 32u4 communicate thru SPI with the Bluetooth LE Breakout.
In the modul BLE.cpp all needed step are implemented for using the breakout.
The nordic sdk includes the necessary functions.
BLE.cpp is written in c++ for compatibility to the nordic sdk.

Evaluate the sine waves of the hub dynamo:
For that i use the counter 1 of the Atmega 32u4. it is count the pulses.
For prevent the overflow after 16 Bit, the software adds the actual counter value to a 32 bit variable and deletes the counter.
Every second the software checks the count of pulses since last call ang gives the count of pulses per second (Counter.c ,Timer.c)

maximum km's:
(Unloaded)
wheel radius = 37 cm, pulses per revolution = 18
circumference per revolution = 2 * 0,00037 kilometers * pi =    0,00232477856 kilometers
pulses per km = 1/0,00232477856 * 18 = 7742 Pulses
or 0,12915436 meters per puls

u32Bit =  4294967295 /7742 = 554761 km. (less than i thought,may be i need a u64Bit Integer)

accuracy:
depends on tire pressure and wight.
Loaded ->
Assumed 4,5 Bar pressure and 70 kg weight a different of 5 mm
circumference per revolution = 2 * 0,000365 kilometers * pi =    0,00229336263 kilometers
pulses per km = 1/0,00229336263 * 18 = 7 848 Pulses
or  0,12740903 meters per pulse									 
unloaded <-> loaded ->
6 Pulses -> 76 cm

almost a metre per kilometre. its a lot !!
But I found a test of gps tracker on Internet. Result : Real distance 5Km , gps tracker 5,14 km.
the speedometer  calculates :  5 km -> 5 * 76 cm = 380 cm. Maybe better.
																 			

###Android App
The App is implemented as a Viewmodel Pattern.
The BLE part is implemented fully asynchron. 
Actually the BLE part is written as a singleton class. But it prepared for using as Android service.
The data exchange between class and Viewmodel works with events and livedata.
GUI:
The App consists three Fragments there's is collected as tabs.
Main and Datafragments :
- Shows the velocity and distance
- Resets the counter
Devicefragment:
- Searching and select cipedtronic devices
Settingsfragment:
- Sets wheel raduis and pulses per revolution

classes:
BLEDevice.java : Consist the BLE Stack with all needed functionality.
CipedTronicMCU.java : Opens BLE services,handle characteristics, calculate mcu data
PageViewModel.java : handles data exchange beetween Fragments, starts the MCU/BLE Part

SectionsPagerAdapter.java : Handles Tabs and Fragments
DataFragment.java : The data GUI
DeviceFragment.java : The device search fragment
SettingsFragment.java : The settings fragment


#Branches:
##main
BLE Uart Version of Cipedtronic
##BLENonUart
 the CUstom and standard service 


