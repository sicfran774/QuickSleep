# QuickSleep
A simple and configurable solo-sleep plugin intended for smaller, vanilla-oriented multiplayer servers. When one player sleeps, others have a short window to veto/cancel the night skip.\
\
If there is only one player in the server, sleeping functions as normal and this plugin is effectively disabled.
# Commands

`/sleep timer [seconds]`\
See how long it takes to countdown to skip the night\
*Optional parameter* `[seconds]`\
Sets how long it takes to make it daytime\
Minimum 3 seconds and maximum 60 seconds\
\
`/sleep confirm`\
If the player is in a bed, it will start the countdown before making it daytime\
\
`/sleep cancel`\
Cancels the night skip.\
\
`/sleep message [player] (wakeup | cancel) <message>`\
Change the message it displays when a player cancels a night skip or successfully night skips\
*Optional parameter* `[player]`\
Sets that specific player's messages\
\
Supports MiniMessage format (see here: https://docs.advntr.dev/minimessage/format.html)

# Images

![](https://cdn.discordapp.com/attachments/755355604192591972/1368377410289930391/image.png?ex=68180045&is=6816aec5&hm=622d89b2f462b73728e0b540dba92fe2a191ec75f749adb3a577685194322f45&)

![](https://media.discordapp.net/attachments/755355604192591972/1368377675743494194/image.png?ex=68180085&is=6816af05&hm=d2c216594f3c1df72cf50af4ba2732a5149e8f522265ed8f506911d4d216c673&=&format=webp&quality=lossless)


# Permissions

```
  sleep.*: Access to all below commands
  sleep.confirm: /sleep confirm
  sleep.cancel: /sleep cancel
  sleep.timer: /sleep timer <seconds>
  sleep.message_self: /sleep message (wakeup | cancel) <message>
  sleep.message_all: /sleep message <player> (wakeup | cancel) <message>
```

# Config

```
  # Default timer for countdown (in seconds)
  timer: 10
  # Enable resetting phantom spawn timer (last time since rest)
  reset_phantom_time: true
  # Reset rain on night skip
  reset_rain: true
  # Reset thunderstorms on night skip
  reset_thunderstorm: true
  # Enable QuickSleep functions when alone in server
  enable_when_alone: false
```

# Notes
* Allowing majority vote cancelling coming soon
* Intended for smaller servers, such as with friends
* Found bugs or want to request a feature? Feel free to open an issue here.
