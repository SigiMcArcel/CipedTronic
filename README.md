# BikeTachometer

Hardware and software for measuring distance of a bike with a mobil phone.
The needed information will get thru the hub dynamo of the bike. 
A microcontroller prozesses the analog input data.
the mobil gets the data thru a serial interface or alternative (may be next project) with ANT+ or BLE

## Hardware
The hub dynamo generates sinus waves up to 30 Volts.
The signals has to format for microcontroller analog comparator to maximal 3.3/ 5 volts and half sinus wave.
this is made with a Z-Diode and a rectifier diode

### Z-Diode limiting
Thanks to https://www.elektronik-kompendium.de/sites/slt/1012151.htm
We need to limit the z-Diode current to save the diode and stabilisation
Izmax = Ptot/Uz => 0,5 Watt/3.3V = 0.15 Ampere
Izmin = 0,1 * Izmax = 0,1 * 0,1 Ampere = 0.015 Ampere
Itot = izmax + Izmin = 0,166 Ampere
Rv = (Utot(max dynamo) - Uz)/Itot = (ca 30V - 3,3V)/0,166 Ampere = 160 Ohm
Take 220 Ohm. May be 1K would be better

Measuring:

Peak 3.7 Volts ??

### Microcontroller comparator
On slow revolutions the output of the dynamo is about 200 mV.
So wheel has to have a minimal rpm for generate pulses.
we Set the minimal pulse voltage to 300 mv.
Therfore we use a voltage divider for the comparator Input of the MCU.

https://www.digikey.ch/de/resources/conversion-calculators/conversion-calculator-voltage-divider

10000 Ohm <=> 680 Ohm 
5 V <=> 0,318 Volt

OR

0000 Ohm <=> 560 Ohm 
5 V <=> 0,318 Volt

We use a microcontroller voltage of 5V (depending of the USB serial Converter VCC).
The Microntroller is a ATMEGA48.







