package com.google.codeu.servlets;

import java.util.Arrays;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.options.MutableDataSet;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;

public class MarkdownProcessor {
  private static final String EMOJI_PATH = "https://www.webfx.com/tools/emoji-cheat-sheet/graphics/emojis/";

  public static String processMarkdown(String text) {
    MutableDataSet options = new MutableDataSet();
    // Set the root path for emoji image files

    options.set(EmojiExtension.ROOT_IMAGE_PATH, EMOJI_PATH);
    // Set which extensions to include
    options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create(), EmojiExtension.create()));

    // Process markdown
    Parser parser = Parser.builder(options).build();
    Node document = parser.parse(text);
    HtmlRenderer renderer = HtmlRenderer.builder(options).build();
    String mess = renderer.render(document);
    return mess;
  }
}
