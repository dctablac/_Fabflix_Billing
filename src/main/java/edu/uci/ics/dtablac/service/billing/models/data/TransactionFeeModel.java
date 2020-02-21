package edu.uci.ics.dtablac.service.billing.models.data;

public class TransactionFeeModel {
    private String VALUE;
    private String CURRENCY;

    public TransactionFeeModel(String newVALUE, String newCURRENCY) {
        this.VALUE = newVALUE;
        this.CURRENCY = newCURRENCY;
    }

    public String getVALUE() {
        return VALUE;
    }

    public String getCURRENCY() {
        return CURRENCY;
    }
}
