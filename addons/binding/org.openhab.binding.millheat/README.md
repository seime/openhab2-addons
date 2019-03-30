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

* Mill Heating API - the account bridge
* Heater
* Room

## Discovery

The binding will discover both rooms and heaters. 

In order to do discovery, add a thing of type Mill Heating API and add username and password.


## Thing Configuration

See full example below for how to configure using thing files.

### Bridge

* username = email address used in app
* password = password used in app
* refreshInterval = number of seconds between refresh calls to the server 

### Room

* roomId = id of room. Use auto discovery to find this value

### Heater

* macAddress = network mac address of device. Can be found in the app by viewing devices. Or you can find it during discovery. Used for heaters connected to a room
* heaterId = id of device/heater. Use auto discovery to find this value. Used to identify independent heaters or heaters connected to a room
* power = number of watts this heater is consuming when active. Used to provide data for the currentPower channel.

## Channels

### Room channels

| Channel        | Read/write           | Item type | Description  |
| ------------- | ------------- | ----- | ---------- |
| currentTemperature      | R | Number | Measured temperature in your room (if more than one heater then it is the average of all heaters) |
| currentMode      | R | String | Current mode (comfort, away, sleep etc) being active  |
| targetTemperature      | R | Number | Current target temperature for this room (managed by the room program and set by comfort- away- and sleepTemperature)  |
| comfortTemperature      | R/W | Number | Comfort mode temperature  |
| awayTemperature      | R/W | Number | Away mode temperature  |
| sleepTemperature      | R/W | Number | Sleep mode temperature  |
| heatingActive      | R | Switch | Whether the heaters in this room are active  |
| program      | R | String |  Name of program used in this room  |


### Heater channels

| Channel        | Read/write           | Item type |  Description  |
| ------------- | ------------- | ----- | ---- |
| currentTemperature      | R | Number | Measured temperature by this heater |
| targetTemperature      | R/W | Number | Target temperature for this heater. NOTE: Actual changing of temperature is only available if this heater is independent/not connected to a room |
| currentPower      | R | Number | Current power usage in watts. Note that the power attribute of the heater thing config must be set for this channel to be active  |
| heatingActive      | R | Switch | Whether the heater is active/heating  |
| fanActive      | R/W | Switch | Whether the fan (if available) is active (UNTESTED) |
| independent      | R | Switch | Whether this heater is controlled independently or part of a room setup |
| window      | R | Contact | Whether this heater has detected that a window nearby is open/detection of cold air (UNTESTED) |
| masterSwitch      | R/W | Switch | If controlled independently, turn heater ON/OFF (but still controlled by the targetTemperature) |


## Full Example

millheat.things:

```
Bridge millheat:bridge:home "Millheat account/bridge" [username="email@address.com",password="topsecret"] {
    Thing room office "Office room" [ roomId="200000000000000" ]
    Thing heater office "Office panel heater" [ macAddress="F0XXXXXXXXX", power=900, deviceId=12345 ]
} 
```

millheat.items:

```
// Items connected to ROOM channels
Number Heating_Office_Room_Current_Temperature "Office current [%d °C]" <temperature>  {channel="millheat:room:home:office:currentTemperature"}
Number Heating_Office_Room_Target_Temperature "Office target [%d °C]" <temperature>  {channel="millheat:room:home:office:targetTemperature"}
Number Heating_Office_Room_Sleep_Temperature "Office sleep [%d °C]" <temperature>  {channel="millheat:room:home:office:sleepTemperature"}
Number Heating_Office_Room_Away_Temperature "Office away [%d °C]" <temperature>  {channel="millheat:room:home:office:awayTemperature"}
Number Heating_Office_Room_Comfort_Temperature "Office comfort [%d °C]" <temperature>  {channel="millheat:room:home:office:comfortTemperature"}
Switch Heating_Office_Room_Heater_Active "Office active [%s]" <fire>  {channel="millheat:room:home:office:heatingActive"}
String Heating_Office_Room_Mode "Office current mode [%s]" {channel="millheat:room:home:office:currentMode"}
String Heating_Office_Room_Program "Office program [%s]" {channel="millheat:room:home:office:program"}

// Items connected to HEATER channels
Number Heating_Office_Heater_Current_Energy "Energy usage [%d W]" <temperature>  {channel="millheat:heater:home:office:currentEnergy"}
Number Heating_Office_Heater_Current_Temperature "Heater current [%d °C]" <temperature>  {channel="millheat:heater:home:office:currentTemperature"}
Number Heating_Office_Heater_Target_Temperature "Heater target [%d °C]" <temperature>  {channel="millheat:heater:home:office:targetTemperature"}
Switch Heating_Office_Heater_Heater_Active "Heater active [%s]" <fire>  {channel="millheat:heater:home:office:heatingActive"}
Switch Heating_Office_Heater_Fan_Active "Fan active [%s]" <fire>  {channel="millheat:heater:home:office:fanActive"}
Contact Heating_Office_Heater_Window "Window status [%s]" <window>  {channel="millheat:heater:home:office:window"}
Switch Heating_Office_Heater_Independent "Heater independent [%s]" <fire>  {channel="millheat:heater:home:office:independent"}
Switch Heating_Office_Heater_MasterSwitch "Heater masterswitch [%s]" <fire>  {channel="millheat:heater:home:office:masterSwitch"}
```

