ChromaBeam is a digital logic simulation game that uses lasers as signals. The game is heavily inspired by Logic World and OCTOPTICOM.

# Basic information
<details>
  <summary>Click to expand</summary>
  
## Tick
The time unit of the game. All components take 0 ticks of time to interact with beams, unless specified otherwise.

## Beam
The signal transmitters of the game. They are composed of 3 Channels: Red, Green and Blue.<br>
Beams, which are a mixture of the basic colors are called composite beams, and are Yellow, Magenta, Cyan and White.<br>
Composite beams can be filtered into their respective channels by specific components.

## Component
The stuff that interact with beams. They are abundant and fill various roles such as beam routing,
color filtering, and even doing logic with beams.

## Circuit
Large components, which encapsulate a large amount of components inside them in a compact form.<br>
They are created using the duplicator tool (described below).<br>
Circuits, by default, have 0 ticks of delay on their IO, but when nesting circuits (putting circuits inside circuits),
each additional circuit will add 1 tick to the delay of the IO on the encapsulating circuit.

## Duplicator
The built-in tool for manipulating large areas of components.<br>
Has features such as copying/cutting/pasting/deleting selected areas, importing/exporting areas from/to files and turning areas into circuits.

## Color shorthands:
None: X<br>
Red: R<br>
Green: G<br>
Yellow: Y<br>
Blue: B<br>
Magenta: M<br>
Cyan: C<br>
White: W<br>
</details>

# Component descriptions
<details>
  <summary>Click to expand</summary>
  
## Basic
<details>
  <summary>Click to expand</summary>
  
### Block
Blocks beams from passing through
### Tunnel
Only lets beams through a single axis (can be rotated though)
### Emitter
Emits either a white beam, or nothing (2 alternative colors: X, W)
</details>

## Mirrors
<details>
  <summary>Click to expand</summary>
  
### Mirror
Reflects beams at a 90 degree angle. Only reflective on the light-gray part.
### Double sided mirror
Works like a mirror, but both sides are reflective.
### Beam splitter
Reflects beams like a mirror, but also lets them through, creating two beams in a right angle.
</details>

## Logic
<details>
  <summary>Click to expand</summary>
  
### Gate
A basic component for doing logic, takes 1 tick to process input.<br>
Has 2 alternative modes:<br>
#### Normal
When the switch has any incoming signal, emits a beam with the same color as the input.
#### Inverting
When the switch has no incoming signal, emits a beam with the same color as the input.
### Relay
Lets beams pass through when the switch has any incoming signal.<br>
Beams pass through in 0 ticks, but the switch takes 1 tick to process changes.
### Delayer
Takes the input beam and emits it 1 tick later.
### Oracle
When it receives an input, it either emits a white beam or nothing for the duration of the input pulse.<br>
The emitted value is non-deterministic.
</details>

## Colored
<details>
  <summary>Click to expand</summary>
  
### RGB emitter
Emits a beam with any of the colors.<br>
(8 alt. colors: X, R, G, Y, B, M, C, W)
### Colored beam splitter
Splits beams based on their color channels.<br>
Channels that match any channel of the splitter will go through straight, while the rest will be reflected like a mirror.<br>
(6 alt. colors: R, G, Y, B, M, C)
### Colored filter
Only lets through beam channels that match any of the filter's channels.<br>
(6 alt. colors: R, G, Y, B, M, C)
### RGB Oracle
Works like the Oracle, but with all 8 colors.
</details>

## IO
<details>
  <summary>Click to expand</summary>
  
### Display
Lights up with the color of the sum of all incoming beams.
### Smart Display
Lights up when two beams of the same color intersect inside the display at a 90 degree angle.<br>
When beams of different color intersect inside the display, it turns off.<br>
Beams can pass through it in 0 ticks, allowing of pixel-based screens.
### Big Display
Functions like the Display, but connects to any neighboring Big Display, creating a seamless, large display.<br>
All connected Big Displays share the same color.
### Switch
Can be clicked with an empty cursor to toggle it's state.<br>
Blocks beams from passing when it's disabled.<br>
Lets beams through in 0 ticks when it's enabled.
### Button
Lets beams through in 0 ticks while it's being clicked and helde with an empty cursor, otherwise it blocks all beams.
</details>

## Circuit
<details>
  <summary>Click to expand</summary>
  
### Circuit IO port
Used by the Duplicator to create the IO ports on the circuit that will be created from an area containing this port.
</details>
</details>

# Default controls
<details>
  <summary>Click to expand</summary>

## General controls:
    View ingame keybind hints:    F2
    Place label at mouse:    L
    Remove label at mouse:    Shift + L
    Open save/load menu:    F5
    Set TPS limit:    F7
    Open the 'duplicator':    TAB   (the duplicator is a tool for area copy/pasting, deletion, etc. (similar to WorldEdit))

## Mouse controls:
    Place components:    Left click
    Delete components:    Right click
    Pick component:    Shift + Middle click
    Move camera:    Middle click + mouse movement
    Zoom in/out: Scrollwheel

## Component menu navigation (you can hold shift to do these in the opposite direction):
    Switch between component categories:    T
    Switch between components inside the category:    E

## Component manipulation (you can hold shift to do these in the opposite direction):
    Rotate component:    R
    Switch between alternative types of the component:    Q
    Flip component:    F

## The duplicator controls are shown inside the game based on context.
</details>
