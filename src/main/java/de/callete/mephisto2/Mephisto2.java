package de.callete.mephisto2;

import callete.api.Callete;
import callete.api.services.network.HotSpot;
import jdk.nashorn.internal.codegen.CompilerConstants;
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
    LOG.info("Connecting Display");
    display = new Display();

    LOG.info("Checking internet connection...");
    if(!Callete.getNetworkService().isOnline()) {
      LOG.info("No internet connection found, running setup wizard...");
      runSetupWizard();
    }
    
    display.setStartupMode();
    LOG.info("Well, internet seems to be working, so start the rest of it...");

    LOG.info("Creating Metadata Cache");
    MetaDataCache.getInstance();

    LOG.info("Creating Station Control");
    control = new StationControl();

    LOG.info("Input Controller");
    inputController = new InputController(control, display);
    inputController.connect();
    inputController.startPlayback();

    Callete.getSystemService().deleteLogs();

    //HTTP Server
    startServer();
  }

  private void runSetupWizard() throws IOException, IllegalAccessException {
    HotSpot hotSpot = Callete.getNetworkService().createHotSpot("Callete", "callete123", "192.168.2.10");
    hotSpot.install();
    hotSpot.start();

    hotSpot.startWLANConfigService(new File("/home/pi/hotspot/"), 8082);
    display.setHotSpotMode();
    
    System.in.read();
    System.exit(0);
  }

  private static void startServer() {
    LOG.info("Starting REST service for UI control");

    //the package that should be scanned for resources, so since this is a resource we add the parent package.
    String[] resourcesLookupPaths = {"de.callete.mephisto2.rest"};

    //finally start the HTTP server
    Callete.getHttpService().startServer(Callete.getConfiguration().getString("deployment.host"), 8080, new File("./ui/"), resourcesLookupPaths);
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
