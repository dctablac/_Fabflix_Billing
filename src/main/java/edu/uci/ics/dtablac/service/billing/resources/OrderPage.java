package edu.uci.ics.dtablac.service.billing.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.orders.Order;
import edu.uci.ics.dtablac.service.billing.core.OrderQuery;
import edu.uci.ics.dtablac.service.billing.logger.ServiceLogger;
import edu.uci.ics.dtablac.service.billing.models.base.ItemModel;
import edu.uci.ics.dtablac.service.billing.models.base.RequestModel;
import edu.uci.ics.dtablac.service.billing.models.base.ResponseModel;
import edu.uci.ics.dtablac.service.billing.models.base.Result;
import edu.uci.ics.dtablac.service.billing.models.data.*;
import edu.uci.ics.dtablac.service.billing.utility.utility;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;


@Path("order")
public class OrderPage {

    // Creates an order to Paypal. Inserts information regarding the sale of a movie into the sale table.
    // Also inserts a record of the transaction mapping the sale_id and token into the transaction table.
    // Return case 312 if no records are found.
    @Path("place")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response order(@Context HttpHeaders headers, String jsonText) {
        // Header fields
        String EMAIL = headers.getHeaderString("email");
        String SESSION_ID = headers.getHeaderString("session_id");
        String TRANSACTION_ID = headers.getHeaderString("transaction_id");

        // Declare mapper and models
        ObjectMapper mapper = new ObjectMapper();
        TokenResponseModel responseModel = null;
        RequestModel requestModel;

        String APPROVE_URL = null;
        String TOKEN = null;

        try {
            requestModel = mapper.readValue(jsonText, RequestModel.class);

            // Check if email header matches request model's email
            if (!utility.requestEmailMatchesHeader(requestModel.getEMAIL(), EMAIL)) {
                responseModel = new TokenResponseModel(
                        Result.ORDER_CREATION_FAIL, null, null);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Check if user has a cart that exists
            ResponseModel tempResponseModel = utility.checkCartExistence(requestModel);
            if (tempResponseModel != null) { // If checker has something to say, eg. no cart exists.
                responseModel = new TokenResponseModel(
                        Result.ITEM_DOES_NOT_EXIST, null, null);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Get items to send cost of in createPayPalOrder
            Object[] items = utility.getCartInfo(requestModel); // Uses a query to cart (joined with movie_price)
            int item_count = items.length;

            // Keep track of sale ids for transaction entry
            int[] sale_ids = new int[item_count];

            double total_cost = 0.0;

            // Stores temp calculations for discount
            double discountCost = 0.0;
            double tempCost = 0.0;

            // Update total_cost, as well as add to sale and transaction tables
            OrderQuery OQ = new OrderQuery();
            Date today = new Date(System.currentTimeMillis());
            int sale_id = -1;

            for (int i = 0; i < item_count; i++) {
                ItemModel item = (ItemModel) items[i];
                item.setSALE_DATE(today.toString());

                // Calculate true cost for an item, including discounts and multiple copies.
                discountCost = item.getDISCOUNT()*item.getUNIT_PRICE();
                tempCost = item.getQUANTITY() * discountCost;
                total_cost += tempCost;

                // Make a new entry to 'sale'
                OQ.sendUpdate(OQ.buildSaleQuery(item.getEMAIL(), item.getMOVIE_ID(),
                        item.getQUANTITY(), Date.valueOf(item.getSALE_DATE())), "add a new entry", "sale");

                // Get sale_id from new 'sale' table entry
                sale_id = OQ.sendSaleIDQuery(OQ.buildSaleIDQuery(item.getEMAIL(), item.getMOVIE_ID()));
                sale_ids[i] = sale_id;
            }

            // Round cost to two decimal places (passes this into
            DecimalFormat df = new DecimalFormat("###.##");

            // Create order for whole cart
            PayPalOrderClient orderClient = new PayPalOrderClient();
            String[] result = utility.createPayPalOrder(orderClient, df.format(total_cost)); // TODO: check this later

            TOKEN = result[0];
            APPROVE_URL = result[1];

            for (int i = 0; i < item_count; i++) {
                // Make a new entry to 'transaction'
                OQ.sendUpdate(OQ.buildTransactionQuery(sale_ids[i], TOKEN), "add new entry to", "transaction");
            }

            responseModel = new TokenResponseModel(Result.ORDER_PLACE_SUCCESS, APPROVE_URL, TOKEN);

            // Get Order
            utility.getOrder(TOKEN, orderClient);
        }
        catch (IOException e) {
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("JSON PARSE EXCEPTION.");
                responseModel = new TokenResponseModel(
                        Result.JSON_PARSE_EXCEPTION, null, null);
            }
            else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("JSON MAPPING EXCEPTION.");
                responseModel = new TokenResponseModel(
                        Result.JSON_MAPPING_EXCEPTION, null, null);
            }
        }
        return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
    }

