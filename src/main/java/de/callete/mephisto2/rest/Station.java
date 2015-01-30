package de.callete.mephisto2.rest;

import callete.api.services.music.model.Stream;
import de.callete.mephisto2.MetaDataCache;

/**
 * Pojo for REST
 */
public class Station {
  private int id;
  private String url;
  private String name;
  private String title;
  public Stream stream;

  public Station(Stream stream) {
    this.url = stream.getPlaybackUrl();
    this.title = "";
    this.id = stream.getId();
    this.stream = stream;
    MetaDataCache.getInstance().applyMetaData(this);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
