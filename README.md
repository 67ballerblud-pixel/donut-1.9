# DonutPay Tracker (Fabric, Minecraft 1.21.11)

Client-side, passive payment tracker for DonutSMP, made for **DILLYY_MC**.

## Features
- Detects real `"<name> paid you $<amount>"` server messages (K / M / B / T suffixes supported)
- **Video-style feed**: cards stack flush in the top-right exactly like the video: dark panel, thick accent bar, player head, name, rolling `+$amount`, a shine sweep as each lands and a draining timer bar. Border colour tells you the size at a glance: green < $100M, gold < $1B, red < $10B, magenta < $1T, rainbow above.
- **TOP DONATORS leaderboard** (key `O`, `/donutpay leaderboard`, or automatic for 7s after any $100M+ show): gold-bordered board with a podium (crowned 1st in the middle, 2nd/3rd on the sides), #4-#8 list, count-up amounts, shimmering border and `TOTAL RAISED` footer. The latest donor flashes green.
- **24 full-screen animations**. A payment picks randomly among the shows for its size (never the same one twice in a row); every $1B+ show gets cinematic letterbox bars:

| Size | Shows |
|---|---|
| $1M+ | **Coin Rain**, **Lucky Slots** (three reels land 7-7-7) |
| $5M+ | **Deal With It** (sunglasses drop onto the head) |
| $10M+ | **Breaking News** (ticker), **Stonks** (candlestick chart shoots up) |
| $50M+ | **Wanted Poster** (PAID stamp), **Lightning** (3 strikes), **Diamond** (falls + shatters) |
| $100M+ | **Energy Orb**, **Vault Cracked** (door swings open), **Fireworks** over a skyline |
| $500M+ | **Legendary Coin**, **Meteor Impact** (shockwave + cracks) |
| $1B+ | **Rocket**, **UFO Abduction** (tractor beam) |
| $5B+ | **Jackpot Wheel** |
| $10B+ | **Supernova -> Black Hole**, **Portal** (swirling dimension gate) |
| $50B+ | **God of Donut SMP** (laser eyes), **Total Eclipse** |
| $100B+ | **"JK. YOU'RE RICH."** gold flood, **Money Storm** |
| $1T+ | **Legend** (crown + fireworks), **Big Bang** (spiral galaxy) |

  Every show has its own timed sound cues. Settings: `Tier animations`, `Shows from`, `Show sounds`, `Auto board`, a **show picker + Play** button (any of the 24), **Demo all**, and a **Leaderboard** preview button.
- **Per-server leaderboards**: every payment is saved under the server you were on (`servers.json`). The TOP DONATORS board and the Stats screen show one server at a time: it opens on the server you're connected to, and you switch with `[` / `]` (rebindable) or the `<` `>` buttons in Stats. The board's top-left shows the server name and the top-right shows `[ 2/3 ]`. The first board is **All servers** (your old combined history).
  - `/donutpay servers` lists every server with its total, `/donutpay leaderboard next|prev` switches, and `/donutpay claimhistory` copies your old all-servers history onto the server you're standing on (only if that server has no data yet).
- Scrolling payment-history HUD + session panel (session total, best payment, top payer)
- Stats screen: lifetime total, largest payment, top payers, scrollable history
- Settings GUI (sounds, volume, pitch, style, animation, HUD corner, threshold...)
- Stats saved to `.minecraft/config/donutpay/stats.json`, settings to `config.json`

## Controls
- `K` settings, `J` stats, `O` leaderboard, `[` / `]` previous / next server board (all rebindable), or `/donutpay`, `/donutpay stats`, `/donutpay leaderboard`, `/donutpay servers`, `/donutpay resetsession`

## Build
Requires JDK 21 and Gradle 9.2+ (internet needed for the first build).

    gradle build

Jar: `build/libs/donutpay-tracker-1.9.0.jar` (not the `-sources` jar).
Install: put it plus **Fabric API 0.141.3+1.21.11** in `.minecraft/mods` with Fabric Loader >= 0.18.1.

No Gradle? Push this folder to GitHub: the included workflow builds the jar (Actions -> artifact).

## Safety / verification
- Only listens to **system messages from the server** (`ClientReceiveMessageEvents.GAME`), never signed player chat.
- The pattern is anchored to the start of the line, so chat like `<Bob> X paid you $1B` is ignored.
- Counts only when logged in as the configured owner and (by default) connected to a `donutsmp` server.
- "Preview" buttons are labelled `[PREVIEW]` and are never saved or counted.
- The mod never sends chat/commands/payments, reads no tokens, and makes no network connections.

## If your payments don't show up
DonutSMP's exact wording may differ slightly from the video. Edit `PAID_YOU` in `DonutPayClient.java`
(copy the exact line from your chat) and rebuild. Note: if the server ever relays normal chat as system
messages, a player could imitate a payment line, so treat the tracker as a convenience, not proof of funds.

## Shows (41 full-screen animations)
A payment picks a random show whose range fits its amount (never the same one twice in a row). The settings screen
has a preview button that cycles through every show.

| Range | Shows |
|---|---|
| $1M+ | Coin Rain, Slots, **XP Level** (orbs fly into the XP bar), **Confetti Cannons** |
| $5M+ | Deal With It, **Totem Pop**, **Waves** |
| $10M+ | Breaking News, Stonks, **Disco**, **TNT** (flashing fuse, boom, debris) |
| $25M+ | **Matrix** (digital rain + typed message), **Hologram Scan** |
| $50M+ | Wanted, Lightning, Diamond, **Inferno**, **Hype Train** |
| $100M+ | Energy, Vault, Fireworks, **Neon Synthwave**, **Beacon**, **Glitch** |
| $500M+ | Legendary, Meteor, **Nuke** (countdown, mushroom cloud), **Midas Touch** (gold flood) |
| $1B+ | Rocket, UFO, **Hyperdrive** (warp streaks into a light tunnel) |
| $5B+ | Jackpot |
| $10B+ | Supernova, Portal |
| $50B+ | God, Eclipse |
| $100B+ | Rich, Storm |
| $1T+ | Legend, Big Bang, **Reality Shatter** (cracking glass) |

(bold = added in 1.8)

## Juice (applies to every show)
All 41 shows are wrapped in a shared layer (`Juice.java`) that scales with the payment size:
camera slam-zoom on impact + soft zoom beats + slow push-in, impact flash with shockwave rings, anamorphic streak and
anime speed lines, beat rings, two light sweeps, floating glow motes, vignette, a running light around the screen frame,
an extra low impact sound, and (on big payments) a rotating sunburst behind the show. The accent colour shifts with the
amount. Disable with `"juice": false` in `config/donutpay/config.json`.
