Mephisto II
===========

Copy files and from resources, this will ensure the hotspot setup for all WLAN chipsets:

Here are the steps again:
* sudo apt-get update  
* sudo apt-get install hostapd dnsmasq
* vi /etc/dnsmasq.conf: interface=wlan0 and dhcp-range=192.168.2.2,192.168.2.100,255.255.255.0,12h
  
  
Enable USB sound card:

