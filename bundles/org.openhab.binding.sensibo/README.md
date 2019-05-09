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

| Channel             | Read/write    | Item type | Description |
| ------------------- | ------------- | --------- | ----------- |
| currentTemperature  | R             | Number:Temperature    | Measured temperature  |
| currentHumidity     | R             | Number    | Measured relative humidity, reported in percent |
| targetTemperature   | R/W           | Number:Temperature    | Current target temperature for this room |
| masterSwitch        | R/W           | Switch    | Switch AC ON or OFF |
| currentMode         | R/W           | String    | Current mode (cool, heat, etc, actual modes provided provided by the API) being active |
| fanLevel            | R/W           | String    | Current fan level (low, auto etc, actual levels provided provided by the API |
| swingMode           | R/W           | String    | Current swing mode (actual modes provided provided by the API |

## Full Example

sensibo.things:

```
Bridge sensibo:account:home "Sensibo account" [apiKey="XYZASDASDAD", refreshInterval=120] {
    Thing sensibosky basement "Sensibo Sky Basement" [ podId="1234566" ]
}
```

sensibo.items:

```
TBD
```