    // Retrieves customer's billing history.
    @Path("retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieve(@Context HttpHeaders headers, String jsonText) {
        // Header fields
        String EMAIL = headers.getHeaderString("email");
        String SESSION_ID = headers.getHeaderString("session_id");
        String TRANSACTION_ID = headers.getHeaderString("transaction_id");

        // Declare mapper and models
        ObjectMapper mapper = new ObjectMapper();
        RequestModel requestModel;
        TransactionResponseModel responseModel = null;

        // Transaction list for responseModel
        ArrayList<TransactionModel> transactions = new ArrayList<TransactionModel>();

        // Get order and information about all of a user's transactions
        try {
            requestModel = mapper.readValue(jsonText, RequestModel.class);

            // Check if email header matches request model's email
            if (!utility.requestEmailMatchesHeader(requestModel.getEMAIL(), EMAIL)) {
                responseModel = new TransactionResponseModel(
                        Result.ORDER_HISTORY_DNE, null);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Establish order client to request history of orders by their capture_id
            PayPalOrderClient orderClient = new PayPalOrderClient();

            // Store capture_ids to iterate through and grab information for
            ArrayList<String> order_ids = new ArrayList<String>();

            OrderQuery OQ = new OrderQuery();
            ResultSet RS = OQ.sendEmailToTokenQuery(OQ.buildEmailToTokenQuery(requestModel.getEMAIL()));
            while (RS.next()) {
                order_ids.add(RS.getString("token"));
            }

            // Iterable array of capture_ids
            String[] iterOrderId = new String[order_ids.size()];
            for ( int j = 0; j < order_ids.size(); j++) {
                iterOrderId[j] = order_ids.get(j);
            }
            int order_id_count = iterOrderId.length;
            ServiceLogger.LOGGER.warning("here.");

            // Declare fields for each TransactionModel
            AmountModel AMOUNT;
            String AMOUNT_TOTAL;
            String AMOUNT_CURRENCY;

            TransactionFeeModel TRANSACTION_FEE;
            String TRANSACTION_VALUE;
            String TRANSACTION_CURRENCY;

            Object[] ITEMS;

            String CAPTURE_ID;
            String STATE;
            String CREATE_TIME;
            String UPDATE_TIME;

            ServiceLogger.LOGGER.warning("here 2.");
            // Iterate through orders and parse information
            Order order;
            for (int i = 0; i < order_id_count; i++) {
                order = utility.getOrder(iterOrderId[i], orderClient);
                if (order == null) {
                    continue;
                }
                CAPTURE_ID = order.id();
                STATE = order.status().toLowerCase();
                System.err.println(order.purchaseUnits().get(0).payments());
                AMOUNT_TOTAL = order.purchaseUnits().get(0).payments().captures().get(0).amount().value();
                AMOUNT_CURRENCY = order.purchaseUnits().get(0).payments().captures().get(0).amount().currencyCode();
                AMOUNT = new AmountModel(AMOUNT_TOTAL, AMOUNT_CURRENCY);

                TRANSACTION_VALUE = order.purchaseUnits().get(0).payments().captures().
                                        get(0).sellerReceivableBreakdown().paypalFee().value();
                TRANSACTION_CURRENCY = order.purchaseUnits().get(0).payments().captures().
                                        get(0).sellerReceivableBreakdown().paypalFee().currencyCode();
                TRANSACTION_FEE = new TransactionFeeModel(TRANSACTION_VALUE, TRANSACTION_CURRENCY);

                CREATE_TIME = order.purchaseUnits().get(0).payments().captures().get(0).createTime();
                UPDATE_TIME = order.purchaseUnits().get(0).payments().captures().get(0).updateTime();

                // Get items of this order id
                ITEMS = utility.getTransactionCartInfo(requestModel, iterOrderId[i]);

                TransactionModel transaction = new TransactionModel(CAPTURE_ID, STATE, AMOUNT, TRANSACTION_FEE,
                                                                    CREATE_TIME, UPDATE_TIME, ITEMS);
                transactions.add(transaction);
            }
        }
        catch (IOException e) {
            if (e instanceof JsonParseException) {
                ServiceLogger.LOGGER.warning("JSON Parse Exception.");
                responseModel = new TransactionResponseModel(Result.JSON_PARSE_EXCEPTION, null);
            }
            else if (e instanceof JsonMappingException) {
                ServiceLogger.LOGGER.warning("JSON Mapping Exception.");
                responseModel = new TransactionResponseModel(Result.JSON_MAPPING_EXCEPTION, null);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Error checking order_ids");
        }
        Object[] finalTransactions = transactions.toArray();
        ServiceLogger.LOGGER.warning(finalTransactions.toString());
        responseModel = new TransactionResponseModel(Result.ORDER_RETRIEVE_SUCCESS, finalTransactions);
        return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
    }


    // Executes a payment created in /api/billing/order/place.
    // Updates the mapping records of given 'token' to add the 'capture_id' into the transactions table.
    // Clears the customer's cart after entire transaction is completed.
    @Path("complete")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response complete(@QueryParam("token") String TOKEN,
                             @QueryParam("PayerID") String PAYER_ID) {
        // Declare models
        ResponseModel responseModel;

        try {
            // Check if token not found
            if (TOKEN == null) {
                responseModel = new ResponseModel(Result.TOKEN_NOT_FOUND);
                return responseModel.buildResponse();
            }
            PayPalOrderClient orderClient = new PayPalOrderClient();

            // Capture order and update the transaction record
            String capture_id = utility.captureOrder(TOKEN, orderClient);
            if (capture_id == null) {
                throw new Exception();
            }

            utility.updateCaptureID(capture_id, TOKEN);

            // Clear customer's cart
            utility.clearCart(TOKEN);
        }
        catch (Exception e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Order already captured. Order can not be completed.");
            responseModel = new ResponseModel(Result.ORDER_INCOMPLETE);
            return responseModel.buildResponse();
        }
        // Order complete
        responseModel = new ResponseModel(Result.ORDER_COMPLETE);
        return responseModel.buildResponse();
    }

}
