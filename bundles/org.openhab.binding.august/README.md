# Yale Access / August Binding for internet connected locks

This binding integrates some Yale/August locks that are linked to a Yale/August Connect WiFi Bridge.

## Supported Things

This binding supports door locks that are connected to the internet via a Yale Connect WiFi bridge and can be controlled
via the Yale Access app.

Only tested with Yale Doorman over WiFi bridge, but other August locks such as Yale Linus *may* work as well.
Please report back if your lock work/do not work.

* `account` = August/Yale Access account
* `lock` = A door lock connected with a WiFi bridge

## Login (READ THIS!)

Yale Access uses 2-factor authentication at first login.

1. Add a new `account` thing with phone, email and password
2. Check your email for the 6 digit code
3. Update the thing configuration with the code

## Thing Configuration

See full example below for how to configure using thing files.

To find the `lockId`, add the bridge and let it discover your locks

### Account

* `email` = Email address used in the mobile app
* `phone` = Mobile number used in the mobile app. Use full number with country code, ie `+4712345678`
* `password` = Same as you use in the mobile app
* `refreshInterval` = number of seconds between refresh calls to the server. This applies to the bridge itself, not the
  locks. They can be configured individually.
* `validationCode` = one time code requested from the service after authenticating with email + phone + password.

### Lock

* `lockId` = id of lock, typically a long string of numbers and letters
* `refreshInterval` = number of seconds between refresh calls to the server

## TODO

* Support for push messages via PubSub.
* Support 2-factor code via SMS

## Tested devices

* Yale Doorman L3 with Yale/August Connect WiFi Bridge
* Yale Doorman V2N with Access module and WiFi bridge

## Channels

Only a few channels have been added so far, but quite a bit more data is available.
If you feel something important is missing, take a look
in [lock details response](src/test/resources/get_lock_response.json) and report back/create a PR.

| Channel   | Read/write | Item type            | Description                                        |
|-----------|------------|----------------------|----------------------------------------------------|
| lockState | R/W        | Switch               | State of locking bolt, ON = locked, OFF = unlocked |
| doorState | R          | Contact              | Whether the door is OPEN or CLOSED                 |
| battery   | R          | Number:Dimensionless | Remaining battery percentage                       |

## Requesting latest status from lock

Sending a `RefreshType` command to the `lockState` channel will query the lock itself instead of relying on the cloud
status. This may take 10-20 seconds to complete, but you get the latest and most accurate state.

## Full Example

august.things:

```
Bridge august:account:accountName "Yale Access account" [ email="XXX@XXX.COM", phone="+4712345678", password="XXXXXXX", refreshInterval="120", validationCode="REPLACE" ] {
  Thing lock frontdoor "Front door" [ lockId="344KJLK32KJ234LKJ234JLKJK34" ]
}
```

august.items:

```
Switch Front_Door_Lock "Front door lock" <lock>  {channel="august:lock:accountName:344KJLK32KJ234LKJ234JLKJK34:lockState"}
Contact Front_Door_Contact "Front door ajar [%s]" <door>   {channel="august:lock:accountName:344KJLK32KJ234LKJ234JLKJK34:doorState"} 
Number:Dimensionless Front_Door_Battery "Front door battery [%d%unit%]" <battery>   {channel="august:lock:accountName:344KJLK32KJ234LKJ234JLKJK34:battery"} 
```
