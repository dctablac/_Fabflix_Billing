package edu.uci.ics.dtablac.service.billing.models.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QuantityRequestModel extends MovieIDRequestModel{
    @JsonProperty(value = "quantity", required = true)
    private Integer QUANTITY;

    @JsonCreator
    public QuantityRequestModel(@JsonProperty(value = "email", required = true) String newEMAIL,
                                @JsonProperty(value = "movie_id", required = true) String newMOVIE_ID,
                                @JsonProperty(value = "quantity", required = true) Integer newQUANTITY) {
        super(newEMAIL, newMOVIE_ID);
        this.QUANTITY = newQUANTITY;
    }

    @JsonProperty(value = "quantity", required = true)

    public Integer getQUANTITY() { return QUANTITY; }
}
