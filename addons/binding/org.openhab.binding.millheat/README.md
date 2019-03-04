# Millheat Binding

This binding integrates the Mill WIFI enabled panel heaters. See https://www.millheat.com/mill-wifi/

## Supported Things

This binding should support all WIFI enabled heaters as well as the WIFI socket.

* Mill Heating API
* Heater
* Room

## Discovery

The binding will discover both rooms and heaters. 
In order to do discovery, add a thing of type Mill Heating API and add username and password.


## Thing Configuration

```
Bridge millheat:bridge:home "Millheat account/bridge" [username="email@address.com",password="topsecret"] {
    Thing heater heaterOffice "Office panel heater" [ macAddress="F0XXXFFFXX" ]
}
```

## Channels


### Room channels

All channels read only.

* currentTemperature - measured temperature in your room (if more than one heater then it is the average of all heaters)
* comfortTemperature - comfort mode temperature 
* awayTemperature - away mode temperature 
* sleepTemperature - sleep mode temperature 
* heatingActive - heating active

### Heater channels

All channels read only.

* currentTemperature - measured temperature in your room (if more than one heater then it is the average of all heaters)
* heatingActive - heating active


## Full Example

millheat.things:

```
Bridge millheat:bridge:home "Millheat account/bridge" [username="email@address.com",password="topsecret"] {
    Thing heater heaterOffice "Office panel heater" [ macAddress="F0XXXFFFXX" ]
}
```

millheat.items:

```
Number Office_Temperature "Temperature [d °C]" <temperature>  {channel="millheat:heater:home:heaterOffice:currentTemperature"}
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
