package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionFeeModel {
    @JsonProperty(value = "value", required = true)
    private String VALUE;
    @JsonProperty(value = "currency", required = true)
    private String CURRENCY;

    public TransactionFeeModel(@JsonProperty(value = "value", required = true) String newVALUE,
                               @JsonProperty(value = "currency", required = true)String newCURRENCY) {
        this.VALUE = newVALUE;
        this.CURRENCY = newCURRENCY;
    }

    @JsonProperty(value = "value", required = true)
    public String getVALUE() { return VALUE; }
    @JsonProperty(value = "currency", required = true)
    public String getCURRENCY() {
        return CURRENCY;
    }
}
