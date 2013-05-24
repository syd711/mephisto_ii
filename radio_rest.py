import json
import getpass
import sys
import time
import telnetlib
import subprocess
import mpc
import ConfigParser
from bottle import route, run
from bottle import static_file 
from bottle import get, post, request

# Config init
###################################################
radioConfigFile = "radio.ini"
config = ConfigParser.RawConfigParser()
config.read(radioConfigFile)

# Server Settings
SETTING_UI_FOLDER = config.get('HttpServer', 'server_htdocs');
SETTINGS_STATION_READ_WAIT = int(config.get('MPD', 'mpd_read_delay'))

# Station List
###############################################################################
@route('/stations/list')
def listStations():
	return getStationList()
	
# Station Reload List
###############################################################################
@route('/stations/reload')
def reloadStations():
	stationList = getStationList()
	mpc.loadId3Tags(stationList)
	return stationList

# Station Edit
###############################################################################
@route('/station/edit/<id>')
def editStation(id):
	index = int(id)
	if(index < 0):
		stationStore = dict()
		stationStore.update({'url': ''})
		stationStore.update({'title': ''})
		stationStore.update({'id': '-1'}) #id for new stations
		return stationStore;
		
	stationList = getStationList()
	return stationList['stations'][index]
	
# Station Save
###############################################################################
@route('/station/save/<id>', method='POST')
def saveStation(id):
    if (id != 'undefined'):
        stationUrl = request.forms.url
        # check if the station is defined	
        index = int(id)
        stationList = getStationList()
        stationCount = len(stationList['stations'])
        if(len(stationUrl) > 5):
            if(index >= 0):
                pos = index+1;
                mpc.sysCmd('mpc del ' + str(pos))
            mpc.sysCmd('mpc add ' + stationUrl)
            if(index >= 0):
                mpc.sysCmd('mpc move ' + str(stationCount) + ' ' + str(pos))
            mpc.savePlaylist()	
            return "true"        
	return "false"

# Station Delete
###############################################################################
@route('/station/delete/<id>')
def deleteStation(id):
    if (id != 'undefined'):
        index = int(id)
        pos = index+1
        mpc.sysCmd('mpc del ' + str(pos))
        mpc.savePlaylist()
        return "true"
    return "false"
	
# Station Move
###############################################################################
@route('/station/move/<posFrom>/<posTo>')
def moveStation(posFrom, posTo):
    mpc.telnetCmd('move ' + posFrom + ' ' + posTo)
    mpc.savePlaylist()
    return posTo

# Volume Edit
###############################################################################
@route('/radio/volume/<volume>')
def editVolume(volume):
	mpc.sysCmd('mpc volume ' + volume)
	return volume
	
# Volume Get
###############################################################################
@route('/radio/volume')
def getVolume():
	vol = mpc.sysCmd('mpc volume').strip()
	return vol[8:-1]

# Remote Control: Current Station
###############################################################################
@route('/radio/control/<action>')
def radioControl(action):
    if(action ==  'load'):
        mpc.loadPlaylist()
    else:
        mpc.sysCmd('mpc ' + action)
	time.sleep(SETTINGS_STATION_READ_WAIT)
    return mpc.sysCmd('mpc current')


# Static file serving
###############################################################################
@route('/ui/<filepath:path>')
def server_static(filepath):
    return static_file(filepath, root=SETTING_UI_FOLDER)
	
	
	
###############################################################################
# Helper
###############################################################################
def getStationList():
    stationList = []
    telnet = mpc.telnetCmd('playlistinfo')
    stationsBlocks = telnet.split('Id')
    for station in stationsBlocks:
        stationItems = station.split('\n')
    	stationStore = dict()
    	stationStore.update({'url': ''})
    	stationStore.update({'title': ''})
    	for stationItem in stationItems:
    		if('Pos:' in stationItem):
    			pos = stationItem[5:]
    			stationStore.update({'id': int(pos)})
    		if('file:' in stationItem):
    			url = stationItem[6:]
    			stationStore.update({'url': url})
    		if('Title:' in stationItem):
    			title = stationItem[7:]
    			stationStore.update({'title': title})
    		if('Name:' in stationItem):
    			name = stationItem[6:]
    			stationStore.update({'name': name})
    	if(len(stationStore['url']) > 0):
    		stationList.append(stationStore)
    	
    result = dict()
    result.update({'stations': stationList})
    result.update({'size': len(stationList)})
    return result
