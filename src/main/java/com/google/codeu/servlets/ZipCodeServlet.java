package com.google.codeu.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Scanner;

/**
 * Returns UFO data as a JSON array, e.g. [{"lat": 38.4404675, "lng": -122.7144313}]
 */
@WebServlet("/zipcode-data")
public class ZipCodeServlet extends HttpServlet {

  private JsonArray zipcodeArray;

  @Override
  public void init() {
    zipcodeArray = new JsonArray();
    Gson gson = new Gson();
    Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/us-zip-codes.csv"));
    while(scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] cells = line.split(",");

      String zip = cells[0];
      String city = cells[1];
      String state = cells[2];
      double lat = Double.parseDouble(cells[3]);
      double lng = Double.parseDouble(cells[4]);

      zipcodeArray.add(gson.toJsonTree(new ZipCodeLocation(zip, city, state, lat, lng)));
    }
    scanner.close();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.getOutputStream().println(zipcodeArray.toString());
  }

  // This class could be its own file if we needed it outside this servlet
  private static class ZipCodeLocation{
    double lat;
    double lng;
    String zip;
    String city;
    String state;

    private ZipCodeLocation(String zip, String city, String state, double lat, double lng) {
      this.lat = lat;
      this.lng = lng;
      this.zip = zip;
      this.city = city;
      this.state = state; 
    }
  }
}