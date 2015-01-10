import json
import getpass
import sys
import time
import mpc
import collections
import operator
import ConfigParser
from gmusicapi import Webclient
from bottle import get, post, request, route
from gplayer import Player

googleConfigFile = "google_credentials.ini"
config = ConfigParser.RawConfigParser()
config.read(googleConfigFile)

artistList = []
albumList = []

albumStore = dict()
artistStore = dict()

api = Webclient()
player = Player(api)
loggedIn = False


# Loads all data requested for 
###############################################################################
def loadGoogleData():
    mpc.log('Google login successful')
    songDict = api.get_all_songs()
    mpc.log('Loaded ' + str(len(songDict)) + ' songs')
    
    for song in songDict:
        album = str(unicode(song['album']).encode('utf-8'))
        albumArtUrl = str(unicode(song.get('albumArtUrl','')).encode('utf-8'))
        artist = str(unicode(song.get('artist','')).encode('utf-8'))
        title = str(unicode(song.get('title','')).encode('utf-8'))
        year = str(song['year'])
        id = song['id']
		
        #build album data
        albumDict = albumStore.get(album, dict())
        albumDict.update({'album':album})
        albumDict.update({'year':year})
        albumDict.update({'albumArtUrl':albumArtUrl})
        albumDict.update({'artist':artist})
        albumStore.update({album:albumDict})	
        #add tracks
        tracks = albumDict.get('tracks', [])
        trackDict = createTrackDict(song)
        tracks.append(trackDict)
        tracks.sort(key=operator.itemgetter('track'))
        albumDict.update({'tracks':tracks})
        albumDict.update({'totalTracks':len(tracks)})
        
        #build artist data
        artistDict = artistStore.get(artist, dict())
        artistDict.update({'artist':artist})
        artistTracksList = artistDict.get('tracks', [])
        artistTrackDict = createTrackDict(song)
        artistTracksList.append(artistTrackDict)
        artistTracksList.sort(key=operator.itemgetter('name'))
        artistDict.update({'tracks':artistTracksList})
        artistDict.update({'totalTracks':len(artistTracksList)})
        artistStore.update({artist:artistDict})
   
    convertDictToSortedList(albumStore, albumList)
    convertDictToSortedList(artistStore, artistList)
   	
    mpc.log('Finished Google API initalization')
    return True

# Exit
###########
def exit():
    print 'Stopping Google REST' #player.stop()
   
# Deletes the credentials   
##############################################################################
@route('/google/credentials/delete')
def deleteCredentials():
    player.stop()
    loggedIn = False
    global api
    api.logout()
    config.set('Google', 'login', '')
    config.set('Google', 'password', '')
    with open(googleConfigFile, 'wb') as configfile:
        config.write(configfile)
    return str(True)
    
# Returns the credentials configured in the ini settings
##############################################################################
@route('/google/credentials/get')
def getCredentials():
    googleLogin = config.get('Google', 'login')
    googlePassword = config.get('Google', 'password')
    
    cred = dict()
    cred.update({'login':googleLogin})
    cred.update({'password':googlePassword})    
    
    global loggedIn
    if(not loggedIn):
        global api
        result = api.login(googleLogin, googlePassword)
        if(result):
            loggedIn = True
            loadGoogleData()
    return cred
    
# Sets the google login and password
##############################################################################
@route('/google/credentials/set', method='POST')
def setCredentials():
    login = request.forms.login
    password = request.forms.password
    
    global api
    global loggedIn
    result = api.login(login, password, perform_upload_auth=False)
    if(result):
        loggedIn = True
        mpc.log('Google login successful')
        loadGoogleData()
        config.set('Google', 'login', login)
        config.set('Google', 'password', password)
    else:
        mpc.log('Google login failed')
        config.set('Google', 'login', login)
        config.set('Google', 'password', '')
        
    with open(googleConfigFile, 'wb') as configfile:
        config.write(configfile)
        
    return str(result)    

# Album list
###############################################################################
@route('/google/albums/list')
def getAlbumList():
	result = dict()
	result.update({'items':albumList})
	return result
	
# Album 
###############################################################################
@route('/google/album/<album>')
def getAlbumList(album):
    tracks = albumStore.get(album).get('tracks')
    player.setPlaylist(tracks)
    return albumStore[album]
	
# Artists list
###############################################################################
@route('/google/artists/list')
def getArtists():
	result = dict()
	result.update({'items':artistList})
	return result	
	
# Artist
###############################################################################
@route('/google/artist/<artist>')
def getArtist(artist):
    artistDict = artistStore[artist]
    player.setPlaylist(artistStore.get(artist).get('tracks'))
    artistDict.update({'artist':artist})
    return artistDict

# Play song
###############################################################################
@route('/google/player/song/<songId>')
def playSong(songId):
    return player.playSong(songId)

# Player actions
###############################################################################
@route('/google/player/<action>')
def playerAction(action):
    if (action == 'current'):
        return player.getCurrentSong()
    elif (action == 'all'):
        return player.playSong(None)
    elif (action == 'stop'):
        return player.stop()
    elif (action == 'next'):
        songId = player.next()
        if(songId is not None):
            player.playSong(songId)
        else:
            player.stop()
    elif (action == 'prev'):
        songId = player.previous()
        if(songId is not None):
            player.playSong(songId)
        else:
            player.stop()
    return str(True)

###############################################################################
# Helper 
###############################################################################
def convertDictToSortedList(dictionary, list):
	keylist = dictionary.keys()
	keylist.sort()
	for key in keylist:
	    list.append(dictionary[key])
	    
def createTrackDict(song):
    title = str(unicode(song.get('title','')).encode('utf-8'))
    id = song['id']
    durationMillis = int(song['durationMillis'])
    duration = time.strftime("%M:%S",time.gmtime(durationMillis/1000))
    trackDict = dict()
    trackDict.update({'name':title})
    trackDict.update({'id':id})
    trackDict.update({'track':song.get('track','0')})
    trackDict.update({'duration':duration})
    return trackDict
	