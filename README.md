Mephisto II
===========

Deployment
--------

Make sure that the deployment server is started on the raspberry.
Then run:

__mvn clean install -Pdeploy__


UI
--------

Ensure to invoke the whole path to the index.html, e.g. __http://127.0.0.1:8080/resources/index.html__




# About This Project

Motivation
--------
Inspired by mightyohms internet radio I decided to build one on my own too. 
So I build my first radio with an Asus WL520-GU router and an Arduino Ethernet as UI controller: Mephisto I.

The project has been so much fun that I decided to build another one, but with a cheaper and more efficient hardware 
by choosing the Raspberry Pi.

<img src="docs/2013-05-24 11.45.14.jpg" width="400">

<img src="docs/2013-05-24 11.45.44.jpg" width="400">

<img src="docs/2013-05-26 12.51.12.jpg" width="400">

The finished radio.


The Name
--------
Why Mephisto II? Well, my last name means "fist" in German (Faust) and this is the second radio I build, so...

##The Radio Box
What? Wait? Why starting with the box? Well, I learned from my first project that I neither have the tools nor the room 
to build a box on my own. So I decided to lookup a possible box first and
build the radio depending on the design of the choosen box. 
The radio is a gift for a woman who's got an old-fashioned furniture style, 
so I bought a radio box from 1932 from ebay and started "hacking".

##The Hardware
Components used:

- Radio box
- Raspberry Pi
- Raspberry Pi GPIO Cobbler
- Lepai Mini Hi-Fi HiFi Stereo Audio Amplifier
- USB Hub 4-port
- Visaton BG 20 speaker
- 2x 74HC595 8 bit shift register
- USB power adapter for the Raspi
- power adapter for the Lepai amp
- a relay that switches the power source for the Raspi once the amp is turned on
- 12x LED for the front display
- 1x LED as power indicator on the front (the 3mm hole was already there, so I used this as for a power LED)
rotary encoder for station selection
- on/off switch
- ...and blood, sweat and tears ;)

 
Problems, Solutions and Open Issues (Hardware)
--------
the Raspberry Pi GPIO ports: Because of the limited space on the front side, I decided to provide a display 
where the user can select one of twelve stations. Using 3mm LEDs this was the amount of LEDs that fit into the front hole. 
The problem was that Raspi does not have that many GPIO ports, so I found this awesome article for the Arduino that did the trick.
the Lepai amp: The amp wasn't the best choice for this project but does what it is intended to do. Problem: 
the amp produces a constant noise which is pretty annoying. I solved this problem by wiring a 50 Ohm resistor 
on the speaker to reduce the noise. Does someone has a recommendation for an amp in that price category (14€)?
the rotary encoder: GRRRRRRR - BIGGEST PAIN EVER. I was able to get the rotary encoder running on an Arduino Uno. 
So I translated the Arduino code to python and wired it to the Raspi. After fiddling around for hours 
I wasn't able to get the rotary encoder running the same way. I tried the a pythong rotary encoder library on Github 
but found contradictorily wiring schemes for the rotary encoder. I wired the A/B pins to GPIO ports on the Raspi 
but it only works on one direction. I wasn't able to fix this problem yet and for me, it's still the biggest bug on the radio.
The Software
With the power of the Raspberry Pi I decided to build a web-interface the user can configure the stations with. 
Since I already fiddled around with the awesome Adafruit Web IDE, I decided to use Python for the REST service 
and jquery-mobile for a responsive web UI. I've never programmed Python before, thinking everything is better than PHP, 
but realized soon that Python is a bit of a pain too. But building the UI using the bottle REST framework was fun and 
I achieved the desired results in really short time. So I decided to give the radio some "extras". 
I stumbled over the gmusic-python API on github and decided that it would be nice if the radio could play all my mp3 as well. 
So I build an UI for that too. You can see some screenshots below. The UI is in German, but I think you get the idea.

Problems, Solutions and Open Issues (Software)
--------
I stumbled over some problems. Some of them I could solve, some of them remained unsolved. I want to share my knowledge here (and hopefully get some answers to my questions too). Here is a short overview about the main problems I faced building the software for the radio.
radio remote control: The UI supports a remote control for the radio so that the next or previous station can be selected. Maybe it would be better here to let the user choose a station directly. Unfortunatly the LEDs for the station status are not updated when the station is changed via remote control.
radio remote volume control: In a first version I was able to control the radio volume control via UI with a jquery-mobile slider component. The slider change did result in a mpc volume command on the server side. After using an USB sound card, the command did not work anymore and I wasn't able to get it running again. The UI component is still there, but commented out in the HTML.
passing correct stream urls to the UI: The "New Station" dialog contains a test button for new stream URLs. The URLs may work when configuring the playlist on a PC but may not when added to the mpd playlist. A nice solution would be to have some a kind of stream repository that is maintained in the outside world so the user only selected his or her radio stations from a list of predefined stations.
Conclusion
Once again, it was fun to build an internet radio on my own. And I'm looking forward to "make" my next project, even though I don't know yet what it's gonna be. Here are some additional fotos of the build process:
