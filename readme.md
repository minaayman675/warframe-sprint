# Warframe Sprint

Provides a better implementation of sprinting in Warframe.

## Setup

This program currently assumes you use some very specific key bindings.

| Key | Binding | Reasoning |
| --- | --- | --- |
| `Control` | **UNBOUND** | Left Control is used by this program to toggle sprint on and off |
| `Shift` | Move Down <br> Hold to Crouch | You need to press this frequently for sliding, so it makes more sense on Shift than on Control. Sprinting is temporarily disabled while this is held. |
| `Y` | Chat | I use `T` for more important things. If you press `Y`, the program will turn off `]` spamming so as not to screw up your chat. |
| Mouse Button 1 | Fire Weapon | Default binding; sprinting is temporarily disabled while this is held |
| Mouse Button 2 | Aim Weapon | Default binding; sprinting is temporarily disabled while this is held |
| Mouse Button 3 | Secondary Fire | Default binding; sprinting is temporarily disabled while this is held |
| Mouse Button 4 | **UNBOUND** | This will spam mouse button 1 while it is held. Some guns have *amazing* fire rates, but are unfortunately semi-automatic. |
| `]` | Sprint | This is the key that this program spams to make you sprint |
| `Delete` (Or any key of your choosing that is out of the way) | Sprint/Roll | Since sprinting is handled automatically, roll needs to be put on a separate key. Sprint/Roll has to be bound to something, so I picked something far away from my hand. |
| `Alt` (Or any key of your choosing) | Roll | Alt is conveniently unbound by default, and in prime position for rolling with |

Additionally, **toggle sprint must be turned off** in your control settings.

## Usage

In your command line run `java -jar warframe-sprint.jar`

## Download

Grab the jar file from the
[latest release](https://github.com/zkxs/warframe-sprint/releases/latest).

## Known Issues

* This *really* needs user-customizable key settings
* Sometimes the program misses an event, resulting in sprint getting stuck in the off state until you click whatever
  mouse button the program thinks is pressed
* Sometimes the program kicks in a little too late. This is most noticeable when you are trying to aim and it kicks you
  out.
* It turns out mashing the sprint button while doing the Corpus hacking minigame does some really bizarre stuff...
  you're going to want to turn sprint off before you start a Corpus hack...
* I'm typing ]]]]]]]]]]]]] everywhere ]s]e]n]d] ]h]e]l]p]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

## Frequently Asked Questions

### How does this work?

In order to create the uninterruptable sprinting effect, it holds the `]` key down, and extremely quickly releases and
represses it once every 200ms.

Auto fire just taps Mouse Button 1 every 20ms. (That's 50 clicks/second, which should be sufficient).
Due to the way some operating systems handle sleeping threads, going below 20ms would be far more complicated and use
much more CPU. (The current CPU usage of this program is very low)

### Why did you make this?

There isn't much of a reason *not* to sprint in Warframe, and the default sprint behavior is hold-to-sprint. This kills
the little-finger.

But wait, Warframe has an option to use toggle sprint! Unfortunately, there are many actions that cancel the sprint,
requiring the player to build up a complex muscle memory to turn it back on at the appropriate times.

Actions that toggle sprint off in the base game include:

* Firing
* Alternate fire
* Aiming
* Crouching (but sliding is ok)
* Hard landings (this isn't even a user action, why on earth does it cancel sprinting?)

Eventually I got tired of trying to figure out if my warframe looked like it was sprinting or not, and whipped this up.

### This program is bad, and you should feel bad.

![I'm just a programmer for fun](http://i.imgur.com/0Llrffi.jpg)

### Who made this?

Mostly 2015 me, who had some very different opinions about code style and readability than 2017 me.
