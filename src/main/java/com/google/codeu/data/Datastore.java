/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.codeu.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.*;
import java.lang.String;

/** Provides access to the data stored in Datastore. */
public class Datastore {

  private DatastoreService datastore;

	public Datastore() {
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

  /** Stores the Message in Datastore. */
  public void storeMessage(Message message) {
    Entity messageEntity = new Entity("Message", message.getId().toString());
    messageEntity.setProperty("user", message.getUser());
    messageEntity.setProperty("text", message.getText());
    messageEntity.setProperty("city", message.getCity());
    messageEntity.setProperty("timestamp", message.getTimestamp());
    messageEntity.setProperty("imageUrl", message.getImageUrl());
    datastore.put(messageEntity);
  }

  /**
  * Process messages query
  * @return a list of messages.
  */
  public List<Message> processQuery(Query query) {
    List<Message> messages = new ArrayList<>();

    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      try {
        String idString = entity.getKey().getName();
        UUID id = UUID.fromString(idString);
        String user = (String) entity.getProperty("user");
        String text = (String) entity.getProperty("text");
        String city = (String) entity.getProperty("city");
				String imageUrl = (String)entity.getProperty("imageUrl");
        long timestamp = (long) entity.getProperty("timestamp");

				Message message = new Message(id, user, text, city, imageUrl, timestamp);
					messages.add(message);
			} catch (Exception e) {
				System.err.println("Error reading message.");
				System.err.println(entity.toString());
				e.printStackTrace();
			}
		}
    return messages;
  }

  /**
   * Gets messages posted by a specific user.
   *
   * @return a list of messages posted by the user, or empty list if user has never posted a
   *     message. List is sorted by time descending.
   */
  public List<Message> getMessages(String user) {

    Query query =
    	new Query("Message")
      	.setFilter(new Query.FilterPredicate("user", FilterOperator.EQUAL, user))
        .addSort("timestamp", SortDirection.DESCENDING);

    return processQuery(query);
  }

  /**
   * Gets reviews posted for a specific city.
   *
   * @return a list of messages posted for a city, or empty list if there is no review for that city.
   * List is sorted by time descending.
   */
  public List<Message> getReviewsForCity(String city) {
    Query query =
      new Query("Message")
        .setFilter(new Query.FilterPredicate("city", FilterOperator.EQUAL, city))
        .addSort("timestamp", SortDirection.DESCENDING);

    return processQuery(query);
  }

  /**
   * Gets all messages posted.
   *
   * @return a list of all messages posted, or empty list if no message was posted.
   * List is sorted by time descending.
   */
   public List<Message> getAllMessages() {

     Query query = new Query("Message")
      	.addSort("timestamp", SortDirection.DESCENDING);

    return processQuery(query);
   }

	public Set<String> getUsers(){
		Set<String> users = new HashSet<>();
		Query query = new Query("Message");
		PreparedQuery results = datastore.prepare(query);
		for(Entity entity : results.asIterable()) {
			users.add((String) entity.getProperty("user"));
		}
		return users;
	}

  /**Stores the User in Datastore. */
  public void storeUser(User user) {
    Entity userEntity = new Entity("User", user.getEmail() );
    userEntity.setProperty("email", user.getEmail() );
    userEntity.setProperty("firstName", user.getFirstName() );
    userEntity.setProperty("lastName", user.getLastName() );
    userEntity.setProperty("country", user.getCountry() );
    userEntity.setProperty("city", user.getCity() );
    userEntity.setProperty("aboutMe", user.getAboutMe() );
    userEntity.setProperty("recommendation", user.getRecommendation() );

    datastore.put(userEntity);
  }

  /**
   * Returns the User owned by the email address,
   *  or null if no matching User was found.
   */
  public User getUser(String email) {

    Query query = new Query("User")
      .setFilter(new Query.FilterPredicate("email", FilterOperator.EQUAL, email));
    PreparedQuery results = datastore.prepare(query);
    Entity userEntity = results.asSingleEntity();
    if(userEntity == null) {
      return null;
    }

    String firstName = (String) userEntity.getProperty("firstName");
    String lastName = (String) userEntity.getProperty("lastName");
    String country = (String) userEntity.getProperty("country");
    String city = (String) userEntity.getProperty("city");
    String aboutMe = (String) userEntity.getProperty("aboutMe");
    String recommendation = (String) userEntity.getProperty("recommendation");
    User user = new User(email, firstName, lastName, country, city, aboutMe, recommendation);

    return user;
  }

