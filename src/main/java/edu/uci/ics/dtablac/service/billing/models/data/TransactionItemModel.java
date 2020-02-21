package edu.uci.ics.dtablac.service.billing.models.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionItemModel {
    @JsonProperty(value = "email", required = true)
    private String EMAIL;
    @JsonProperty(value = "movie_id", required = true)
    private String MOVIE_ID;
    @JsonProperty(value = "quantity", required = true)
    private Integer QUANTITY;
    @JsonProperty(value = "unit_price", required = true)
    private Float UNIT_PRICE;
    @JsonProperty(value = "discount", required = true)
    private Float DISCOUNT;
    @JsonProperty(value = "sale_date", required = true)
    private String SALE_DATE;

    @JsonCreator
    public TransactionItemModel(@JsonProperty(value = "email", required = true) String newEMAIL,
                                @JsonProperty(value = "movie_id", required = true) String newMOVIE_ID,
                                @JsonProperty(value = "quantity", required = true) Integer newQUANTITY,
                                @JsonProperty(value = "unit_price", required = true) Float newUNIT_PRICE,
                                @JsonProperty(value = "discount", required = true) Float newDISCOUNT,
                                @JsonProperty(value = "sale_date", required = true) String newSALE_DATE) {
        this.EMAIL = newEMAIL;
        this.MOVIE_ID = newMOVIE_ID;
        this.QUANTITY = newQUANTITY;
        this.UNIT_PRICE = newUNIT_PRICE;
        this.DISCOUNT = newDISCOUNT;
        this.SALE_DATE = newSALE_DATE;
    }

    public int compareTo(TransactionItemModel TIM) {
        String compareModel = TIM.getMOVIE_ID();
        return this.getMOVIE_ID().compareTo(compareModel);
    }

    @JsonProperty(value = "email")
    public String getEMAIL() { return EMAIL; }
    @JsonProperty(value = "movie_id")
    public String getMOVIE_ID() { return MOVIE_ID; }
    @JsonProperty(value = "quantity")
    public Integer getQUANTITY() { return QUANTITY; }
    @JsonProperty(value = "unit_price")
    public Float getUNIT_PRICE() { return UNIT_PRICE; }
    @JsonProperty(value = "discount")
    public Float getDISCOUNT() { return DISCOUNT; }
    @JsonProperty(value = "sale_date")
    public String getSALE_DATE() { return SALE_DATE; }
}