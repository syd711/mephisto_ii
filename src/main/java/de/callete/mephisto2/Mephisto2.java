package de.callete.mephisto2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In the beginning, there was main...
 */
public class Mephisto2 {
  private final static Logger LOG = LoggerFactory.getLogger(Mephisto2.class);

  public static void main(String[] args) {
    LOG.info("Starting Mephisto II");

    LOG.info("Connecting Display");
    Display display = new Display();

    LOG.info("Creating Station Control");
    StationControl control = new StationControl();

    LOG.info("Input Controller");
    new InputController(control, display).connect();
  }
}
