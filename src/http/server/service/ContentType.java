package http.server.service;

import javafx.util.Pair;
/**
 * ContentType class
 * This service class provide content-type by using the extension
 */
public class ContentType {

    private String extension;

    /**
     * Array of pair of extension => content-type
     */
    private static final Pair<String, String>[] contentTypes = new Pair[]{
            new Pair<>("html", "text/html"),
            new Pair<>("jpg", "image/jpeg"),
            new Pair<>("jpeg", "image/jpeg"),
            new Pair<>("png", "image/png"),
            new Pair<>("webm", "video/webm"),
            new Pair<>("avi", "video/x-msvideo"),
            new Pair<>("mov", "video/quicktime"),
            new Pair<>("pdf", "application/pdf"),
            new Pair<>("mp3", "audio/mpeg"),
    };


    /**
     * ContentType constructor
     * @param extension
     */
    public ContentType(String extension){

        this.extension = extension;
    }

    /**
     * get content-type of extension used to instanciate class
     * @return content-type
     */
    public String getContentType(){
        String contentType = "";
        for (int i=0; i<contentTypes.length; i++){
            if(contentTypes[i].getKey().equals(this.extension))
                contentType = contentTypes[i].getValue();
        }
        return contentType;
    }

}
