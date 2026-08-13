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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser HTML pour extraire les données de Kraland.
 */
public class KralandHtmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(KralandHtmlParser.class);

    /**
     * Capture le param et le token de l'appel updateAjax('ajax-order', param, token) déclenché
     * par le clic sur le bouton "Dormir" (classe alert11). Le "\.alert11\"" ancre la sélection
     * jQuery exacte pour éviter de capturer alert110/alert111/alert1103/alert1110...
     */
    private static final Pattern SLEEP_AJAX_TRIGGER_PATTERN = Pattern.compile(
            "\\.alert11\"[\\s\\S]*?updateAjax\\(\\s*'ajax-order'\\s*,\\s*'([^']+)'\\s*,\\s*'([^']+)'\\s*\\)"
    );

    /**
     * Vérifie si la page contient une notification (report).
     * Une notification est présente si le badge contient le caractère "!".
     */
    public boolean hasNotification(String html) {
        Document doc = Jsoup.parse(html);
        Elements bellIcons = doc.select("i.fa.fa-bell");

        for (Element bellIcon : bellIcons) {
            Element parent = bellIcon.parent();
            if (parent != null) {
                Elements badges = parent.select("span.badge.badge-danger");
                for (Element badge : badges) {
                    String badgeText = badge.text().trim();
                    LOGGER.debug("Found notification badge with text: '{}'", badgeText);
                    if ("!".equals(badgeText)) {
                        LOGGER.info("Notification detected (badge contains '!')");
                        return true;
                    }
                }
            }
        }

        LOGGER.debug("No notification detected");
        return false;
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

            accounts.add(new AccountInfo(accountName, fullUrl));
            LOGGER.debug("Found account: {} at {}", accountName, fullUrl);
        }

        return accounts;
    }

    /**
     * Parse les kramails depuis une page de compte.
     */
    public List<Kramail> parseKramails(String html) {
        Document doc = Jsoup.parse(html);
        List<Kramail> kramails = new ArrayList<>();

        Elements h1Elements = doc.select("h1");
        if (h1Elements.isEmpty()) {
            LOGGER.warn("No h1 found, cannot determine recipient");
            return kramails;
        }

        String recipient = Objects.requireNonNull(h1Elements.first()).ownText().trim();

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
                kramails.add(new Kramail(new KramailId(id), title, originator, recipient));
                LOGGER.debug("Found unread kramail: id={}, title={}, from={}, to={}",
                        id, title, originator, recipient);
            }
        }

        return kramails;
    }

    /**
     * Vérifie si le bouton "Dormir" est disponible (a la classe btn-primary).
     */
    public boolean isSleepButtonAvailable(String html) {
        Document doc = Jsoup.parse(html);
        Elements sleepLinks = doc.select("a:contains(Dormir)");

        LOGGER.info("Found {} links containing 'Dormir'", sleepLinks.size());

        for (Element link : sleepLinks) {
            String classes = link.attr("class");
            LOGGER.info("Sleep link classes: {}", classes);
            if (link.hasClass("btn-primary")) {
                LOGGER.info("Found sleep button with btn-primary class");
                return true;
            }
        }

        LOGGER.info("Sleep button not available (no btn-primary class found)");
        return false;
    }

    /**
     * Extrait le param et le token de l'appel AJAX déclenché par le clic sur "Dormir".
     * Ce call renvoie le fragment HTML contenant le vrai formulaire de confirmation
     * (avec ses champs anti-triche générés côté serveur).
     */
    public Optional<AjaxOrderTrigger> findSleepAjaxTrigger(String html) {
        Matcher matcher = SLEEP_AJAX_TRIGGER_PATTERN.matcher(html);
        if (matcher.find()) {
            return Optional.of(new AjaxOrderTrigger(matcher.group(1), matcher.group(2)));
        }
        LOGGER.warn("Sleep AJAX trigger not found in plateau page");
        return Optional.empty();
    }

    /**
     * Extrait les champs cachés du formulaire de confirmation d'ordre (post_msg)
     * depuis le fragment HTML renvoyé par l'appel AJAX.
     */
    public Map<String, String> extractOrderFormFields(String html) {
        Document doc = Jsoup.parse(html);
        Element form = doc.selectFirst("form[name=post_msg]");
        Map<String, String> fields = new LinkedHashMap<>();

        if (form == null) {
            LOGGER.warn("post_msg form not found in AJAX order fragment");
            return fields;
        }

        for (Element input : form.select("input[type=hidden]")) {
            fields.put(input.attr("name"), input.attr("value"));
        }

        return fields;
    }

    /**
     * Param et token de l'appel updateAjax('ajax-order', param, token).
     */
    public record AjaxOrderTrigger(String param, String token) {}

    /**
     * Information d'un compte Kraland.
     */
    public record AccountInfo(String name, String url) {}
}
