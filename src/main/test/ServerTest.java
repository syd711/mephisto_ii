import callete.api.Callete;
import de.callete.mephisto2.Mephisto2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Test class for developing the UI
 */
public class ServerTest {
  private final static Logger LOG = LoggerFactory.getLogger(Mephisto2.class);
  private static File streamsSettingsFile = new File("./conf/streams.properties");
  
  public static void main(String[] args) throws IOException {
    LOG.info("Starting REST service for UI control");
    Callete.getStreamingService().setConfigFile(streamsSettingsFile);
    
    int serverPort = Callete.getConfiguration().getInt("http.server.port");
    String serverHost = Callete.getConfiguration().getString("deployment.host");
    //the resource directory doesn't really matter here, since we only want to provide a REST service
    File resourceDirectory = new File(Callete.getConfiguration().getString("http.server.resources"));

    //the package that should be scanned for resources, so since this is a resource we add the parent package.
    String[] resourcesLookupPaths = {"de.callete.mephisto2.rest"};

    //finally start the HTTP server
    Callete.getHttpService().startServer(serverHost, serverPort, resourceDirectory, resourcesLookupPaths);
    LOG.info("Http Server started.");
    
    System.in.read();
    
  }
}
