#!/bin/sh
sudo apt-get update
sudo apt-get install hostapd dnsmasq  
sudo mkdir /etc/hostapd/
sudo touch /etc/hostapd/hostapd.conf

unzip hostapd.zip
sudo mv /usr/sbin/hostapd /usr/sbin/hostapd.bak
sudo mv hostapd /usr/sbin/hostapd.edimax
sudo ln -sf /usr/sbin/hostapd.edimax /usr/sbin/hostapd
sudo chown root.root /usr/sbin/hostapd
sudo chmod 755 /usr/sbin/hostapd