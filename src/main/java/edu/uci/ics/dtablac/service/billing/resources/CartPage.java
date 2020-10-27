package edu.uci.ics.dtablac.service.billing.resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uci.ics.dtablac.service.billing.core.CartQuery;
import edu.uci.ics.dtablac.service.billing.logger.ServiceLogger;
import edu.uci.ics.dtablac.service.billing.models.base.*;
import edu.uci.ics.dtablac.service.billing.models.data.ItemResponseModel;
import edu.uci.ics.dtablac.service.billing.models.data.ThumbnailModel;
import edu.uci.ics.dtablac.service.billing.utility.utility;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("cart")
public class CartPage {

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Inserts unique mapping of customer's email to a movie and quantity into the cart table.
    // Sends a privilege verification request to the IDM to ensure that the customer is a registered user.
    @Path("insert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert(@Context HttpHeaders headers, String jsonText) {
        // Declare RequestModel, ResponseModel, and ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        QuantityRequestModel requestModel;
        ResponseModel responseModel = null;

        // Get request headers
        String EMAIL = headers.getHeaderString("email");
        ServiceLogger.LOGGER.info("This endpoint has email: "+EMAIL);
        String SESSION_ID = headers.getHeaderString("session_id");
        String TRANSACTION_ID = headers.getHeaderString("transaction_id");

        // Attempt to add to cart
        try {
            Response response; // To store results of duplicate insertion and quantity checks
            requestModel = mapper.readValue(jsonText, QuantityRequestModel.class);

            // Check if email header matches request model's email
            if (!utility.requestEmailMatchesHeader(requestModel.getEMAIL(), EMAIL)) {
                responseModel = new ResponseModel(Result.CART_OP_FAIL);
                ServiceLogger.LOGGER.info("Email mismatch");
                ServiceLogger.LOGGER.info("Request model email: "+requestModel.getEMAIL());
                ServiceLogger.LOGGER.info("Header email: "+EMAIL);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Check if movie_id is valid
            if (utility.movieIDInvalid(requestModel.getMOVIE_ID())) {
                responseModel = new ResponseModel(Result.CART_OP_FAIL);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Verify that customer is a registered user (privilege verification request)
            int privilegeRC = utility.checkPrivilege(requestModel.getEMAIL(), 4);
            if (privilegeRC == 14) { // Resultcode for a user not found.
                responseModel = new ResponseModel(Result.USER_NOT_FOUND);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Check if quantity to add to cart is greater than zero.
            responseModel = utility.checkQuantity(requestModel);
            if (responseModel != null) {
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Check if duplicate insertion is requested.
            responseModel = utility.checkDuplicates(requestModel);
            if (responseModel != null) {
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Insert item into cart.
            CartQuery CQ = new CartQuery();
            PreparedStatement query = CQ.buildInsertQuery(requestModel.getEMAIL(), requestModel.getMOVIE_ID(),
                                                     requestModel.getQUANTITY());
            responseModel = CQ.sendUpdate(query, "insert into");
        }
        catch (IOException e) {
            e.printStackTrace();
            if (e instanceof JsonParseException) {
                responseModel = new ResponseModel(Result.JSON_PARSE_EXCEPTION);
            }
            else if (e instanceof JsonMappingException) {
                responseModel = new ResponseModel(Result.JSON_MAPPING_EXCEPTION);
            }
        }
        return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Updates existing mapping of customer's email to a movie and quantity in the cart table.
    @Path("update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@Context HttpHeaders headers, String jsonText) {
        // Headers
        String EMAIL = headers.getHeaderString("email");
        String SESSION_ID = headers.getHeaderString("session_id");
        String TRANSACTION_ID = headers.getHeaderString("transaction_id");

        // Declare models and mapper
        ObjectMapper mapper = new ObjectMapper();
        ResponseModel responseModel = null;
        QuantityRequestModel requestModel;

        // Update quantity of movie in customer's cart
        try {
            requestModel = mapper.readValue(jsonText, QuantityRequestModel.class);

            // Check if email header matches request model's email
            if (!utility.requestEmailMatchesHeader(requestModel.getEMAIL(), EMAIL)) {
                responseModel = new ResponseModel(Result.CART_OP_FAIL);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Check if quantity to add to cart is greater than zero.
            responseModel = utility.checkQuantity(requestModel);
            if (responseModel != null) { // If this checkpoint is null, then the item quantity is greater than zero.
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Check if item exists in the user's cart.
            responseModel = utility.checkExistence(requestModel);
            if (responseModel != null) { // If this checkpoint is null, item exists in the cart.
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Update the cart.
            CartQuery CQ = new CartQuery();
            PreparedStatement query = CQ.buildUpdateQuery(requestModel.getEMAIL(), requestModel.getMOVIE_ID(),
                                                     requestModel.getQUANTITY());
            responseModel = CQ.sendUpdate(query, "update item in");
        }
        catch (IOException e) {
            if (e instanceof JsonMappingException) {
                responseModel = new ResponseModel(Result.JSON_MAPPING_EXCEPTION);
            }
            else if (e instanceof JsonParseException) {
                responseModel = new ResponseModel(Result.JSON_PARSE_EXCEPTION);
            }
        }
        return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Deletes a single customer-movie mapping from the cart table.
    @Path("delete")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@Context HttpHeaders headers, String jsonText) {
        // Headers
        String EMAIL = headers.getHeaderString("email");
        String SESSION_ID = headers.getHeaderString("session_id");
        String TRANSACTION_ID = headers.getHeaderString("transaction_id");


        // Declare models and mapper
        ObjectMapper mapper = new ObjectMapper();
        MovieIDRequestModel requestModel;
        ResponseModel responseModel = null;

        // Deletes an entry from the shopping cart that matches the movie_id
        try {
            Response response;
            requestModel = mapper.readValue(jsonText, MovieIDRequestModel.class);

            // Check if email header matches request model's email
            if (!utility.requestEmailMatchesHeader(requestModel.getEMAIL(), EMAIL)) {
                responseModel = new ResponseModel(Result.CART_OP_FAIL);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Check if item exists in the user's cart.
            responseModel = utility.checkExistence(requestModel);
            if (responseModel != null) { // If this checkpoint is null, item exists in the cart.
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Delete the item from cart.
            CartQuery CQ = new CartQuery();
            PreparedStatement query = CQ.buildDeleteQuery(requestModel.getEMAIL(), requestModel.getMOVIE_ID());
            responseModel = CQ.sendUpdate(query, "delete item from");
        }
        catch (IOException e) {
            if (e instanceof JsonMappingException) {
                responseModel = new ResponseModel(Result.JSON_MAPPING_EXCEPTION);
                return responseModel.buildResponse();
            }
            else if (e instanceof JsonParseException) {
                responseModel = new ResponseModel(Result.JSON_PARSE_EXCEPTION);
                return responseModel.buildResponse();
            }
        }
        return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Retrieves all items and quantities associated with a customer in the cart table.
    // It will also return the associated prices and discount for each item in that customer's cart (from the
    //   movie_prices table).
    // If no records are found, return Case 312.
    // Sends a request to the Movies Service 'thumbnail' endpoint to retrieve movie information.
    @Path("retrieve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieve(@Context HttpHeaders headers, String jsonText) {
        // Headers
        String EMAIL = headers.getHeaderString("email");
        String SESSION_ID = headers.getHeaderString("session_id");
        String TRANSACTION_ID = headers.getHeaderString("transaction_id");


        // Declare models and mapper
        ObjectMapper mapper = new ObjectMapper();
        RequestModel requestModel;
        ItemResponseModel responseModel = null;

        // Declare ArrayList<ItemModel> to store ItemModels (convert to generic array later)
        ArrayList<ItemModel> ITEMS = new ArrayList<ItemModel>();

        // Declare fields to add to ItemModel
        //String Email = requestModel.getEMAIL();
        Float UNIT_PRICE;
        Float DISCOUNT;
        Integer QUANTITY;
        String MOVIE_ID;
        String MOVIE_TITLE;
        String BACKDROP_PATH;
        String POSTER_PATH;

        // Retrieve entire items and quantities associated with a customer in the cart table.
        try {
            Response response;
            requestModel = mapper.readValue(jsonText, RequestModel.class);
            String email_from_request = requestModel.getEMAIL();

            // Check if email header matches request model's email
            if (!utility.requestEmailMatchesHeader(requestModel.getEMAIL(), EMAIL)) {
                responseModel = new ItemResponseModel(Result.CART_OP_FAIL, null);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Query to email, unit_price, discount, quantity, movie_id (movie finances)
            CartQuery CQ = new CartQuery();
            PreparedStatement query = CQ.buildRetrieveQuery(email_from_request);

            ResultSet rs1 = query.executeQuery(); // To retrieve list of movie_ids
            Object[] finalMovie_Ids = utility.getMovie_Ids(rs1);

            ResultSet rs2 = query.executeQuery(); // New ResultSet for movie financial information

            // Query to movie_title, backdrop_path, poster_path
            ThumbnailModel[] thumbnails = utility.getThumbnailInformation(finalMovie_Ids);

            // If movies were not present in the cart, then no thumbnails were mapped.
            if (thumbnails == null) {
                responseModel = new ItemResponseModel(Result.ITEM_DOES_NOT_EXIST, null);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Variables to track thumbnails if movies were present in a user's cart.
            int len = thumbnails.length;
            int j = 0;

            // Build ItemModels and add to list
            while(rs2.next() && (j < len)) {
                ThumbnailModel thumbnail = thumbnails[j];
                UNIT_PRICE = rs2.getFloat("UNIT_PRICE");
                DISCOUNT = rs2.getFloat("DISCOUNT");
                QUANTITY = rs2.getInt("QUANTITY");
                MOVIE_ID = rs2.getString("MOVIE_ID");
                MOVIE_TITLE = thumbnail.getTITLE();
                BACKDROP_PATH = thumbnail.getBACKDROP_PATH();
                POSTER_PATH = thumbnail.getPOSTER_PATH();
                ItemModel item = new ItemModel(email_from_request, UNIT_PRICE, DISCOUNT, QUANTITY, MOVIE_ID,
                                                 MOVIE_TITLE, BACKDROP_PATH, POSTER_PATH);
                ITEMS.add(item);
                j++;
            }
        }
        catch (IOException e) {
            if (e instanceof JsonMappingException) {
                responseModel = new ItemResponseModel(Result.JSON_MAPPING_EXCEPTION, null);
                return responseModel.buildResponse();
            } else if (e instanceof JsonParseException) {
                responseModel = new ItemResponseModel(Result.JSON_PARSE_EXCEPTION, null);
                return responseModel.buildResponse();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to map ItemModels.");
        }
        Object[] finalItems = ITEMS.toArray();
        if (finalItems.length == 0) {
            responseModel = new ItemResponseModel(Result.ITEM_DOES_NOT_EXIST, null);
        }
        else {
            responseModel = new ItemResponseModel(Result.CART_RETRIEVE_SUCCESS, finalItems);
        }
        return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Clears all cart records (mappings) associated with a customer.
    // If no records are deleted, return Case 312.
    @Path("clear")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response clear(@Context HttpHeaders headers, String jsonText) {
        // Headers
        String EMAIL = headers.getHeaderString("email");
        String SESSION_ID = headers.getHeaderString("session_id");
        String TRANSACTION_ID = headers.getHeaderString("transaction_id");

        // Declare models and mapper
        ObjectMapper mapper = new ObjectMapper();
        RequestModel requestModel;
        ResponseModel responseModel = null;

        // Clear the entire cart.
        try {
            requestModel = mapper.readValue(jsonText, RequestModel.class);

            // Check if email header matches request model's email
            if (!utility.requestEmailMatchesHeader(requestModel.getEMAIL(), EMAIL)) {
                responseModel = new ResponseModel(Result.CART_OP_FAIL);
                return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
            }

            // Clearing all movies associated with an email. So movie_id argument is null.
            CartQuery CQ = new CartQuery();
            PreparedStatement query = CQ.buildDeleteQuery(requestModel.getEMAIL(), null);
            responseModel = CQ.sendUpdate(query, "clear");
        }
        catch (IOException e) {
            if (e instanceof JsonMappingException) {
                responseModel = new ResponseModel(Result.JSON_MAPPING_EXCEPTION);
                return responseModel.buildResponse();
            } else if (e instanceof JsonParseException) {
                responseModel = new ResponseModel(Result.JSON_PARSE_EXCEPTION);
                return responseModel.buildResponse();
            }
        }
        return utility.buildHeaderResponse(responseModel, EMAIL, SESSION_ID, TRANSACTION_ID);
    }
}