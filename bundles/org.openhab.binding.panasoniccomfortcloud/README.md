# Panasonic Comfort Cloud Binding

This binding integrates Panasonic Comfort Cloud air conditioners.

## Supported Things

This binding supports Panasonic Air Conditioners/heat pumps that are connected to the internet via WiFi _and_ can be
controlled via the Panasonic Comfort Cloud app.

* `account` = Panasonic Comfort Cloud Account - the account bridge
* `aircondition` = An air conditioner or WiFi dongle

## Discovery

In order to do discovery, add a thing of type Panasonic Comfort Cloud API (`account`) and add `username` and `password`.

## Thing Configuration

See full example below for how to configure using thing files.

### account

* `username` = Same as you use in the mobile app (_mandatory_)
* `password` = Same as you use in the mobile app (_mandatory_)
* `appVersion` = The version of your Panasonic Comfort Cloud mobile app. You can find this information in the
  application section of your phone (_mandatory_ with default value). Current default appVersion is `1.17.0`.
* `refreshInterval` = Number of seconds between refresh calls to the server (_optional_)

NOTE: If your account refuses to go online with error message `New app version published - check the version number of
your mobile app and enter the value as account config parameter (currently using <current version>)`, update
the `appVersion` config field. This _may_ work if the API has not changed too much.

### aircondition

* `deviceId` = id of air condition device (_mandatory_)

DeviceId can be found printed on side or back of the device. Or you can find it during discovery.

Devices with built-in WiFi support appears to use format 'MODEL+SERIAL' while devices with a separate WiFi dongle
usually only uses the 'SERIAL' part.

#### Finding the correct deviceId for use in thing files

If you are using *thing* files: Device discovery will create a modified thingUID that follows the openHAB
requirements. In order to find the _correct_ deviceId to use in thing files, _check discovered thing properties_.

## Tested devices

The following devices have been *tested* by users and found to be working. Currently no devices are *not* known to work.

If your device is *not* listed, please test
and [report back](https://community.openhab.org/t/panasonic-comfort-cloud-binding/133848) so I can add it to the list.

* CS-MTZ16WKE
* CS-HZ35XKE
* CS-NZ9SKE with CZ-TACG1 dongle
* CS-TZ20WKEW
* CS-TZ25WKEW & CU-3Z68TBE (multisplit: 3 indoor units with 1 outdoor unit)
* CS-TZ35WKEW & CU-TZ35WKE
* CS-TZ42WKEW
* CS-Z20XKEW (multi-split)
* CS-Z25VKEW
* CS-Z25XKEW (multi-split)
* CS-Z35XKEW (multi-split)
* CS-Z42VKEW
* CS-Z50VKEW
* CU-5Z90TBE (multi-split)

> Air-to-water pumps are currently *not* supported.

## Channels

Note: Possible values for most `String` channels are reported as a thing property!

| Channel                     | Read/write | Item type            | Description                                                                |
|-----------------------------|------------|----------------------|----------------------------------------------------------------------------|
| `masterSwitch`              | R/W        | `Switch`             | Switch AC ON or OFF                                                        |
| `currentIndoorTemperature`  | R          | `Number:Temperature` | Measured indoor temperature                                                |
| `currentOutdoorTemperature` | R          | `Number:Temperature` | Measured outdoor temperature                                               |
| `targetTemperature`         | R/W        | `Number:Temperature` | Target temperature / setpoint                                              |
| `operationMode`             | R/W        | `String`             | Current mode (COOL, HEAT, etc, see thing properties)                       |
| `airSwingAutoMode`          | R/W        | `String`             | Current auto air swing mode (AUTO, LEFT_RIGHT etc, see thing properties)   |
| `airSwingHorizontal`        | R/W        | `String`             | Current horizontal air swing mode (LEFT, CENTER etc, see thing properties) |
| `airSwingVertical`          | R/W        | `String`             | Current vertical air swing mode (TOP, BOTTOM etc, see thing properties)    |
| `ecoMode`                   | R/W        | `String`             | Current eco mode (AUTO, POWERFUL, QUIET, see thing properties)             |
| `nanoe`                     | R/W        | `String`             | Nanoe mode (UNAVAILABLE, OFF, ON, MODE_G, ALL)                             |
| `actualNanoe`               | R          | `String`             | Actual Nanoe mode (UNAVAILABLE, OFF, ON, MODE_G, ALL)                      |

Some channels are still missing like iAutoX and ecoNavi. If you have a device that supports these functions, please
[contact me on the forum](https://community.openhab.org/t/panasonic-comfort-cloud-binding/133848)

## Reported issues

* Temperature measurements (outdoor/indoor) *may* report false values when AC is off.

## Full Example

panasoniccomfortcloud.things:

```
Bridge panasoniccomfortcloud:account:accountName "Panasonic Comfort Cloud account" [ username="XXX@XXX.COM", password="XXXXXXX", refreshInterval="120", appVersion="Defaults to 1.17.0, check current mobile app version and bump to this if trouble" ] {
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
