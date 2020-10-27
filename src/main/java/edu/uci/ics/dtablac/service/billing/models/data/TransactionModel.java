package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionModel {
    @JsonProperty(value = "capture_id", required = true)
    private String CAPTURE_ID;
    @JsonProperty(value = "state", required = true)
    private String STATE;
    @JsonProperty(value = "amount", required = true)
    private AmountModel AMOUNT;
    @JsonProperty(value = "transaction_fee", required = true)
    private TransactionFeeModel TRANSACTION_FEE;
    @JsonProperty(value = "create_time", required = true)
    private String CREATE_TIME;
    @JsonProperty(value = "update_time", required = true)
    private String UPDATE_TIME;
    @JsonProperty(value = "items", required = true)
    private Object[] ITEMS;

    public TransactionModel(@JsonProperty(value = "capture_id", required = true) String newCAPTURE_ID,
                            @JsonProperty(value = "state", required = true) String newSTATE,
                            @JsonProperty(value = "amount", required = true) AmountModel newAMOUNT,
                            @JsonProperty(value = "transaction_fee", required = true)
                                    TransactionFeeModel newTRANSACTION_FEE,
                            @JsonProperty(value = "create_time", required = true) String newCREATE_TIME,
                            @JsonProperty(value = "update_time", required = true) String newUPDATE_TIME,
                            @JsonProperty(value = "items", required = true) Object[] newITEMS) {
        this.CAPTURE_ID = newCAPTURE_ID;
        this.STATE = newSTATE;
        this.AMOUNT = newAMOUNT;
        this.TRANSACTION_FEE = newTRANSACTION_FEE;
        this.CREATE_TIME = newCREATE_TIME;
        this.UPDATE_TIME = newUPDATE_TIME;
        this.ITEMS = newITEMS;
    }

    @JsonProperty(value = "capture_id")
    public String getCAPTURE_ID() { return CAPTURE_ID; }
    @JsonProperty(value = "state")
    public String getSTATE() { return STATE; }
    @JsonProperty(value = "amount")
    public AmountModel getAMOUNT() { return AMOUNT; }
    @JsonProperty(value = "transaction_fee")
    public TransactionFeeModel getTRANSACTION_FEE() { return TRANSACTION_FEE; }
    @JsonProperty(value = "create_time")
    public String getCREATE_TIME() { return CREATE_TIME; }
    @JsonProperty(value = "update_time")
    public String getUPDATE_TIME() { return UPDATE_TIME; }
    @JsonProperty(value = "items")
    public Object[] getITEMS() { return ITEMS; }
}
