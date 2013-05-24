import threading
import mpc
import time
from gmusicapi import Api

FALLBACK_PLAYLIST = "radio.pls"

class PlayerUpdate(threading.Thread):
    def __init__(self, player):
        self.__player = player
        self.__name = "Playlist Updater"
        self.__running = True
        threading.Thread.__init__(self)
    
    def setRunning(self, running):
        self.__running = running        
        
    def run(self):
        while(self.__running):
            current = mpc.sysCmd('mpc current')
            if(len(current.strip()) == 0):
                next = self.__player.next()
                if next is None:
                    self.__running = False
                else:
                    url = Player.api.get_stream_url(next)
                    mpc.telnetCmd('clear')
                    mpc.telnetCmd('add ' + str(url))
                    mpc.telnetCmd('play')    
            time.sleep(3)
        mpc.log('Stopping Update Thread')
        
    
class Player(object):
    api = None
    updater = None
    current = None
    playlist = None
    
    def __init__(self, a): 
        Player.api = a
        Player.updater = PlayerUpdate(self)
        
    def getCurrentSong(self):
        return Player.current;
    
    # Applies the active playlist
    ###########################################################################
    def setPlaylist(self, pl):
        self.stop()
        Player.playlist = pl
        
    # Stops the update thread
    ###########################################################################
    def stop(self):
        Player.updater.setRunning(False)
        mpc.telnetCmd('clear')
        mpc.telnetCmd('stop')
        mpc.telnetCmd('load ' + FALLBACK_PLAYLIST)
        return str(True);
        
    # Plays the next songs or terminates the play if list is empty
    ###########################################################################
    def next(self):
        index = 0
        if Player.playlist is None:
            return None
            
        for track in Player.playlist:
            if(track.get('id') != Player.current):
                index+=1
            else:
                break
                
        index+=1            
        mpc.log('Calculated next track index: ' + str(index) + ' of ' + str(len(Player.playlist)))
        if index < len(Player.playlist):
            Player.current = Player.playlist[index].get('id')
        else:
            Player.current = None
        return Player.current
        
    # Plays the previous songs or terminates the play if list is empty
    ###########################################################################
    def previous(self):
        index = 0
        if Player.playlist is None:
            return None
            
        for track in Player.playlist:
            if(track.get('id') != Player.current):
                index+=1;
            else:
                break
                
        index-=1
        mpc.log('Calculated previous track index: ' + str(index) + ' of ' + str(len(Player.playlist)))
        if index >= 0:
            Player.current = Player.playlist[index].get('id')
        else:
            Player.current = Player.playlist[0].get('id')
        return Player.current
        
            
    # Plays the given song from the playlist
    ###########################################################################
    def playSong(self, songId):
        self.stop()
        
        if(songId is None):
            Player.current = Player.playlist[0].get('id');
        else:
            Player.current = songId;
            
        url = Player.api.get_stream_url(Player.current)
        mpc.telnetCmd('clear')
        mpc.telnetCmd('add ' + str(url))
        mpc.telnetCmd('play')
        
        Player.updater = PlayerUpdate(self)
        Player.updater.setRunning(True)
        Player.updater.start()
            
        return Player.current