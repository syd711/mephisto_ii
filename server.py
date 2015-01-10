import radio_rest
import atexit
import mpc
import gmusic_rest
import threading
import ConfigParser
from bottle import route, run
from radio_control import RadioControl

radioConfigFile = "radio.ini"
config = ConfigParser.RawConfigParser()
config.read(radioConfigFile)

# Server Settings
SETTING_SERVER_IP = config.get('HttpServer', 'server_host')
SETTING_SERVER_PORT = int(config.get('HttpServer', 'server_port'))

# Start mpc/mpd
mpc.loadPlaylist()
mpc.sysCmd('mpc play')

# Radio Control
control = RadioControl()
control.setDaemon(True)
control.start()

# Exit listener for the player
def goodbye():
    print "Terminating Radio Server"
    print "Stopping GMusic REST Player"
    gmusic_rest.exit()
    print "Stopping Radio Control"
    control.stop()
    
atexit.register(goodbye)

# Run!
###############################################################################
#run(host=SETTING_SERVER_IP, port=SETTING_SERVER_PORT, debug=True)
run(host=SETTING_SERVER_IP, port=SETTING_SERVER_PORT, debug=False, server='cherrypy')
#run(reloader=True)