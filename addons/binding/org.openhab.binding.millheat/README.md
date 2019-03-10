# Millheat Binding

This binding integrates the Mill WIFI enabled panel heaters. See https://www.millheat.com/mill-wifi/

## Getting started

Either

1. Add a new thing in Inbox (assuming PaperUI) of type Mill Heating API with username and password
2. Find your rooms and heaters discovered in the Inbox

OR

1. Add a bridge manually in a .thing file
2. Find your rooms and heaters discovered in the Inbox

OR

1. Add a bridge along with heaters and rooms manually in a .thing file
2. Add corresponding items in an .item file or do this in the UI


## Supported Things

This binding should support all WIFI enabled heaters as well as the WIFI socket.

NOTE: Currently only room heaters work. Might be other missing functionality as I only have a single type installed.

* Mill Heating API - the account bridge
* Heater
* Room

## Discovery

The binding will discover both rooms and heaters. 

In order to do discovery, add a thing of type Mill Heating API and add username and password.


## Thing Configuration

See full example below for how to configure using thing files.

* username = email address used in app
* password = password used in app
* refreshInterval = number of seconds between refresh calls to the server 

## Channels

### Room channels

| Channel        | Read/write           | Description  |
| ------------- | ------------- | ----- |
| currentTemperature      | R | measured temperature in your room (if more than one heater then it is the average of all heaters) |
| comfortTemperature      | R/W | comfort mode temperature  |
| awayTemperature      | R/W | away mode temperature  |
| sleepTemperature      | R/W | sleep mode temperature  |
| heatingActive      | R | whether the heaters in this room are active  |



### Heater channels

| Channel        | Read/write           | Description  |
| ------------- | ------------- | ----- |
| currentTemperature      | R | measured temperature by this heater |
| currentPower      | R | current power usage in watts. Note that the power attribute of the heater config must be set for this channel to be active  |
| heatingActive      | R | whether the heater is active  |

## Full Example

millheat.things:

```
Bridge millheat:bridge:home "Millheat account/bridge" [username="email@address.com",password="topsecret"] {
    Thing room office "Office room" [ roomId="200000000000000" ]
    Thing heater office "Office panel heater" [ macAddress="F0XXXXXXXXX", power=900 ]
} 
```

millheat.items:

```
// Items connected to ROOM channels
Number Heating_Office_Room_Current_Temperature "Office current [%d °C]" <temperature>  {channel="millheat:room:home:office:currentTemperature"}
Number Heating_Office_Room_Sleep_Temperature "Office sleep [%d °C]" <temperature>  {channel="millheat:room:home:office:sleepTemperature"}
Number Heating_Office_Room_Away_Temperature "Office away [%d °C]" <temperature>  {channel="millheat:room:home:office:awayTemperature"}
Number Heating_Office_Room_Comfort_Temperature "Office comfort [%d °C]" <temperature>  {channel="millheat:room:home:office:comfortTemperature"}
Switch Heating_Office_Room_Heater_Active "Office active [%s]" <fire>  {channel="millheat:room:room:office:heatingActive"}

// Items connected to HEATER channels
Number Heating_Office_Heater_Current_Wattage "Watts consuming [%d W]" <temperature>  {channel="millheat:heater:home:office:currentPower"}
Number Heating_Office_Heater_Current_Temperature "Heater current [%d °C]" <temperature>  {channel="millheat:heater:home:office:currentTemperature"}
Switch Heating_Office_Heater_Heater_Active "Heater active [%s]" <fire>  {channel="millheat:heater:home:office:heatingActive"}
```

