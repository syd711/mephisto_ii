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

  private int stationIndex = 15;
  private StationControl control;
  private Display display;

  private Map<Integer, Integer> station2Streams = new HashMap<>();
  private Map<Integer, Integer> streams2Stations = new HashMap<>();

  public InputController(StationControl control, Display display) {
    this.control = control;
    this.display = display;

    station2Streams.put(15, 1);
    station2Streams.put(14, 2);
    station2Streams.put(13, 3);
    station2Streams.put(12, 4);
    station2Streams.put(11, 5);
    station2Streams.put(10, 6);
    station2Streams.put(9, 7);
    station2Streams.put(8, 8);
    station2Streams.put(7, 9);
    station2Streams.put(6, 10);
    station2Streams.put(5, 11);
    station2Streams.put(4, 12);


    for (Map.Entry<Integer, Integer> entry : station2Streams.entrySet()) {
      streams2Stations.put(entry.getValue(), entry.getKey());
    }
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
        if (event.rotatedLeft()) {
          stationIndex++;
        }
        else {
          stationIndex--;
        }
        if (stationIndex < 4) {
          stationIndex = 15;
        }
        if (stationIndex > 15) {
          stationIndex = 4;
        }
        display.enableLed(stationIndex);
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
    int pos = Callete.getSettings().getInt(SELECTED_STATION, 1);
    LOG.info("Starting playback on position " + pos);
    stationIndex = streams2Stations.get(pos);
    playSelection();
  }

  public void play(int streamIndex) {
    stationIndex = streams2Stations.get(streamIndex);
    control.playAt(streamIndex);
    display.enableLed(stationIndex);
    Callete.saveSetting(SELECTED_STATION, streamIndex);
  }

  private void playSelection() {
    int streamIndex = station2Streams.get(stationIndex);
    control.playAt(streamIndex);
    display.enableLed(stationIndex);
    Callete.saveSetting(SELECTED_STATION, streamIndex);
  }
}
