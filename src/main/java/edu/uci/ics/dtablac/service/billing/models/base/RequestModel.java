package edu.uci.ics.dtablac.service.billing.models.base;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestModel {
    @JsonProperty(value = "email", required = true)
    private String EMAIL;

    @JsonCreator
    public RequestModel(@JsonProperty(value = "email", required = true) String newEMAIL) {
        this.EMAIL = newEMAIL;
    }

    @JsonProperty(value = "email", required = true)
    public String getEMAIL() { return EMAIL; }
}
