package de.callete.mephisto2;

import callete.api.Callete;
import callete.api.services.music.model.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Controls the radio stations.
 */
public class StationControl {
  private final static Logger LOG = LoggerFactory.getLogger(StationControl.class);

  private List<Stream> streams;
  private File streamsSettingsFile = new File("./conf/streams.properties");

  public StationControl() {
    Callete.getStreamingService().setConfigFile(streamsSettingsFile);
    streams = Callete.getStreamingService().getStreams();
    Callete.getMusicPlayer().enableMonitoring(false);
  }
  
  public void refresh() {
    streams = Callete.getStreamingService().getStreams();
  }

  public void playAt(int pos) {
    int index = pos-1;
    Stream stream = streams.get(index);
    LOG.info("Starting playback of " + stream);
    Callete.getMusicPlayer().getPlaylist().clear();
    Callete.getMusicPlayer().getPlaylist().setActiveItem(stream);
    Callete.getMusicPlayer().play();
  }
}
