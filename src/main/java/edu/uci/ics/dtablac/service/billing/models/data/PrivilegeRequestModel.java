package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.dtablac.service.billing.models.base.RequestModel;

public class PrivilegeRequestModel extends RequestModel {

    @JsonProperty(value = "plevel", required = true)
    private Integer PLEVEL;

    @JsonCreator
    public PrivilegeRequestModel(@JsonProperty(value = "email", required = true) String newEMAIL,
                                 @JsonProperty(value = "plevel", required = true) Integer newPLEVEL) {
        super(newEMAIL);
        this.PLEVEL = newPLEVEL;
    }

    @JsonProperty(value = "plevel")
    public Integer getPLEVEL() { return PLEVEL; }
}
