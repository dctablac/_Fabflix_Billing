package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThumbnailModel {
    @JsonProperty(value = "movie_id", required = true)
    private String MOVIE_ID;
    @JsonProperty(value = "title", required = true)
    private String TITLE;
    @JsonProperty(value = "backdrop_path")
    private String BACKDROP_PATH;
    @JsonProperty(value = "poster_path")
    private String POSTER_PATH;

    @JsonCreator
    public ThumbnailModel(@JsonProperty(value = "movie_id", required = true) String newMOVIE_ID,
                          @JsonProperty(value = "title", required = true) String newTITLE,
                          @JsonProperty(value = "backdrop_path") String newBACKDROP_PATH,
                          @JsonProperty(value = "poster_path") String newPOSTER_PATH) {
        this.MOVIE_ID = newMOVIE_ID;
        this.TITLE = newTITLE;
        this.BACKDROP_PATH = newBACKDROP_PATH;
        this.POSTER_PATH = newPOSTER_PATH;
    }

    @JsonProperty(value = "movie_id")
    public String getMOVIE_ID() { return MOVIE_ID; }
    @JsonProperty(value = "title")
    public String getTITLE() { return TITLE; }
    @JsonProperty(value = "backdrop_path")
    public String getBACKDROP_PATH() { return BACKDROP_PATH; }
    @JsonProperty(value = "poster_path")
    public String getPOSTER_PATH() { return POSTER_PATH; }
}