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
import java.util.List;
import java.util.Map;

@WebServlet("/markers")
public class MarkerServlet extends HttpServlet {

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /** Responds with a JSON array containing marker data. */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    List<Marker> markers = datastore.getMarkers();
    Gson gson = new Gson();
    String json = gson.toJson(markers);

    response.getOutputStream().println(json);
  }

  /** Accepts a POST request containing a new marker. */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("POST ROUTE");

    String latStr = request.getParameter("lat");
    String lngStr = request.getParameter("lng");
    if(latStr == null){
      String resp = "Lat is not defined";
      response.getOutputStream().println(resp);
    }
    if(lngStr == null){
      String resp = "Lat is not defined";
      response.getOutputStream().println(resp);
    }
    double lat = Double.parseDouble(latStr);
    double lng = Double.parseDouble(lngStr);

    String content = Jsoup.clean(request.getParameter("content"), Whitelist.none());

    String url = getFileUrl(request, "landmark");

    Marker marker = new Marker(lat, lng, content, url);
    datastore.storeMarker(marker);
    
    
    Gson gson = new Gson();
    String json = gson.toJson(marker);
    response.getOutputStream().println(json);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException{
    response.setContentType("application/json");

    String latStr = request.getParameter("lat");
    String lngStr = request.getParameter("lng");
    String content = request.getParameter("content");
    
    if(latStr == null){
      String resp = "Lat is not defined";
      response.getOutputStream().println(resp);
    }
    if(lngStr == null){
      String resp = "Lng is not defined";
      response.getOutputStream().println(resp);
    }
    if(content == null){
      String resp = "Content is not defined";
      response.getOutputStream().println(resp);
    }

    double lat = Double.parseDouble(latStr);
    double lng = Double.parseDouble(lngStr);

    Marker marker = new Marker(lat,lng, content);

    Marker deletedMarker = datastore.removeMarker(marker);
    if(deletedMarker == null){
      String resp = "Marker does not exist.";
      response.getOutputStream().println(resp);
    }
    else {
      Gson gson = new Gson();
      String json = gson.toJson(deletedMarker);
      response.getOutputStream().println(json);
    }
  }
  private String getFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (devserver)
    if(blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    return imagesService.getServingUrl(options);

  }
}