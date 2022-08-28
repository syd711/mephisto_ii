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
//    try {
//      setPin(3);
//      Thread.sleep(2000);
//      setPin(4);
//      Thread.sleep(2000);
//      setPin(5);
//      Thread.sleep(2000);
//      setPin(6);
//      Thread.sleep(2000);
//      setPin(7);
//      Thread.sleep(2000);
//      setPin(8);
//      Thread.sleep(2000);
//      setPin(9);
//      Thread.sleep(2000);
//      setPin(10);
//      Thread.sleep(2000);
//      setPin(11);
//      Thread.sleep(2000);
//      setPin(13);
//      Thread.sleep(2000);
//      setPin(14);
//      Thread.sleep(2000);
//      setPin(15);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
  }

  private void setPin(int pin) {
    LOG.info("Setting pin " + pin);
    shiftRegister.clearRegisters();
    shiftRegister.writeRegisters();
    shiftRegister.setRegisterPin(pin, PinState.HIGH);
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
