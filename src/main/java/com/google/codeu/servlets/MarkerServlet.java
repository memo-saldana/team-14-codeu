package com.google.codeu.servlets;

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
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    double lat = Double.parseDouble(request.getParameter("lat"));
    double lng = Double.parseDouble(request.getParameter("lng"));
    String content = Jsoup.clean(request.getParameter("content"), Whitelist.none());

    Marker marker = new Marker(lat, lng, content);
    datastore.storeMarker(marker);
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    String latStr = request.getParameter("lat");
    System.out.println(latStr);
    double lat = Double.parseDouble(request.getParameter("lat"));
    double lng = Double.parseDouble(request.getParameter("lng"));

    Marker marker = new Marker(lat,lng);

    datastore.removeMarker(marker);
  }
}