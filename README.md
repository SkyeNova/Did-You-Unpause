# Did You Unpause?

A tiny [Fabric](https://fabricmc.net/) client mod for Minecraft 1.21.9/1.21.10 that fixes one very
specific, very annoying [Flashback](https://github.com/Moulberry/Flashback) mistake: pausing the
recording to go do something else (visit the Nether, alt-tab, whatever), then forgetting to
unpause it once you're back.

If Flashback's recorder is paused and you keep playing, this mod throws a title on your screen,
plays an anvil-landing sound, and drops a sarcastic chat message reminding you to run
`/flashback unpause`, with clickable buttons underneath to snooze the nagging or disable it until
you unpause. It keeps nagging every so often for as long as you stay active and paused, so it's
hard to miss. It also drops a one-off pun in chat whenever the recording actually pauses or
unpauses.

It only reacts to Flashback's actual state (`Flashback.RECORDER` / `Recorder#isPaused()`), no
guessing, no mixins into Flashback itself, just reading its public API.

## Settings

Press **F9** in-game (rebindable in Options > Controls) to open the settings screen, or open it
from [Mod Menu](https://modrinth.com/mod/modmenu) if you have it installed. From there you can:

- turn the whole mod on/off
- choose what counts as "still playing" while paused: breaking or placing blocks (both on by
  default), moving, and/or looking around (both off by default)
- change how often it re-nags you, and how long the snooze button lasts
- switch to "auto-unpause" mode, where instead of nagging you it just quietly unpauses the
  recording for you and lets you know with a friendly chat message

## Requirements

- Minecraft 1.21.9 or 1.21.10
- [Fabric Loader](https://fabricmc.net/use/) 0.15.10+
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Flashback](https://modrinth.com/mod/flashback) 0.38.0+
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, for the in-menu settings button)

## Building

```
./gradlew build
```

The output jar will be in `build/libs/`. Drop it in your `mods` folder alongside Fabric API and
Flashback.

## Contributing

This is a small companion mod for a specific friend group, not an actively developed project.
See [CONTRIBUTING.md](CONTRIBUTING.md) before opening an issue or PR.

---

<p align="center">
  <a href="https://www.callisto.host/">
    <img src=".github/assets/callisto-wordmark.png" alt="Callisto" width="240">
  </a>
  <br>
  <sub>Made by the people behind <a href="https://www.callisto.host/">Callisto</a></sub>
</p>
