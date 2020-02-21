package edu.uci.ics.dtablac.service.billing.models.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.dtablac.service.billing.logger.ServiceLogger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModel {
    @JsonIgnore
    private Result result;
    @JsonProperty(value = "resultCode", required = true)
    private Integer RESULTCODE;
    @JsonProperty(value = "message", required = true)
    private String MESSAGE;

    public ResponseModel() {}

    @JsonCreator
    public ResponseModel(@JsonProperty(value = "resultCode", required = true) Integer newRESULTCODE,
                         @JsonProperty(value = "message", required = true) String newMESSAGE) {
        this.RESULTCODE = newRESULTCODE;
        this.MESSAGE = newMESSAGE;
    }

    //TODO: experiment if it works to add this tag and comment out the
    // original constructor, methods, and variables. @JsonCreator or @JsonIgnoer
    public ResponseModel(Result result) {
        this.result = result;
        this.RESULTCODE = result.getResultCode();
        this.MESSAGE = result.getMessage();
    }

    @JsonProperty(value = "resultCode")
    public Integer getRESULTCODE() { return RESULTCODE; }
    @JsonProperty(value = "message")
    public String getMESSAGE() { return MESSAGE; }

    @JsonIgnore
    public Result getResult() {
        return result;
    }

    @JsonIgnore
    public void setResult(Result result) {
        this.result = result;
    }

    @JsonIgnore
    public Response buildResponse() {
        ServiceLogger.LOGGER.info("Response being made with Result: " + this.result);
        if (result == null || result.getStatus() == Response.Status.INTERNAL_SERVER_ERROR) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(result.getStatus()).entity(this).build();
    }
}
