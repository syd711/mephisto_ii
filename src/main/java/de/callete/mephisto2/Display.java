package de.callete.mephisto2;

import callete.api.Callete;
import callete.api.services.gpio.PinState;
import callete.api.services.gpio.ShiftRegister74hc595;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the logic for controlling the 12 LEDs.
 */
public class Display {
  private final static Logger LOG = LoggerFactory.getLogger(Mephisto2.class);

  private ShiftRegister74hc595 shiftRegister;

  public Display() {
    connectDisplay();
  }

  public void enableLed(int index) {
    LOG.info("Writing states for index " + index);
    shiftRegister.clearRegisters();
    shiftRegister.writeRegisters();
    shiftRegister.setRegisterPin(index, PinState.HIGH);
    shiftRegister.writeRegisters();
  }


  //-------------- Helper ---------------------------------------

  private void connectDisplay() {
    int ser = Callete.getConfiguration().getInt("shift_register_SER");
    int rclk = Callete.getConfiguration().getInt("shift_register_RCLK");
    int srclk = Callete.getConfiguration().getInt("shift_register_SRCLK");

    LOG.info("Connecting shift register");
    shiftRegister = Callete.getGPIOService().connectShiftRegister(2, ser, rclk, srclk, "shift register");
  }
}
