# Panasonic Comfort Cloud Binding

This binding integrates Panasonic Comfort Cloud air conditioners.

## Supported Things

This binding supports Panasonic Air Conditioners that are connected to the internet via WiFi and can be controlled via
the Panasonic Comfort Cloud app.

* `account` = Panasonic Comfort Cloud Account - the account bridge
* `aircondition` = An air conditioner

## Discovery

In order to do discovery, add a thing of type Panasonic Comfort Cloud API and add username and password.

## Thing Configuration

See full example below for how to configure using thing files.

### Account

* `username` = Same as you use in the mobile app
* `password` = Same as you use in the mobile app
* `refreshInterval` = number of seconds between refresh calls to the server

### Airconditioner

* `deviceId` = id of aircondition device

Can be found printed on the back of the device. Or you can find it during discovery.

## Channels

Note: Supported values for most String channels are reported as a thing property!

| Channel                   | Read/write | Item type            | Description                                                                |
|---------------------------|------------|----------------------|----------------------------------------------------------------------------|
| masterSwitch              | R/W        | Switch               | Switch AC ON or OFF                                                        |
| currentIndoorTemperature  | R          | Number:Temperature   | Measured indoor temperature                                                |
| currentOutdoorTemperature | R          | Number:Temperature   | Measured outdoor temperature                                               |
| targetTemperature         | R/W        | Number:Temperature   | Target temperature for this room / setpoint                                |
| operationMode             | R/W        | String               | Current mode (COOL, HEAT, etc, actual modes provided provided by the API)  |
| airSwingAutoMode          | R/W        | String               | Current auto air swing mode (AUTO, LEFT_RIGHT etc, see thing properties)   |
| airSwingHorizontal        | R/W        | String               | Current horizontal air swing mode (LEFT, CENTER etc, see thing properties) |
| airSwingVertical          | R/W        | String               | Current vertical air swing mode (TOP, BOTTOM etc, see thing properties)    |
| ecoMode                   | R/W        | String               | Current eco mode (AUTO, POWERFUL, QUIET)                                   |
| nanoe                     | R          | String               | Nanoe mode (UNAVAILABLE, OFF, ON, MODE_G, ALL)                             |
| actualNanoe               | R          | String               | Actual Nanoe mode (UNAVAILABLE, OFF, ON, MODE_G, ALL)                      |

Some channels are still missing like iAutoX and ecoNavi.

## Full Example

panasoniccomfortcloud.things:

```
Bridge panasoniccomfortcloud:account:accountName "Panasonic Comfort Cloud account" [ username="XXX@XXX.COM", password="XXXXXXX", refreshInterval="120" ] {
  Thing aircondition bedroom1 "AC Bedroom" [ deviceId="CS-TZ25WKEW+XXXXXXXX" ]
}
```

panasoniccomfortcloud.items:

```
Switch masterSwitch "AC on/off" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:masterSwitch"}
Number:Temperature currentIndoorTemperature "Inside temperature" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:currentIndoorTemperature"}
Number:Temperature currentOutdoorTemperature "Outside temperature" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:currentOutdoorTemperature"}
Number:Temperature targetTemperature "Target temperature" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:targetTemperature"}

String operationMode "Mode" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:operationMode"}
String airSwingAutoMode "Air swing auto mode" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:airSwingAutoMode"}
String airSwingVertical "Vertical air direction" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:airSwingVertical"}
String airSwingHorizontal "Horizontal air direction" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:airSwingHorizontal"}

String ecoMode "Eco mode" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:ecoMode"}
String fanSpeed "Fan speed" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:fanSpeed"}

String nanoe "Nanoe" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:nanoe"}
String actualNanoe "Actual Nanoe" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:actualNanoe"}
```
