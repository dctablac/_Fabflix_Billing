package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.dtablac.service.billing.models.base.ResponseModel;

public class ThumbnailResponseModel extends ResponseModel {
    @JsonProperty(value = "thumbnails", required = true)
    private ThumbnailModel[] THUMBNAILS;

    @JsonCreator
    public ThumbnailResponseModel(@JsonProperty(value = "resultCode", required = true) Integer newRESULTCODE,
                                  @JsonProperty(value = "message", required = true) String newMessage,
                                  @JsonProperty(value = "thumbnails", required = true) ThumbnailModel[] newTHUMBNAILS) {
        super(newRESULTCODE, newMessage);
        this.THUMBNAILS = newTHUMBNAILS;
    }

    @JsonProperty(value = "thumbnails")
    public ThumbnailModel[] getTHUMBNAILS() { return THUMBNAILS; }
}
