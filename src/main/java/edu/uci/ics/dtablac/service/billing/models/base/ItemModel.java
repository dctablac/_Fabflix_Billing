package edu.uci.ics.dtablac.service.billing.models.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemModel {
    @JsonProperty(value = "email", required = true)
    private String EMAIL;
    @JsonProperty(value = "unit_price", required = true)
    private Float UNIT_PRICE;
    @JsonProperty(value = "discount", required = true)
    private Float DISCOUNT;
    @JsonProperty(value = "quantity", required = true)
    private Integer QUANTITY;
    @JsonProperty(value = "movie_id", required = true)
    private String MOVIE_ID;
    @JsonProperty(value = "movie_title", required = true)
    private String MOVIE_TITLE;
    @JsonProperty(value = "backdrop_path")
    private String BACKDROP_PATH;
    @JsonProperty(value = "poster_path")
    private String POSTER_PATH;

    @JsonProperty(value = "sale_date")
    private String SALE_DATE;

    @JsonCreator
    public ItemModel(@JsonProperty(value = "email", required = true) String newEMAIL,
                     @JsonProperty(value = "unit_price", required = true) Float newUNIT_PRICE,
                     @JsonProperty(value = "discount", required = true) Float newDISCOUNT,
                     @JsonProperty(value = "quantity", required = true) Integer newQUANTITY,
                     @JsonProperty(value = "movie_id", required = true) String newMOVIE_ID,
                     @JsonProperty(value = "movie_title", required = true) String newMOVIE_TITLE,
                     @JsonProperty(value = "backdrop_path") String newBACKDROP_PATH,
                     @JsonProperty(value = "poster_path") String newPOSTER_PATH) {
        this.EMAIL = newEMAIL;
        this.UNIT_PRICE = newUNIT_PRICE;
        this.DISCOUNT = newDISCOUNT;
        this.QUANTITY = newQUANTITY;
        this.MOVIE_ID = newMOVIE_ID;
        this.MOVIE_TITLE = newMOVIE_TITLE;
        this.BACKDROP_PATH = newBACKDROP_PATH;
        this.POSTER_PATH = newPOSTER_PATH;
    }

    @JsonProperty(value = "email")
    public String getEMAIL() { return EMAIL; }
    @JsonProperty(value = "unit_price")
    public Float getUNIT_PRICE() { return UNIT_PRICE; }
    @JsonProperty(value = "discount")
    public Float getDISCOUNT() { return DISCOUNT; }
    @JsonProperty(value = "quantity")
    public Integer getQUANTITY() { return QUANTITY; }
    @JsonProperty(value = "movie_id")
    public String getMOVIE_ID() { return MOVIE_ID; }
    @JsonProperty(value = "movie_title")
    public String getMOVIE_TITLE() { return MOVIE_TITLE; }
    @JsonProperty(value = "backdrop_path")
    public String getBACKDROP_PATH() { return BACKDROP_PATH; }
    @JsonProperty(value = "poster_path")
    public String getPOSTER_PATH() { return POSTER_PATH; }

    @JsonProperty(value = "sale_date")
    public String getSALE_DATE() { return SALE_DATE; }
    @JsonProperty(value = "sale_date")
    public void setSALE_DATE(String newSALE_DATE) {
        this.SALE_DATE = newSALE_DATE;
    }


}
