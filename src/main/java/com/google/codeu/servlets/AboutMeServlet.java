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

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;


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

    if(userData == null || userData.getAboutMe() == null) {
      return;
    }
    response.getOutputStream().println(userData.getAboutMe());
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if(!userService.isUserLoggedIn()) {
      response.sendRedirect("/index.html");
      return;
    }

    String userEmail = userService.getCurrentUser().getEmail();
    String aboutMe = Jsoup.clean(request.getParameter("about-me"), Whitelist.none());

    // Process markdown
    Parser parser = Parser.builder().build();
    Node document = parser.parse(aboutMe);
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    aboutMe = renderer.render(document);

    User user = new User(userEmail, aboutMe);
    datastore.storeUser(user);

    response.sendRedirect("/user-page.html?user="+userEmail);
  }
}
