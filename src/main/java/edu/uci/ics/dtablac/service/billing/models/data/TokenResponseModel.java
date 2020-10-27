package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.dtablac.service.billing.models.base.ResponseModel;
import edu.uci.ics.dtablac.service.billing.models.base.Result;

public class TokenResponseModel extends ResponseModel {
    @JsonProperty(value = "approve_url")
    private String APPROVE_URL;
    @JsonProperty(value = "token")
    private String TOKEN;

    @JsonCreator
    public TokenResponseModel(Result result,
                              @JsonProperty(value = "approve_url") String newAPPROVE_URL,
                              @JsonProperty(value = "token") String newTOKEN) {
        super(result);
        this.APPROVE_URL = newAPPROVE_URL;
        this.TOKEN = newTOKEN;
    }

    @JsonProperty(value = "approve_url")
    public String getAPPROVE_URL() { return APPROVE_URL; }
    @JsonProperty(value = "token")
    public String getTOKEN() { return TOKEN; }
}
