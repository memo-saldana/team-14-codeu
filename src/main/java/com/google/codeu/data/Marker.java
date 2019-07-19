package com.google.codeu.data;
import java.util.ArrayList;
import java.util.List;

public class Marker {
  private double lng, lat;
  private String content, landmark;
  private List<Long> ratings;

  public Marker(double lat, double lng, String content, String landmark, List<Long> ratings) {
    this(lat,lng,content,landmark);
    this.ratings = ratings;
  }
  public Marker(double lat, double lng, String content, String landmark) {
    this.lat = lat;
    this.lng = lng;
    this.content = content;
    this.landmark = landmark;
    this.ratings = new ArrayList<>();
    for(int i = 0; i<5; i++){

      this.ratings.add(0L);
    }
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
  public List<Long> getRatings() {
    return ratings;
  }
} 