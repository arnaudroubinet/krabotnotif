package arn.roub.krabot.scrapper;


import lombok.Builder;

@Builder
public record Kramail(String id, String title, String originator) {

}
