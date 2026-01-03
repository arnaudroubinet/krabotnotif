package arn.roub.krabot.infrastructure.adapter.out.scraping;

import arn.roub.krabot.domain.model.Kramail;
import arn.roub.krabot.domain.model.KramailId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser HTML pour extraire les données de Kraland.
 */
public class KralandHtmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(KralandHtmlParser.class);

    /**
     * Vérifie si la page contient une notification (report).
     */
    public boolean hasNotification(String html) {
        Document doc = Jsoup.parse(html);
        return doc.select("i[class*=fa fa-bell]").next().get(0).attributes().get("class").equals("badge badge-danger");
    }

    /**
     * Vérifie si la page nécessite une authentification.
     */
    public boolean requiresAuthentication(String html) {
        return html.contains("Identifiant")
                || html.contains("S'identifier")
                || html.contains("c[1]");
    }

    /**
     * Extrait les informations de comptes depuis la sidebar.
     */
    public List<AccountInfo> extractAccounts(String html) {
        Document doc = Jsoup.parse(html);
        List<AccountInfo> accounts = new ArrayList<>();

        Elements accountLinks = doc.select("div[class*=list-group]").select("a[href*=kramail/]");

        for (Element link : accountLinks) {
            String href = link.attr("href");
            if (href.contains("post/")) {
                continue;
            }

            String accountName = link.ownText().trim();
            String fullUrl = href.startsWith("http") ? href : "http://www.kraland.org/" + href;
            String section = href.toLowerCase().contains("joyeux") ? "plateau" : "membre";

            accounts.add(new AccountInfo(accountName, fullUrl, section));
            LOGGER.debug("Found account: {} ({}) at {}", accountName, section, fullUrl);
        }

        return accounts;
    }

    /**
     * Parse les kramails depuis une page de compte.
     */
    public List<Kramail> parseKramails(String html, String section) {
        Document doc = Jsoup.parse(html);
        List<Kramail> kramails = new ArrayList<>();

        Elements h1Elements = doc.select("h1");
        if (h1Elements.isEmpty()) {
            LOGGER.warn("No h1 found, cannot determine recipient");
            return kramails;
        }

        String recipient = h1Elements.first().ownText().trim();

        Elements rows = doc.select("table tbody tr");

        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() < 4) {
                continue;
            }

            // Only process UNREAD kramails (those with <strong>)
            Elements strongLink = cells.get(1).select("strong a");
            if (strongLink.isEmpty()) {
                continue;
            }

            String id = cells.get(0).select("input[type=checkbox]").attr("value");
            String title = cells.get(1).select("span.invisible").text();
            String originator = cells.get(2).select("a").text();

            if (!id.isEmpty() && !title.isEmpty()) {
                kramails.add(new Kramail(new KramailId(id), title, originator, recipient, section));
                LOGGER.debug("Found unread kramail: id={}, title={}, from={}, to={}, section={}",
                        id, title, originator, recipient, section);
            }
        }

        return kramails;
    }

    /**
     * Information d'un compte Kraland.
     */
    public record AccountInfo(String name, String url, String section) {}
}
