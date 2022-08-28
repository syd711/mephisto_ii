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
    LOG.debug("Writing LED state for " + index);
    shiftRegister.clearRegisters();
    shiftRegister.writeRegisters();
    shiftRegister.setRegisterPin(index, PinState.HIGH);
    shiftRegister.writeRegisters();
  }

  public void setStartupMode() {
      shiftRegister.clearRegisters();
      shiftRegister.writeRegisters();
      shiftRegister.setRegisterPin(3, PinState.HIGH);
      shiftRegister.setRegisterPin(4, PinState.HIGH);
      shiftRegister.setRegisterPin(5, PinState.HIGH);
      shiftRegister.setRegisterPin(6, PinState.HIGH);
      shiftRegister.setRegisterPin(7, PinState.HIGH);
      shiftRegister.setRegisterPin(8, PinState.HIGH);
      shiftRegister.setRegisterPin(9, PinState.HIGH);
      shiftRegister.setRegisterPin(10, PinState.HIGH);
      shiftRegister.setRegisterPin(11, PinState.HIGH);
      shiftRegister.setRegisterPin(13, PinState.HIGH);
      shiftRegister.setRegisterPin(14, PinState.HIGH);
      shiftRegister.setRegisterPin(15, PinState.HIGH);
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
