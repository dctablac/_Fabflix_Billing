package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.dtablac.service.billing.models.base.ResponseModel;
import edu.uci.ics.dtablac.service.billing.models.base.Result;

public class TransactionResponseModel extends ResponseModel {
    @JsonProperty(value = "transactions")
    private Object[] TRANSACTIONS;

    @JsonCreator
    public TransactionResponseModel(@JsonProperty(value = "resultCode") Integer newRESULTCODE,
                                    @JsonProperty(value = "message") String newMESSAGE,
                                    @JsonProperty(value = "transactions") Object[] newTransactions) {
        super(newRESULTCODE, newMESSAGE);
        this.TRANSACTIONS = newTransactions;
    }

    @JsonCreator
    public TransactionResponseModel(Result result, @JsonProperty(value = "transactions") Object[] newTransactions) {
        super(result);
        this.TRANSACTIONS = newTransactions;
    }

    @JsonProperty(value = "transactions")
    public Object[] getTRANSACTIONS() { return TRANSACTIONS; }
}
