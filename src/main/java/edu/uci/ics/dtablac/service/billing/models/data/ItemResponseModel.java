package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.dtablac.service.billing.models.base.ItemModel;
import edu.uci.ics.dtablac.service.billing.models.base.ResponseModel;
import edu.uci.ics.dtablac.service.billing.models.base.Result;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemResponseModel extends ResponseModel {
    @JsonProperty(value = "items")
    private Object[] ITEMS;

    @JsonCreator
    public ItemResponseModel(@JsonProperty(value = "resultCode", required = true) Integer newRESULTCODE,
                             @JsonProperty(value = "message", required = true) String newMESSAGE,
                             @JsonProperty(value = "items") Object[] newITEMS) {
        super(newRESULTCODE, newMESSAGE);
        this.ITEMS = newITEMS;
    }

    @JsonCreator
    public ItemResponseModel(Result result, @JsonProperty(value = "items") Object[] newITEMS) {
        super(result);
        this.ITEMS = newITEMS;
    }

    @JsonProperty(value = "items")
    public Object[] getITEMS() { return ITEMS; }
}
