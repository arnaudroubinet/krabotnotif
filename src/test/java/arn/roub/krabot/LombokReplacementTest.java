package arn.roub.krabot;

import arn.roub.krabot.scrapper.Kramail;
import arn.roub.krabot.utils.elements.*;
import arn.roub.krabot.utils.DiscordWebhook;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LombokReplacementTest {

    @Test
    void testKramailBuilder() {
        Kramail kramail = new Kramail("123", "Test Title", "Test Originator");

        assertEquals("123", kramail.id());
        assertEquals("Test Title", kramail.title());
        assertEquals("Test Originator", kramail.originator());
    }

    @Test
    void testFieldBuilder() {
        Field field = Field.builder()
                .name("Test Field")
                .value("Test Value")
                .inline(true)
                .build();

        assertEquals("Test Field", field.getName());
        assertEquals("Test Value", field.getValue());
        assertTrue(field.isInline());
    }

    @Test
    void testAuthorBuilder() {
        Author author = Author.builder()
                .name("Test Author")
                .url("http://example.com")
                .iconUrl("http://example.com/icon.png")
                .build();

        assertEquals("Test Author", author.getName());
        assertEquals("http://example.com", author.getUrl());
        assertEquals("http://example.com/icon.png", author.getIconUrl());
    }

    @Test
    void testFieldEqualsAndHashCode() {
        Field field1 = Field.builder()
                .name("Test")
                .value("Value")
                .inline(true)
                .build();

        Field field2 = Field.builder()
                .name("Test")
                .value("Value")
                .inline(true)
                .build();

        assertEquals(field1, field2);
        assertEquals(field1.hashCode(), field2.hashCode());
    }

    @Test
    void testEmbedObjectBuilder() {
        Field field = Field.builder()
                .name("Test Field")
                .value("Test Value")
                .inline(false)
                .build();

        EmbedObject embed = EmbedObject.builder()
                .title("Test Embed")
                .description("Test Description")
                .color(Color.BLUE)
                .field(field)
                .build();

        assertEquals("Test Embed", embed.getTitle());
        assertEquals("Test Description", embed.getDescription());
        assertEquals(Color.BLUE, embed.getColor());
        assertEquals(1, embed.getFields().size());
        assertEquals(field, embed.getFields().get(0));
    }

    @Test
    void testDiscordWebhookSetters() {
        DiscordWebhook webhook = new DiscordWebhook("http://example.com");
        
        webhook.setContent("Test Content");
        webhook.setUsername("Test User");
        webhook.setAvatarUrl("http://example.com/avatar.png");

        // We can't test getters since they don't exist, but we can verify the setters don't throw exceptions
        assertDoesNotThrow(() -> {
            webhook.setContent("New Content");
            webhook.setUsername("New User");
            webhook.setAvatarUrl("http://example.com/new-avatar.png");
        });
    }

    @Test
    void testToStringMethods() {
        Field field = Field.builder()
                .name("Test")
                .value("Value")
                .inline(true)
                .build();

        String toString = field.toString();
        assertTrue(toString.contains("Test"));
        assertTrue(toString.contains("Value"));
        assertTrue(toString.contains("true"));
    }
}