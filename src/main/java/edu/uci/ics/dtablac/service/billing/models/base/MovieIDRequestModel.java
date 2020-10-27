package edu.uci.ics.dtablac.service.billing.models.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieIDRequestModel extends RequestModel {
    @JsonProperty(value = "movie_id", required = true)
    private String MOVIE_ID;

    @JsonCreator
    public MovieIDRequestModel(@JsonProperty(value = "email", required = true) String newEMAIL,
                               @JsonProperty(value = "movie_id", required = true) String newMOVIE_ID) {
        super(newEMAIL);
        this.MOVIE_ID = newMOVIE_ID;
    }

    @JsonProperty(value = "movie_id", required = true)
    public String getMOVIE_ID() { return MOVIE_ID; }
}
