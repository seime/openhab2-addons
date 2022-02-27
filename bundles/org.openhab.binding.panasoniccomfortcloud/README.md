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

DeviceId can be found printed on side or back of the device. Or you can find it during discovery.

Devices with built-in WIFI support appears to use format 'MODEL+SERIAL' while devices with a separate WIFI dongle only
uses the 'SERIAL' part.

Note: If you are using *thing* files; Device discovery will create a deviceId with '+' replaced with '-'. Check
discovered thing properties to find the correct value for your device.

#### Tested devices

* CS-TZ25WKEW & CU-3Z68TBE (multisplit: 3 indoor units with 1 outdoor unit)
* CS-NZ9SKE with CZ-TACG1 dongle
* CS-TZ35WKEW & CU-TZ35WKE
* CS-TZ20WKEW

## Channels

Note: Possible values for most `String` channels are reported as a thing property!

| Channel                   | Read/write | Item type            | Description                                                                 |
|---------------------------|------------|----------------------|-----------------------------------------------------------------------------|
| masterSwitch              | R/W        | Switch               | Switch AC ON or OFF                                                         |
| currentIndoorTemperature  | R          | Number:Temperature   | Measured indoor temperature                                                 |
| currentOutdoorTemperature | R          | Number:Temperature   | Measured outdoor temperature                                                |
| targetTemperature         | R/W        | Number:Temperature   | Target temperature / setpoint                                               |
| operationMode             | R/W        | String               | Current mode (COOL, HEAT, etc, see thing properties)                        |
| airSwingAutoMode          | R/W        | String               | Current auto air swing mode (AUTO, LEFT_RIGHT etc, see thing properties)    |
| airSwingHorizontal        | R/W        | String               | Current horizontal air swing mode (LEFT, CENTER etc, see thing properties)  |
| airSwingVertical          | R/W        | String               | Current vertical air swing mode (TOP, BOTTOM etc, see thing properties)     |
| ecoMode                   | R/W        | String               | Current eco mode (AUTO, POWERFUL, QUIET, see thing properties)              |
| nanoe                     | R          | String               | Nanoe mode (UNAVAILABLE, OFF, ON, MODE_G, ALL, see thing properties)        |
| actualNanoe               | R          | String               | Actual Nanoe mode (UNAVAILABLE, OFF, ON, MODE_G, ALL, see thing properties) |

Some channels are still missing like iAutoX and ecoNavi.

## Reported issues

* Temperature measurements (outdoor/indoor) *may* report false values when AC is off.
* Not all units supports

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
String operationMode "Mode" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:operationMode"}
Number:Temperature targetTemperature "Target temperature" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:targetTemperature"}
String ecoMode "Eco mode" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:ecoMode"}

String airSwingAutoMode "Air swing auto mode" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:airSwingAutoMode"}
String airSwingVertical "Vertical air direction" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:airSwingVertical"}
String airSwingHorizontal "Horizontal air direction" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:airSwingHorizontal"}
String fanSpeed "Fan speed" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:fanSpeed"}

Number:Temperature currentIndoorTemperature "Inside temperature" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:currentIndoorTemperature"}
Number:Temperature currentOutdoorTemperature "Outside temperature" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:currentOutdoorTemperature"}

String nanoe "Nanoe" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:nanoe"}
String actualNanoe "Actual Nanoe" {channel="panasoniccomfortcloud:aircondition:accountName:bedroom1:actualNanoe"}
```
