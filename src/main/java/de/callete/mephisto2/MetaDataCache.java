package de.callete.mephisto2;

import callete.api.Callete;
import callete.api.services.music.model.Stream;
import callete.api.services.music.player.PlaylistMetaData;
import callete.api.services.music.player.PlaylistMetaDataChangeListener;
import de.callete.mephisto2.rest.Station;

import java.util.HashMap;
import java.util.Map;

/**
 * The streaming meta data is stored here, we use it for the UI if connected.
 */
public class MetaDataCache implements PlaylistMetaDataChangeListener {
  private static MetaDataCache instance = new MetaDataCache();
  
  private Map<String, CacheItem> cache = new HashMap<>();
  
  private MetaDataCache() {
    Callete.getMusicPlayer().getPlaylist().addMetaDataChangeListener(this);
  }
  
  public static MetaDataCache getInstance() {
    return instance;    
  }
  
  @Override
  public void updateMetaData(PlaylistMetaData metaData) {
    Stream stream = (Stream) metaData.getItem();
    if(!cache.containsKey(stream.getPlaybackUrl())) {
      cache.put(stream.getPlaybackUrl(), new CacheItem());
    }
    
    CacheItem item = cache.get(stream.getPlaybackUrl());
    item.apply(metaData);
  }

  /**
   * Applies available metadata to the station. 
   * @param station the station pojo to update
   */
  public void applyMetaData(Station station) {
    CacheItem item = cache.get(station.stream.getPlaybackUrl());
    if(item != null) {
      station.setTitle(item.artist + " - " + item.title);
      station.setName(item.name);
    }
  }
  
  private class CacheItem {
    private String name;
    private String title;
    private String artist;
    
    public void apply(PlaylistMetaData metaData) {
      this.artist = metaData.getArtist();
      this.title = metaData.getTitle();
      this.name = metaData.getName();      
    }
  }
}
