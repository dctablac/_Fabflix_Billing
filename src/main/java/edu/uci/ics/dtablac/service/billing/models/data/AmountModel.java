package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AmountModel {
    @JsonProperty(value = "total", required = true)
    private String TOTAL;
    @JsonProperty(value = "currency", required = true)
    private String CURRENCY;

    public AmountModel(@JsonProperty(value = "total", required = true) String newTotal,
                       @JsonProperty(value = "currency", required = true) String newCurrency) {
        this.TOTAL = newTotal;
        this.CURRENCY = newCurrency;
    }

    @JsonProperty(value = "total")
    public String getTOTAL() { return TOTAL; }
    @JsonProperty(value = "currency")
    public String getCURRENCY() { return CURRENCY; }
}
