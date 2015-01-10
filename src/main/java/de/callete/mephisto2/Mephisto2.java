package de.callete.mephisto2;

import callete.api.Callete;
import callete.api.services.gpio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Sample to show how a rotary encoder works.
 */
public class Mephisto2 {
  private final static Logger LOG = LoggerFactory.getLogger(Mephisto2.class);

  public static void main(String[] args) throws InterruptedException, IOException {
    LOG.info("Started RotaryEncoderWithPushButtonExample");
    
    GPIOService gpioService = Callete.getGPIOService();
    RotaryEncoder rotary = gpioService.connectRotaryEncoder(12, 16, "rotary");
    rotary.setIgnoreHalfSteps(true);
    rotary.addChangeListener(new RotaryEncoderListener() {
      @Override
      public void rotated(RotaryEncoderEvent event) {
        LOG.info("Rotary steps: " + event.getSteps() + ", direction left: " + event.rotatedLeft());
      }
    });

    PushButton pushButton = gpioService.connectPushButton(18, "push-button");
    pushButton.addPushListener(new PushListener() {
      @Override
      public void pushed(PushEvent e) {
        if(e.isLongPush()) {
          LOG.info("Detected long push event.");
        }
        else {
          LOG.info("Detected push event");
        }
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

    System.in.read();
  }
}
