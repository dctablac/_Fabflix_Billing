package edu.uci.ics.dtablac.service.billing.models.data;

public class AmountModel {
    private String TOTAL;
    private String CURRENCY;

    public AmountModel(String newTotal, String newCurrency) {
        this.TOTAL = newTotal;
        this.CURRENCY = newCurrency;
    }

    public String getTOTAL() {
        return TOTAL;
    }

    public String getCURRENCY() {
        return CURRENCY;
    }
}
