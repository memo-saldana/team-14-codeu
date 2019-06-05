package com.google.codeu.servlets;

import java.io.IOException;
import java.util.List;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.codeu.data.Datastore;
import com.google.codeu.data.Message;
import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;


/**
 * Handles fetching all messages for the public feed.
 */
@WebServlet("/feed")
public class MessageFeedServlet extends HttpServlet{

 private Datastore datastore;

 @Override
 public void init() {
  datastore = new Datastore();
 }

 /**
  * Responds with a JSON representation of Message data for all users.
  */
 @Override
 public void doGet(HttpServletRequest request, HttpServletResponse response)
   throws IOException {

  response.setContentType("application/json");

  List<Message> messages = datastore.getAllMessages();
  Gson gson = new Gson();
  String json = gson.toJson(messages);

  response.getOutputStream().println(json);
 }

 @Override
 public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

   UserService userService = UserServiceFactory.getUserService();
   if (!userService.isUserLoggedIn()) {
     response.sendRedirect("/index.html");
     return;
   }

   String userEmail = userService.getCurrentUser().getEmail();
   String text = Jsoup.clean(request.getParameter("text"), Whitelist.none());

   // Process markdown
   Parser parser = Parser.builder().build();
   Node document = parser.parse(text);
   HtmlRenderer renderer = HtmlRenderer.builder().build();
   String mess = renderer.render(document);

   Message message = new Message(userEmail, mess);
   datastore.storeMessage(message);

   response.sendRedirect("/feed.html");
 }
}
