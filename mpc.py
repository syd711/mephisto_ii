import sys
import time
import telnetlib
import subprocess
import ConfigParser

# Config init
###################################################
radioConfigFile = "radio.ini"
config = ConfigParser.RawConfigParser()
config.read(radioConfigFile)

SETTINGS_STATION_READ_WAIT = int(config.get('MPD', 'mpd_read_delay'))

def loadId3Tags(stationList):
    stations = stationList['stations'];
    if(len(stations) > 0):
		sysCmd('mpc play\n')
		for station in stations:
			if(len(station['title']) == 0):
				time.sleep(SETTINGS_STATION_READ_WAIT) 
			sysCmd('mpc next\n')

def sysCmd(cmd):
	log('Executing system command "' + cmd + '"')
	p = subprocess.Popen(cmd, stdout=subprocess.PIPE, shell=True)
	(output, err) = p.communicate()
	return output

def telnetCmd(cmd):
    log('Executing telnet command "' + cmd + '"')
    mpdHost = config.get('MPD', 'mpd_host')
    mpdPort = int(config.get('MPD', 'mpd_port'))
    tn = telnetlib.Telnet(mpdHost, mpdPort)
    tn.read_until('OK MPD 0.16.0')
    tn.write(cmd + "\n")
    result = tn.read_until('OK', 10)
    tn.close()
    return result
    
def savePlaylist():
    sysCmd('mpc rm radio.pls')
    sysCmd('mpc save radio.pls')
    
def loadPlaylist():
    sysCmd('mpc clear')    
    sysCmd('mpc load radio.pls')
    
def getCurrentStation():
    log = sysCmd('mpc status')
    pos = log.find('[playing] #');
    log = log[pos:]
    endPos = log.find('/');
    log = log[11:endPos]
    return int(log)
    
def log(msg):
    print 'Server Log: ' + msg
    return