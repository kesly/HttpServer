package http.server.service;

import javafx.util.Pair;

public class HttpCode {

    public static final Pair OK = new Pair(200, "OK");

    public static final Pair BAD_REQUEST = new Pair(400, "Bad Request");

    public static final Pair NOT_FOUND = new Pair(404, "Not Found");

    public static final Pair CREATED = new Pair(201, "Created");
}
