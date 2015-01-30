package de.callete.mephisto2.rest;

import callete.api.Callete;
import callete.api.services.music.model.Stream;
import de.callete.mephisto2.Mephisto2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/stations")
@Produces(MediaType.APPLICATION_JSON)
public class StationsRestResource {
  private final static Logger LOG = LoggerFactory.getLogger(StationsRestResource.class);

  @GET
  @Path("/list")
  public Stations listStations() {
    return getStationList();
  }

  @GET
  @Path("/reload")
  public Stations reloadStations() {
    return getStationList();
  }

  @GET
  @Path("/edit/{id}")
  public Station editStation(@PathParam("id") String id) {
    int pos = Integer.parseInt(id);
    pos--;
    return getStationList().getStations().get(pos);
  }

  @GET
  @Path("/play/{id}")
  public Station playStation(@PathParam("id") String id) {
    int pos = Integer.parseInt(id);
    List<Stream> streams = Callete.getStreamingService().getStreams();
    Stream stream = streams.get(pos);
    Callete.getMusicPlayer().getPlaylist().clear();
    Callete.getMusicPlayer().getPlaylist().setActiveItem(stream);
    Callete.getMusicPlayer().play();

    Mephisto2.getInstance().getInputController().play((pos+1));
    
    return new Station(stream);
  }

  
  @POST
  @Path("/save/{id}")
  public Station saveStation(@PathParam("id") String id, @FormParam("url") String url) {
    int numericId = Integer.parseInt(id);
    numericId--;
    List<Stream> streams = Callete.getStreamingService().getStreams();
    Stream stream = streams.get(numericId);
    stream.setUrl(url);
    Callete.getStreamingService().saveStreams(streams);
    Mephisto2.getInstance().getStationControl().refresh();
    return new Station(stream);
  }

  @GET
  @Path("/move/{from}/{to}")
  public Station moveStation(@PathParam("from") String from, @PathParam("to") String to) {
    int fromPos = Integer.parseInt(from);
    int toPos= Integer.parseInt(to);
    List<Stream> streams = Callete.getStreamingService().getStreams();
    Stream stream = streams.remove(fromPos);
    streams.add(toPos, stream);
    Callete.getStreamingService().saveStreams(streams);
    Mephisto2.getInstance().getStationControl().refresh();
    return new Station(stream);
  }

  // ------------ Helper -----------------
  private Stations getStationList() {
    LOG.info("Refreshing Station.");
    return new Stations();
  }
}
