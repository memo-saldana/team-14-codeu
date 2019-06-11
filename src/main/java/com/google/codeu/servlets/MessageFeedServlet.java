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
import org.jsoup.nodes.Document.OutputSettings;

import java.util.Arrays;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;

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
    String text = Jsoup.clean(request.getParameter("text"), "", Whitelist.none(), new OutputSettings().prettyPrint(false));

    MutableDataSet options = new MutableDataSet();
    // Set the root path for emoji image files
    options.set(EmojiExtension.ROOT_IMAGE_PATH, "https://www.webfx.com/tools/emoji-cheat-sheet/graphics/emojis/");
    // Set which extensions to include
    options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(), EmojiExtension.create()));

		// Process markdown
    Parser parser = Parser.builder(options).build();
		Node document = parser.parse(text);
    HtmlRenderer renderer = HtmlRenderer.builder(options).build();
		String mess = renderer.render(document);

		Message message = new Message(userEmail, mess);
		datastore.storeMessage(message);

		response.sendRedirect("/feed.html");
	}
}
