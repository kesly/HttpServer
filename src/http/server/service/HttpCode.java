package http.server.service;

import javafx.util.Pair;
/**
 * HttpCode class
 * This service class provide some HTTP code with the message
 */
public class HttpCode {

    /**
     * <code>200</code> OK
     */
    public static final Pair OK = new Pair(200, "OK");

    /**
     * <code>204</code> request ok but don't need to leave actual page
     */
    public static final Pair NO_CONTENT = new Pair(200, "No Content");

    /**
     * <code>400</code> Bad request
     */
    public static final Pair BAD_REQUEST = new Pair(400, "Bad Request");

    /**
     * <code>404</code> Resource is not found
     */
    public static final Pair NOT_FOUND = new Pair(404, "Not Found");

    /**
     * <code>201</code> Resource created
     */
    public static final Pair CREATED = new Pair(201, "Created");

    /**
     * <code>403</code> Don't have permision to create/modify resource
     */
    public static final Pair FORBIDEN = new Pair(403, "Forbidden");

    /**
     * <code>403</code> Don't have permision to create/modify resource
     */
    public static final Pair FOUND = new Pair(302, "Found");
}
