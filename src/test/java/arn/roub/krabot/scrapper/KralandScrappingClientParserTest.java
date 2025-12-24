package arn.roub.krabot.scrapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("KralandScrappingClient parsing tests")
class KralandScrappingClientParserTest {

    @Test
    @DisplayName("Should detect no notification and no kramails")
    void shouldDetectNoNotificationAndNoKramails() {
        var client = new KralandScrappingClient();
        String html = "<html><body>"
                + "<span class='badge badge-danger'></span>"
                + "<span class='badge' id='badge'>0</span>"
                + "</body></html>";

        ScrappingResponse resp = client.parseKramailHtml(html);
        assertFalse(resp.hasNotification());
        assertNotNull(resp.kramails());
        assertEquals(0, resp.kramails().size());
    }

    @Test
    @DisplayName("Should parse kramails list with authors and subjects")
    void shouldParseKramailsList() {
        var client = new KralandScrappingClient();
        String html = "<html><body>"
                + "<span class='badge badge-danger'>1</span>"
                + "<span class='badge' id='badge'>3</span>"
                + "<table>"
                + "<tr><td><a href='kramail/post/test-0-30689687'>test</a></td>"
                + "<td><a href='communaute/membres/rowanne-1-13187'>Rowanne</a></td>"
                + "<td>2025-12-24 10:45:55</td></tr>"
                + "<tr><td><a href='kramail/post/re-1'>Re: One</a></td>"
                + "<td><a href='communaute/membres/jeina-1-19887'>Jeina</a></td>"
                + "<td>10/12 (20:22)</td></tr>"
                + "<tr><td><a href='kramail/post/re-2'>Re: Two</a></td>"
                + "<td><a href='communaute/membres/jeina-1-19887'>Jeina</a></td>"
                + "<td>10/12 (19:47)</td></tr>"
                + "</table></body></html>";

        ScrappingResponse resp = client.parseKramailHtml(html);
        assertTrue(resp.hasNotification());
        List<Kramail> mails = resp.kramails();
        assertEquals(3, mails.size());

        Kramail first = mails.get(0);
        assertEquals("kramail/post/test-0-30689687", first.id());
        assertEquals("test", first.title());
        assertEquals("Rowanne", first.originator());
    }

    @Test
    @DisplayName("Should ignore icon-only anchors")
    void shouldIgnoreIconOnlyAnchors() {
        var client = new KralandScrappingClient();
        String html = "<html><body>"
                + "<span class='badge badge-danger'></span>"
                + "<table>"
                + "<tr><td><a href='kramail/post/nouveau-4-113187'></a></td></tr>"
                + "</table></body></html>";

        ScrappingResponse resp = client.parseKramailHtml(html);
        assertFalse(resp.hasNotification());
        assertEquals(0, resp.kramails().size());
    }
}