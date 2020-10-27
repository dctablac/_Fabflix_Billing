package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThumbnailRequestModel {
    @JsonProperty(value = "movie_ids", required = true)
    private Object[] MOVIE_IDS;

    @JsonCreator
    public ThumbnailRequestModel(@JsonProperty(value = "movie_ids", required = true) Object[] newMOVIE_IDS) {
        this.MOVIE_IDS = newMOVIE_IDS;
    }

    @JsonProperty(value = "movie_ids")
    public Object[] getMOVIE_IDS() { return MOVIE_IDS; }
}
