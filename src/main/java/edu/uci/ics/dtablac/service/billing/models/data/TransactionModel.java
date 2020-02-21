package edu.uci.ics.dtablac.service.billing.models.data;

import edu.uci.ics.dtablac.service.billing.models.base.ItemModel;

public class TransactionModel {
    private String CAPTURE_ID;
    private String STATE;
    private AmountModel AMOUNT;
    private TransactionFeeModel TRANSACTION_FEE;
    private String CREATE_TIME;
    private String UPDATE_TIME;
    private ItemModel[] ITEMS;

    public TransactionModel() {};

    public String getCAPTURE_ID() {
        return CAPTURE_ID;
    }

    public String getSTATE() {
        return STATE;
    }

    public AmountModel getAMOUNT() {
        return AMOUNT;
    }

    public TransactionFeeModel getTRANSACTION_FEE() {
        return TRANSACTION_FEE;
    }

    public String getCREATE_TIME() {
        return CREATE_TIME;
    }

    public String getUPDATE_TIME() {
        return UPDATE_TIME;
    }

    public ItemModel[] getITEMS() {
        return ITEMS;
    }
}
