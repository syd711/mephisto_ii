package de.callete.mephisto2;

import callete.api.Callete;
import callete.api.services.gpio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controls the rotary encoder actions.
 */
public class InputController {
  private final static Logger LOG = LoggerFactory.getLogger(InputController.class);
  public static final String SELECTED_STATION = "selected.station";

  private int stationIndex = 1;
  private StationControl control;
  private Display display;

  private Map<Integer, Integer> station2Streams = new HashMap<>();

  public InputController(StationControl control, Display display) {
    this.control = control;
    this.display = display;

    station2Streams.put(1, 10);
    station2Streams.put(2, 9);
    station2Streams.put(3, 8);
    station2Streams.put(4, 3);
    station2Streams.put(5, 7);
    station2Streams.put(6, 15);
    station2Streams.put(7, 14);
    station2Streams.put(8, 13);
    station2Streams.put(9, 11);
    station2Streams.put(10, 6);
    station2Streams.put(11, 5);
    station2Streams.put(12, 4);
  }

  public void connect() {
    GPIOService gpioService = Callete.getGPIOService();
    int pinA = Callete.getConfiguration().getInt("rotary_encoder_A");
    int pinB = Callete.getConfiguration().getInt("rotary_encoder_B");

    RotaryEncoder rotary = gpioService.connectRotaryEncoder(pinA, pinB, "rotary", RotaryEncoder.ENCODING_MODE.MANUAL);
    rotary.setIgnoreHalfSteps(true);
    rotary.addChangeListener(new RotaryEncoderListener() {
      @Override
      public void rotated(RotaryEncoderEvent event) {
        stationIndex++;

//        else {
//          stationIndex--;
//        }
        if (stationIndex < 0) {
          stationIndex = 12;
        }
        if (stationIndex > 12) {
          stationIndex = 1;
        }

        int led = station2Streams.get(stationIndex);
        display.enableLed(led);
      }
    });

    int pinPush = Callete.getConfiguration().getInt("rotary_button");
    PushButton pushButton = gpioService.connectPushButton(pinPush, "push-button");
    pushButton.addPushListener(new PushListener() {
      @Override
      public void pushed(PushEvent e) {
        if (e.isLongPush()) {
          LOG.info("Detected long push event.");
        }
        else {
          LOG.info("Detected push event");
        }

        playSelection();
      }

      @Override
      public long getPushDebounceMillis() {
        return 10;
      }

      @Override
      public long getLongPushDebounceMillis() {
        return 400;
      }
    });
  }

  public void startPlayback() {
    stationIndex = Callete.getSettings().getInt(SELECTED_STATION, 1);
    LOG.info("Starting playback on position " + stationIndex);
    playSelection();
  }

  public void play(int streamIndex) {
    control.playAt(streamIndex);

    int led = station2Streams.get(stationIndex);
    display.enableLed(led);
    Callete.saveSetting(SELECTED_STATION, streamIndex);
  }

  private void playSelection() {
    control.playAt(stationIndex);
    int led = station2Streams.get(stationIndex);
    display.enableLed(led);
    Callete.saveSetting(SELECTED_STATION, stationIndex);
  }
}
