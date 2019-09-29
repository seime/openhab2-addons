# Sensibo Binding

This binding integrates the Sensibo Sky aircondition remote control 
See https://www.sensibo.com/

## Supported Things

This binding supports Sensibo Sky only.

* `account` = Sensibo API - the account bridge
* `sensibosky` = Sensibo Sky remote control

## Discovery

In order to do discovery, add a thing of type Sensibo API and add the API key. API key can be obtained here: https://home.sensibo.com/me/api

## Thing Configuration

See full example below for how to configure using thing files.

### Account

* `apiKey` = API key obtained here: https://home.sensibo.com/me/api
* `refreshInterval` = number of seconds between refresh calls to the server

### Sensibo Sky

* `macAddress` = network mac address of device.

Can be found printed on the back of the device
Or you can find it during discovery.

## Channels

### Sensibo Sky

| Channel             | Read/write    | Item type             | Description |
| ------------------- | ------------- | --------------------- | ----------- |
| currentTemperature  | R             | Number:Temperature    | Measured temperature  |
| currentHumidity     | R             | Number:Dimensionless  | Measured relative humidity, reported in percent |
| targetTemperature   | R/W           | Number:Temperature    | Current target temperature for this room |
| masterSwitch        | R/W           | Switch                | Switch AC ON or OFF |
| mode                | R/W           | String                | Current mode (cool, heat, etc, actual modes provided provided by the API) being active |
| fanLevel            | R/W           | String                | Current fan level (low, auto etc, actual levels provided provided by the API |
| swingMode           | R/W           | String                | Current swing mode (actual modes provided provided by the API |

## Full Example

sensibo.things:

```
Bridge sensibo:account:home "Sensibo account" [apiKey="XYZASDASDAD", refreshInterval=120] {
    Thing sensibosky office "Sensibo Sky Office" [ macAddress="00:11:22:33:44:55" ]
}
```

sensibo.items:

```
Number:Temperature AC_Office_Room_Current_Temperature "Temperature [%.1f °C]" <temperature>  {channel="sensibo:sensibosky:home:office:currentTemperature"}
Number:Dimensionless AC_Office_Room_Current_Humidity "Relative humidity [%.1f %%]" <humidity  >  {channel="sensibo:sensibosky:home:office:currentHumidity"}
Number:Temperature AC_Office_Room_Target_Temperature "Target temperature [%d °C]" <temperature>  {channel="sensibo:sensibosky:home:office:targetTemperature"}
String AC_Office_Room_Mode "AC mode [%s]" {channel="sensibo:sensibosky:home:office:mode"}
String AC_Office_Room_Swing_Mode "AC swing mode [%s]" {channel="sensibo:sensibosky:home:office:swingMode"}
Switch AC_Office_Heater_MasterSwitch "AC power [%s]" <switch>  {channel="sensibo:sensibosky:home:office:masterSwitch"}
String AC_Office_Heater_Fan_Level "Fan level [%s]" <fan>  {channel="sensibo:sensibosky:home:office:fanLevel"}
```
