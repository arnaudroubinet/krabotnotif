package arn.roub.krabot.infrastructure.adapter.out.scraping;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Optional;

public class KralandCharacteristicsParser {

    /**
     * Extracts the character name from the HTML.
     */
    public Optional<String> extractName(String html) {
        Document doc = Jsoup.parse(html);
        Element container = doc.selectFirst("div.col-md-3.sidebar, #content, .container");
        if (container == null) return Optional.empty();
        String own = container.ownText();
        if (own != null) {
            for (String line : own.split("\\n")) {
                String s = line.trim();
                if (s.length() > 0 && s.matches("[A-Za-zÀ-ÖØ-öø-ÿ].*\\s.*") && s.length() < 100) {
                    return Optional.of(s);
                }
            }
        }
        for (Element child : container.children()) {
            String s = child.ownText().trim();
            if (!s.isEmpty() && s.matches("[A-Za-zÀ-ÖØ-öø-ÿ].*\\s.*") && s.length() < 100) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public Optional<String> extractPlayerId(String html) {
        Document doc = Jsoup.parse(html);
        Elements anchors = doc.select("a[href*=\"/communaute/membres/\"]");
        for (Element a : anchors) {
            String href = a.attr("href");
            if (href == null || href.isEmpty() || href.contains("/edit")) continue;
            var m = href.replaceAll(".*/", "");
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("-(\\\\d+)$").matcher(href);
            if (matcher.find()) return Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }

    public Optional<Integer> extractPP(String html) {
        Document doc = Jsoup.parse(html);
        Elements elems = doc.getAllElements();
        for (Element e : elems) {
            String text = e.text();
            if (text != null && text.matches(".*\\bPP\\b.*")) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("PP\\\\D*(\\\\d+)").matcher(text);
                if (m.find()) return Optional.of(Integer.parseInt(m.group(1)));
            }
        }
        return Optional.empty();
    }
}
