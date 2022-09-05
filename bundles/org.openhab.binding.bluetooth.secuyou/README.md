# Secuyou Smart Lock

This extension adds support for
[Secuyou Smart Lock](https://www.secuyou.dk/collections/produkter/products/secuyou-smart-lock-med-venstre-greb) for
terrace doors.

## Supported Things

Following thing type is supported by this extension:

* [Secuyou Smart Lock](https://www.secuyou.dk/collections/produkter/products/secuyou-smart-lock-med-venstre-greb) model
  2.21

The lock must already have been setup in the Secuyou app.

NOTE: Only tested with a single lock of model 2.21 and firmware version 6. Might work somewhat on older models.

NOTE2: Flaky firmware; sometimes when manually locked by touching the device, the lock reports UNKNOWN/IN PROGRESS
status (neither LOCKED nor UNLOCKED) for a while. In the app this can be seen as a grayed out lock symbol while still
reporting that the phone is connected to the lock.

NOTE3: Make sure you have set "Home Lock" to true - or the lock will assume your BLE dongle is a nearby phone and not
trigger auto locking.

| Thing Type ID       | Description                            |
| ------------------- |----------------------------------------|
| secuyou_smart_lock | Secuyou Smart Lock                     |

## Discovery

As any other Bluetooth device, devices are discovered automatically by the corresponding bridge.

## Thing Configuration

Supported configuration parameters for the things:

| Property         | Type    | Default | Required | Description                                                                                                                                                 |
|------------------|---------|---------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| address          | String  |         | Yes      | Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX")                                                                                             |
| pinCode          | Integer |         | No       | Pin code as used in app. Necessary to control lock, but not read status                                                                                     |
| encryptionKey    | String  |         | No       | Hex encoded encryption key. Necessary to control lock but not read status                                                                                   |
| keepAliveSeconds | Integer | 600     | No       | How often a refresh shall occur in seconds. Note that lock changes are pushed, no polling should be necessary. Defaults to -1 (no polling)                  |
| attemptLockRescue | Boolean | false   | No       | When lock reports LOCKING_OPERATION_IN_PROGRESS, try to toggle the lock twice to get accurate state reading without actually changing the lock position     |
| treatLockingInProgressAsLocked | Boolean | false   | No       | When lock reports LOCKING_OPERATION_IN_PROGRESS, treat this as LOCKED if previous known position was UNLOCKED. Warning: Your door may actually be unlocked! |

## Channels

Following channels are supported for `Secuyou Smart Lock` thing:

| Channel ID      | Item Type | Description                                                                                                   |
|-----------------|-----------|---------------------------------------------------------------------------------------------------------------|
| lock            | Switch    | Lock status, ON=Locked, OFF=Unlocked, UNDEF if locking is in progress or lock not initialized                 |
| handle_position | Contact   | Whether the handle is fully closed or open                                                                    |
| battery         | String    | Battery level, GOOD/LOW/CRITICAL/EMPTY. Replace on LOW, only unlocking is possible when CRITICAL or less      |
| home_lock       | Switch    | Mode of operation, ON=Manual unlocking/locking (highly recommended), OFF=Auto locks when BLE connection drops |

## Example

secuyou.things with Bluetooth adapter config included

```
Bridge bluetooth:bluez:hci1 "My BLE dongle" [ address="00:00:00:00:00:00", backgroundDiscovery=false] {
    secuyou_smart_lock my_terrace_door "Secuyou Terrace Door" [ address="00:00:00:00:00:00", pinCode="12345", encryptionKey="2B7E151628A... 32chars", keepAliveSeconds=30]
}
```

secuyou.items:

```
Switch MyDoor_Lock_State "Door locked [%s]" <door> {channel="bluetooth:secuyou_smart_lock:hci1:my_terrace_door:lock"}
Switch MyDoor_HomeLock "Home lock [%s]" <door> {channel="bluetooth:secuyou_smart_lock:hci1:my_terrace_door:home_lock"}
Contact MyDoor_Handle_State "Handle position [%s]" <door> {channel="bluetooth:secuyou_smart_lock:hci1:my_terrace_door:handle_position"}
String MyDoor_Battery "Battery [%s]" <battery> {channel="bluetooth:secuyou_smart_lock:hci1:my_terrace_door:battery"}
```
