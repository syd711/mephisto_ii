package de.callete.mephisto2;

import callete.api.Callete;
import callete.api.services.network.HotSpot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * In the beginning, there was main...
 */
public class Mephisto2 {
  private final static Mephisto2 instance = new Mephisto2();
  private final static Logger LOG = LoggerFactory.getLogger(Mephisto2.class);

  private Display display;
  private StationControl control;
  private InputController inputController;

  public static Mephisto2 getInstance() {
    return instance;
  }

  private void init() throws IOException, IllegalAccessException {
    LOG.info("Starting Mephisto II");

    LOG.info("Checking internet connection...");
    if (!Callete.getNetworkService().isOnline()) {
      LOG.info("No internet connection found, exiting.");
      System.exit(0);
    }
    LOG.info("Well, internet seems to be working, so start the rest of it...");

    LOG.info("Creating Metadata Cache");
    MetaDataCache.getInstance();

    LOG.info("Creating Station Control");
    control = new StationControl();

    if(Callete.getSystemService().isLinux()) {
      LOG.info("Connecting Display");
      display = new Display();
      display.setStartupMode();

      LOG.info("Input Controller");
      inputController = new InputController(control, display);
      inputController.connect();
      inputController.startPlayback();
    }

    Callete.getSystemService().deleteLogs();

    //HTTP Server
    startServer();
  }

  private static void startServer() {
    LOG.info("Starting REST service for UI control");

    //the package that should be scanned for resources, so since this is a resource we add the parent package.
    String[] resourcesLookupPaths = {"de.callete.mephisto2.rest"};

    //finally start the HTTP server
    String host = Callete.getConfiguration().getString("deployment.host");
    Callete.getHttpService().startServer(host, 8080, new File("./ui/"), resourcesLookupPaths);
    LOG.info("Http Server started.");
  }

  public StationControl getStationControl() {
    return control;
  }


  public InputController getInputController() {
    return inputController;
  }

  public static void main(String[] args) throws IOException, IllegalAccessException {
    Mephisto2.getInstance().init();
  }
}
