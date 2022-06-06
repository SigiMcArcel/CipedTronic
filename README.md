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

Take a higher one 

