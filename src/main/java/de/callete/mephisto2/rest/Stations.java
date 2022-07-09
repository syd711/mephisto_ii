package de.callete.mephisto2.rest;

import callete.api.Callete;
import callete.api.services.music.model.Stream;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Stations {
  private List<Station> stations = new ArrayList<>();

  public Stations() {
    List<Stream> streams = Callete.getStreamingService().getStreams();
    for (Stream stream : streams) {
      stations.add(new Station(stream));
    }
  }

  public List<Station> getStations() {
    return stations;
  }

  public void setStations(List<Station> stations) {
    this.stations = stations;
  }

  public int getSize() {
    return stations.size();

  }

}
