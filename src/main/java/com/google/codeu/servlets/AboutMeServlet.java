package com.google.codeu.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.codeu.data.Datastore;
import com.google.codeu.data.User;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.jsoup.nodes.Document.OutputSettings;

/**
 * Responds with a hard-coded message for testing purposes.
 */
@WebServlet("/about")
public class AboutMeServlet extends HttpServlet{

  private Datastore datastore;

  @Override
  public void init() {
    datastore = new Datastore();
  }

  /**
   * Responds with the "about me " section for a particular user.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("text/html");

    String user = request.getParameter("user");

    if(user == null || user.equals("")) {
      // Request is invalid, return empty response
      return;
    }
    User userData = datastore.getUser(user);

    if(userData == null || userData.getAboutMe() == null || userData.getFirstName() == null || userData.getLastName() == null || userData.getCountry() == null || userData.getCity() == null || userData.getRecommendation() == null) {
      return;
    }

    String aboutMe = userData.getAboutMe();
    String firstName = userData.getFirstName();
    String lastName = userData.getLastName();
    String country = userData.getCountry();
    String city = userData.getCity();
    String recommendation = userData.getRecommendation();

    aboutMe = aboutMe.replace("\n","<br>");
    recommendation = recommendation.replace("\n","<br>");

    response.getOutputStream().println("<h3 style='color: #f88379; font-size: 20px;'>Name</h3>");
    response.getOutputStream().println("<p>"+ firstName + " " + lastName + "</p>");

    response.getOutputStream().println("<h3 style='color: #f88379; font-size: 20px;'>From</h3>");
    response.getOutputStream().println("<p>" + city + ", " + country + "</p>");

    response.getOutputStream().println("<h3 style='color: #f88379; font-size: 20px;'>About me</h3>");
    response.getOutputStream().println("<p>" + aboutMe + "</p>");

    response.getOutputStream().println("<h3 style='color: #f88379; font-size: 20px;'>My recommendation</h3>");
    response.getOutputStream().println("<p>" + recommendation + "</p>");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if(!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String userEmail = userService.getCurrentUser().getEmail();
    String firstName = Jsoup.clean(request.getParameter("firstname"), Whitelist.none());
    String lastName = Jsoup.clean(request.getParameter("lastname"), Whitelist.none());
    String country = Jsoup.clean(request.getParameter("country"), Whitelist.none());
    String city = Jsoup.clean(request.getParameter("city"), Whitelist.none());
    String aboutMe = Jsoup.clean(request.getParameter("about-me"), "", Whitelist.none(), new OutputSettings().prettyPrint(false));
    String recommendation = Jsoup.clean(request.getParameter("recommendation"), "", Whitelist.none(), new OutputSettings().prettyPrint(false));

    User user = new User(userEmail, firstName, lastName, country, city, aboutMe, recommendation);
    datastore.storeUser(user);

    response.sendRedirect("/user-page.html?user="+userEmail);
  }
}
