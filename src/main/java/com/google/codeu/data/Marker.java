package com.google.codeu.data;

public class Marker {
  private double lng, lat;
  private String content, landmark;

  public Marker(double lat, double lng, String content, String landmark) {
    this.lat = lat;
    this.lng = lng;
    this.content = content;
    this.landmark = landmark;
  }
  public Marker(double lat, double lng, String content ) {
    this(lat, lng, content, "");
  }

  public Marker(double lat, double lng){
    this(lat, lng, "", "");
  }

  public double getLat() {
    return lat;
  }
  public double getLng() {
    return lng;
  }
  public String getContent() {
    return content;
  }
  public String getLandmark() {
    return landmark;
  }
} 