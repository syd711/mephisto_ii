package de.callete.mephisto2;

import callete.api.Callete;
import callete.api.services.gpio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the rotary encoder actions.
 */
public class InputController {
  private final static Logger LOG = LoggerFactory.getLogger(InputController.class);

  private int stationIndex = 0;
  private StationControl control;
  private Display display;

  public InputController(StationControl control, Display display) {
    this.control = control;
    this.display = display;
  }

  public void connect() {
    GPIOService gpioService = Callete.getGPIOService();
    int pinA = Callete.getConfiguration().getInt("rotary_encoder_A");
    int pinB = Callete.getConfiguration().getInt("rotary_encoder_B");

    RotaryEncoder rotary = gpioService.connectRotaryEncoder(pinA, pinB, "rotary");
    rotary.setIgnoreHalfSteps(true);
    rotary.addChangeListener(new RotaryEncoderListener() {
      @Override
      public void rotated(RotaryEncoderEvent event) {
        if(event.rotatedLeft()) {
          stationIndex--;
        }
        else {
          stationIndex++;
        }
        if(stationIndex < 0) {
          stationIndex = 11;
        }
        if(stationIndex > 11) {
          stationIndex = 0;
        }
      }
    });

    int pinPush = Callete.getConfiguration().getInt("rotary_button");
    PushButton pushButton = gpioService.connectPushButton(pinPush, "push-button");
    pushButton.addPushListener(new PushListener() {
      @Override
      public void pushed(PushEvent e) {
        if(e.isLongPush()) {
          LOG.info("Detected long push event.");
        }
        else {
          LOG.info("Detected push event");
        }

        control.playAt(stationIndex);
        display.enableLed(stationIndex);
      }

      @Override
      public long getPushDebounceMillis() {
        return 10;
      }

      @Override
      public long getLongPushDebounceMillis() {
        return 600;
      }
    });
  }
}
