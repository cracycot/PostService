package org.example.services;

public class MessageBuilder {
    public static String madeMessageAddPointsForUser(Long id, int points) {
        return "add:" + " " + id + " " + points;
    }

    public static String madeMessageRemovePointsForUser(Long id, int points) {
        return "remove:" + " " + id;
    }
}