  public List<Marker> getMarkers() {
    List<Marker> markers = new ArrayList<>();

    Query query = new Query("Marker");
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      double lat = (double) entity.getProperty("lat");
      double lng = (double) entity.getProperty("lng");
      String content = (String) entity.getProperty("content");
      String landmark = (String) entity.getProperty("landmark");
      List<Long> ratings =(List<Long>) entity.getProperty("ratings");
      
      Marker marker = new Marker(lat, lng, content, landmark, ratings);
      markers.add(marker);
    }
    return markers;
  }

  public void storeMarker(Marker marker) {
    Entity markerEntity = new Entity("Marker");
    markerEntity.setProperty("lat", marker.getLat());
    markerEntity.setProperty("lng", marker.getLng());
    markerEntity.setProperty("content", marker.getContent());
    markerEntity.setProperty("landmark", marker.getLandmark());
    markerEntity.setProperty("ratings", marker.getRatings());
    datastore.put(markerEntity);
  }

  public Marker removeMarker(Marker marker) {
    Query query = new Query("Marker")
      .setFilter(CompositeFilterOperator.and(
        new Query.FilterPredicate("lat", FilterOperator.EQUAL, marker.getLat()),
        new Query.FilterPredicate("lng", FilterOperator.EQUAL, marker.getLng()),
        new Query.FilterPredicate("content", FilterOperator.EQUAL, marker.getContent())));
    PreparedQuery results = datastore.prepare(query);
    Entity markerEntity = results.asSingleEntity();

    // if not found, return null
    if(markerEntity == null) {
      return null;
    }
    List<Long> ratings =(List<Long>) markerEntity.getProperty("ratings");
    
    datastore.delete(markerEntity.getKey());

    Marker deletedMarker = new Marker((double) markerEntity.getProperty("lat"), (double) markerEntity.getProperty("lng"),
      (String) markerEntity.getProperty("content"), (String) markerEntity.getProperty("landmark"), ratings );
    return deletedMarker;
  }

  public Marker addLandmarkRating(Double lat, Double lng, String content, int rating) {
    Query query = new Query("Marker")
      .setFilter(CompositeFilterOperator.and(
        new Query.FilterPredicate("lat", FilterOperator.EQUAL, lat),
        new Query.FilterPredicate("lng", FilterOperator.EQUAL, lng),
        new Query.FilterPredicate("content", FilterOperator.EQUAL, content)));
    PreparedQuery results = datastore.prepare(query);
    Entity markerEntity = results.asSingleEntity();
    
    if(markerEntity == null){
      return null;
    } 
    List<Long> ratings =(List<Long>) markerEntity.getProperty("ratings");

    // update ratings array
    Long val = ratings.get(rating-1);
    ratings.set(rating-1, val + 1);
    markerEntity.setProperty("ratings", ratings);
    datastore.put(markerEntity);

    // return new marker data
    Marker marker = new Marker((double) markerEntity.getProperty("lat"), (double) markerEntity.getProperty("lng"),
      (String) markerEntity.getProperty("content"), (String) markerEntity.getProperty("landmark"), ratings );
    
    return marker;

  }

  // Check that all markers have a ratings, array, if not, adds it with 0 ratings
  public void verifyMarkerRatings() {

    Query query = new Query("Marker");
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {

      Object ratingsObj = entity.getProperty("ratings");
      if( ratingsObj == null ){
        System.out.println("NO RATINGS FOUND FOR" + entity.getProperty("content"));
        List<Long> ratings = new ArrayList<>();
        for(int i = 0; i<5; i++){
          ratings.add(0L);
        }
        entity.setProperty("ratings", ratings);
        datastore.put(entity);
      }
    }
  }
  
}
