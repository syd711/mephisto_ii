import RPi.GPIO as GPIO
import threading
import ConfigParser
import mpc
from rotary_encoder import RotaryEncoder2

radioConfigFile = "radio.ini"
config = ConfigParser.RawConfigParser()
config.read(radioConfigFile)

SER_Pin = int(config.get('Radio', 'shift_register_SER')) #23   #pin on the 75HC595
RCLK_Pin = int(config.get('Radio', 'shift_register_RCLK')) #24  #pin on the 75HC595
SRCLK_Pin = int(config.get('Radio', 'shift_register_SRCLK')) #25 #pin on the 75HC595

ROTARY_PIN_A = int(config.get('Radio', 'rotary_encoder_A')) 
ROTARY_PIN_B = int(config.get('Radio', 'rotary_encoder_B')) 
ROTARY_PIN_BUTTON = int(config.get('Radio', 'rotary_button')) 

DEBUG = config.get('Common', 'debug_enabled')


# The ration control thread, including GPIO setup
################################################################################
class RadioControl(threading.Thread):
    
    def __init__(self):
        #How many of the shift registers - change this
        self.number_of_74hc595s = 2
        self.numOfRegisterPins = self.number_of_74hc595s * 8
        self.ledPos = (mpc.getCurrentStation()-1)
        self.lastLedPos = self.ledPos;
        self.debouncing = False
        self.registers = [False, False, False, False, False, False, False, False,False, False, False, False, False, False, False, False]
        
        #here comes the rotary stuff
        self.encoder = RotaryEncoder2(ROTARY_PIN_A, ROTARY_PIN_B)
        
        self.__name = "Radio Control Thread"
        self.__running = True
        self.setup()
        threading.Thread.__init__(self)
        
    # The run method of the thread
    #####################################
    def run(self):
        print "Started Radio Control Thread"
        while(self.__running):
            #read rotary encoder and update led
            delta = self.encoder.get_delta()
            if delta!=0:
                self.updateLedPositionCount(True)
                self.updateLeds()
            #check rotary button
            self.stationButtonPressed()
            
            
                
    # Stop thread on cancel
    ######################################
    def stop(self):
        self.__running = False
        print "Stopped Radio Control Thread"
        
    
    # Debounced read of the push button
    ######################################
    def stationButtonPressed(self):
        stationSelected = GPIO.input(ROTARY_PIN_BUTTON)
        if(stationSelected and self.lastLedPos != self.ledPos):
            self.lastLedPos = self.ledPos
            mpc.loadPlaylist()
            mpc.sysCmd('mpc play ' + str(self.ledPos+1))
            self.updateLeds()
            

    # Setup of the GPIO 
    #####################################
    def setup(self):
        print "Setting up GPIO"
        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)
        
        print "Setting up GPIO for leds"
        GPIO.setup(SER_Pin, GPIO.OUT)
        GPIO.setup(RCLK_Pin, GPIO.OUT)
        GPIO.setup(SRCLK_Pin, GPIO.OUT)
        
        print "Setting up GPIO for rotary encoder"
        GPIO.setup(ROTARY_PIN_BUTTON, GPIO.IN)
        
        #reset all register pins
        self.clearRegisters()
        self.writeRegisters()
        
        #init leds
        self.updateLeds()

    # Set all register pins to LOW
    #####################################
    def clearRegisters(self):
        for i in range(self.numOfRegisterPins):
            self.registers[i] = False;
            
    # Set and display registers
    # Only call AFTER all values are set 
    # how you would like (slow otherwise)
    #####################################
    def writeRegisters(self):
        GPIO.output(RCLK_Pin, False)
        for i in range(self.numOfRegisterPins):
            GPIO.output(SRCLK_Pin, False)
            val = self.registers[i]
            GPIO.output(SER_Pin, val)
            GPIO.output(SRCLK_Pin, True)
        GPIO.output(RCLK_Pin, True)


    # set an individual pin HIGH or LOW
    #####################################
    def setRegisterPin(self, index, value): 
        self.registers[index] = value
      
    
    # Writes the shift register status
    # for the LEDs
    #####################################
    def updateLeds(self):
        for i in range(self.numOfRegisterPins):
            if(i == self.ledPos or i == self.lastLedPos):
                self.setRegisterPin(i, True)
            else:
                self.setRegisterPin(i, False)
          
        self.writeRegisters()  #MUST BE CALLED TO DISPLAY CHANGES
       
    
    # Ensures that the first LED is lid again
    # when the rotary enoder has selected the last LED
    # and vice versa.
    #####################################
    def updateLedPositionCount(self, up):
        if(up):
            self.ledPos+=1
            if(self.ledPos >= 12):
                self.ledPos = 0
        else:
            self.ledPos-=1;
            if(self.ledPos < 0):
                self.ledPos = 12-1