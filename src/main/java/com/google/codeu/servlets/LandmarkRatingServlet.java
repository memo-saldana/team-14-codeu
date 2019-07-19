package com.google.codeu.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

import com.google.gson.Gson;
import com.google.codeu.data.Marker;

import com.google.codeu.data.Datastore;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

@WebServlet("/ratings")
public class LandmarkRatingServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /** Accepts a POST request containing a new rating for a landmark. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String latStr = request.getParameter("lat");
    String lngStr = request.getParameter("lng");
    String ratingStr = request.getParameter("rating");
    String resp = "";
    if(latStr == null){
      resp += "Lat is not defined. ";
    }
    if(lngStr == null){
      resp += "Lng is not defined. ";
    }
    if(ratingStr == null){
      resp += "Rating is not defined. ";
    }
    if(request.getParameter("content") == null){
      resp += "Content is not defined. ";
    }
    if(resp.length() > 0){
      response.getOutputStream().println(resp);
      return;  
    }
    double lat = Double.parseDouble(latStr);
    double lng = Double.parseDouble(lngStr);
    int rating = Integer.parseInt(ratingStr);
    if(rating < 1 || rating > 5) {
      resp = "Invalid rating";
      response.getOutputStream().println(resp);
      return;
    }
    String content = Jsoup.clean(request.getParameter("content"), Whitelist.none());

    Marker marker = datastore.addLandmarkRating(lat,lng,content,rating);
    
    
    Gson gson = new Gson();
    String json = gson.toJson(marker);
    response.getOutputStream().println(json);
  }

  
}
