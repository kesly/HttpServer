package http.server.service;

import javafx.util.Pair;

public class ContentType {

    private String extension;
    private static final Pair<String, String>[] contentTypes = new Pair[]{
            new Pair<>("html", "text/html"),
            new Pair<>("jpg", "image/jpeg"),
            new Pair<>("jpeg", "image/jpeg"),
            new Pair<>("png", "image/png"),
            new Pair<>("webm", "video/webm"),
            new Pair<>("avi", "video/x-msvideo"),
            new Pair<>("pdf", "application/pdf"),
    };


    public ContentType(String extension){
        this.extension = extension;
    }


    public String getContentType(){
        String contentType = "";
        for (int i=0; i<contentTypes.length; i++){
            if(contentTypes[i].getKey().equals(this.extension))
                contentType = contentTypes[i].getValue();
        }
        return contentType;
    }

}
