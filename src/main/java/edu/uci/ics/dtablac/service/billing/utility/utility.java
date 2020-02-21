package edu.uci.ics.dtablac.service.billing.utility;

import com.braintreepayments.http.HttpResponse;
import com.braintreepayments.http.exceptions.HttpException;
import com.braintreepayments.http.serializer.Json;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.orders.*;
import edu.uci.ics.dtablac.service.billing.BillingService;
import edu.uci.ics.dtablac.service.billing.configs.IdmConfigs;
import edu.uci.ics.dtablac.service.billing.configs.MoviesConfigs;
import edu.uci.ics.dtablac.service.billing.configs.ServiceConfigs;
import edu.uci.ics.dtablac.service.billing.core.CartQuery;
import edu.uci.ics.dtablac.service.billing.core.OrderQuery;
import edu.uci.ics.dtablac.service.billing.logger.ServiceLogger;
import edu.uci.ics.dtablac.service.billing.models.base.*;
import edu.uci.ics.dtablac.service.billing.models.data.*;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class utility {

    // Sends privilege verification to idm
    public static int checkPrivilege(String email, Integer plvl) {
        IdmConfigs idmConfigs = BillingService.getIdmConfigs();
        String servicePath = idmConfigs.getScheme()+idmConfigs.getHostName()+":"+
                             idmConfigs.getPort()+idmConfigs.getPath();
        String endpointPath = idmConfigs.getPrivilegePath();

        PrivilegeRequestModel requestModel = new PrivilegeRequestModel(email, plvl);
        ResponseModel responseModel = null;

        // Create a new client
        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);

        // Create a WebTarget to send a request to
        ServiceLogger.LOGGER.info("Building WebTarget...");
        WebTarget webTarget = client.target(servicePath).path(endpointPath);

        // Create an InvocationBuilder to create the HTTP request (bundle request)
        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        // Send the request to /idm/privilege and save it to a response
        ServiceLogger.LOGGER.info("Sending request...");
        Response response = invocationBuilder.post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));
        ServiceLogger.LOGGER.info("Request sent.");

        ServiceLogger.LOGGER.info("Received status " + response.getStatus());
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonText = response.readEntity(String.class);
            responseModel = mapper.readValue(jsonText, ResponseModel.class);
            ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");
        }
        catch (IOException e) {
            ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
        }

        // Return the resultCode
        int resultCode = responseModel.getRESULTCODE();
        ServiceLogger.LOGGER.info("Privilege resultCode: " + resultCode);
        return resultCode;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Calls 'thumbnail' endpoint to retrieve information on a movie ID
    public static ThumbnailModel[] getThumbnailInformation(Object[] movie_ids) {
        MoviesConfigs moviesConfigs = BillingService.getMoviesConfigs();
        String servicePath = moviesConfigs.getScheme()+moviesConfigs.getHostName()+":"+
                moviesConfigs.getPort()+moviesConfigs.getPath();
        String endpointPath = moviesConfigs.getThumbnailPath();

        ThumbnailRequestModel requestModel = new ThumbnailRequestModel(movie_ids);
        ThumbnailResponseModel responseModel = null;

        // Create a new client
        ServiceLogger.LOGGER.info("Building client...");
        Client client = ClientBuilder.newClient();
        client.register(JacksonFeature.class);

        // Create a WebTarget to send a request to
        ServiceLogger.LOGGER.info("Building WebTarget...");
        WebTarget webTarget = client.target(servicePath).path(endpointPath);

        // Create an InvocationBuilder to create the HTTP request (bundle request)
        ServiceLogger.LOGGER.info("Starting invocation builder...");
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        // Send the request to /movies/thumbnail and save it to a response
        ServiceLogger.LOGGER.info("Sending request...");
        Response response = invocationBuilder.post(Entity.entity(requestModel, MediaType.APPLICATION_JSON));
        ServiceLogger.LOGGER.info("Request sent.");

        ServiceLogger.LOGGER.info("Received status " + response.getStatus());
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonText = response.readEntity(String.class);
            responseModel = mapper.readValue(jsonText, ThumbnailResponseModel.class);
            ServiceLogger.LOGGER.info("Successfully mapped response to POJO.");
            return responseModel.getTHUMBNAILS();
        }
        catch (IOException e) {
            ServiceLogger.LOGGER.warning("Unable to map response to POJO.");
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // To get all the details of a user's cart to place an order.
    public static Object[] getCartInfo(RequestModel requestModel) {
        ArrayList<ItemModel> Items = new ArrayList<ItemModel>();

        // ItemModel fields
        String email;
        String movie_id;
        Integer quantity;
        Float unit_price;
        Float discount;

        CartQuery CQ = new CartQuery();
        ResultSet RS = CQ.sendQuery(CQ.buildRetrieveQuery(requestModel.getEMAIL()));
        try {
            while (RS.next()) {
                email = RS.getString("EMAIL");
                movie_id = RS.getString("MOVIE_ID");
                quantity = RS.getInt("QUANTITY");
                unit_price = RS.getFloat("UNIT_PRICE");
                discount = RS.getFloat("DISCOUNT");
                ItemModel item = new ItemModel(email, unit_price, discount, quantity, movie_id,
                                                "N/A", null,null);
                // Title, backdrop, and poster are not required for what the order needs.
                Items.add(item);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Query failed: Unable to get cart record for order.");
        }
        return Items.toArray();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Helper function to clear the cart from the db, from /order/complete
    public static void clearCart(String token) {
        // Get email from 'transaction' joined with 'sale' table on sale_id and specific token
        OrderQuery OQ = new OrderQuery();
        String email = OQ.sendTokenToEmailQuery(OQ.buildMapTokenToEmailQuery(token));

        // Clear cart associated with email.
        CartQuery CQ = new CartQuery();
        CQ.sendUpdate(CQ.buildDeleteQuery(email, null), "clear");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Verifies that the quantity of a movie to add to cart is greater than zero.
    public static ResponseModel checkQuantity(QuantityRequestModel requestModel) {
        if (requestModel.getQUANTITY() < 0) {
            ServiceLogger.LOGGER.warning("Quantity must be greater than zero.");
            ResponseModel responseModel = new ResponseModel(Result.QUANTITY_INVALID_VALUE);
            return responseModel;
        }
        return null;
    }

    // Checks if there are any requests to add a movie that is already in the cart (unique movie_id).
    // Users must use 'update' to change the quantity instead.
    public static ResponseModel checkDuplicates(QuantityRequestModel requestModel) {
        CartQuery CQ = new CartQuery();
        ResultSet rs = CQ.sendQuery(CQ.buildExistenceQuery(requestModel.getMOVIE_ID(), requestModel.getEMAIL()));
        try {
            if (rs.next()) { // If item exists, cannot insert a new instance of this movie, must use update.
                ResponseModel responseModel = new ResponseModel(Result.DUPLICATE_INSERTION);
                return responseModel;
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to track duplicate.");
        }
        return null;
    }

    // Checks if there is an item in the user's cart, with the corresponding movie_id, to update.
    public static ResponseModel checkExistence(MovieIDRequestModel requestModel) {
        CartQuery CQ = new CartQuery();
        ResultSet rs = CQ.sendQuery(CQ.buildExistenceQuery(requestModel.getMOVIE_ID(), requestModel.getEMAIL()));
        try {
            if (!rs.next()) { // If rs is null, item does not exist in the cart.
                ResponseModel responseModel = new ResponseModel(Result.ITEM_DOES_NOT_EXIST);
                return responseModel;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to track if item exists in the cart.");
        }
        return null;
    }

    // Checks if there is an item in the user's cart, by email
    public static ResponseModel checkCartExistence(RequestModel requestModel) {
        CartQuery CQ = new CartQuery();
        ResultSet rs = CQ.sendQuery(CQ.buildRetrieveQuery(requestModel.getEMAIL()));
        try {
            if (rs.next() == false) { // If rs is null, item does not exist in the cart.
                ResponseModel responseModel = new ResponseModel(Result.ITEM_DOES_NOT_EXIST);
                return responseModel;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to track if item exists in the cart.");
        }
        return null; // return null means no problem, and therefore, no change in responseModel to return.
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Object[] getMovie_Ids(ResultSet rs) {
        ResultSet rs2 = rs;
        ArrayList<String> movie_ids = new ArrayList<String>();
        try {
            while(rs2.next()) {
                movie_ids.add(rs.getString("MOVIE_ID"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to map JSON to POJO.");
        }
        return movie_ids.toArray();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Builds header responses to return in HTTP requests
    public static Response buildHeaderResponse(ResponseModel responseModel,String email,
                                      String session_id, String transaction_id) {
        // Return a response with same headers
        Response.ResponseBuilder builder;
        /*if (responseModel == null) {
            builder = Response.status(Response.Status.BAD_REQUEST);
        }
        else {
            builder = Response.status(Response.Status.OK).entity(responseModel);
        }*/
        builder = Response.status(responseModel.getResult().getStatus()).entity(responseModel);

        // Pass along headers
        builder.header("email", email);
        builder.header("session_id", session_id);
        builder.header("transaction_id", transaction_id);

        // return the response
        return builder.build();
    }

    // Check if request email and header email match
    public static boolean requestEmailMatchesHeader(String requestEmail, String headerEmail) {
        return requestEmail.equals(headerEmail);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void updateCaptureID(String capture_id, String token) {
        OrderQuery OQ = new OrderQuery();
        OQ.sendUpdate(OQ.buildUpdateCaptureIDQuery(capture_id, token), "add a new entry to", "transaction");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///// ***** PAYPAL HELPER METHODS ***** /////

    public static String[] createPayPalOrder(PayPalOrderClient orderClient, String cost) {
        Order order = null;

        // Construct a request object and set desired parameters
        // orderRequest creates a post request to paypal v2/checkout/orders

        OrderRequest orderRequest = new OrderRequest();

        // MUST use this method instead of intent to create capture
        orderRequest.checkoutPaymentIntent("CAPTURE");

        // Create application context // These will be changed with the front end.
        ApplicationContext applicationContext = new
                ApplicationContext().returnUrl("http://localhost:12345/api/billing/order/complete")
                                    .cancelUrl("http://localhost:12345/api/billing/test/hello");
        // ReturnUrl is the location where the user will be redirected after payment completion.
        // CancelUrl is the location a user is redirected after canceling an order.
        // Add Application context to order request.
        orderRequest.applicationContext(applicationContext);

        // Create Purchase Units/Movie purchase list (grabbed from cart)

        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        purchaseUnits.add(new PurchaseUnitRequest().amountWithBreakdown(new
                AmountWithBreakdown().currencyCode("USD").value(cost))); // Replace 10 with sum of cost from cart
        orderRequest.purchaseUnits(purchaseUnits);

        // Create an OrdersCreateRequestObject
        OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);

        // Call API. Requires braintree to call paypal/v2/api/endpoints.
        // To retrieve the order_id, call response.result().id()
        try {
            // Call API with your client and get a response for your call
            HttpResponse<Order> response = orderClient.client.execute(request);

            // If call returns body in response, you can get the de-serialized version by calling
            // result() on the response
            order = response.result();

            // Retrieve order_id (same as token) for table.
            ServiceLogger.LOGGER.info("Order ID: " + order.id());
            order.links().forEach(link -> ServiceLogger.LOGGER.info(link.rel() + " => "
                    + link.method() + ":" + link.href()));

            String[] result = new String[2];
            result[0] = order.id();
            List<LinkDescription> list = order.links();
            result[1] = list.get(1).href();

            return result;
            // Thrown Response Status Code is not 200. This occurs when the order could not be created.
        }
        catch (IOException e) {
            ServiceLogger.LOGGER.warning("*****COULD NOT CREATE ORDER*****");
            if (e instanceof HttpException) {
                HttpException he = (HttpException) e;
                ServiceLogger.LOGGER.warning(he.getMessage());
                he.headers().forEach(x -> ServiceLogger.LOGGER.warning(x + " :" +
                        he.headers().header(x)));
            }
            else {
                ServiceLogger.LOGGER.warning("Something went wrong client-side.");
            }
        }
        return null;
    }

    public static String captureOrder(String orderID, PayPalOrderClient orderClient) {
        Order order;
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderID);

        // The orderID is the order_id/token generated from the orders/create request

        try {
            // Call API with your client and get a response for your call
            HttpResponse<Order> response = orderClient.client.execute(request);

            // If call returns body in response, you can get the de-serialized version
            // by calling result() on the response.
            order = response.result();

            // Retrieve capture_id
            String capture_id = order.purchaseUnits().get(0).payments().captures().get(0).id();
            ServiceLogger.LOGGER.info("Capture ID: " + capture_id);

            order.purchaseUnits().get(0).payments().captures().get(0).links()
                    .forEach(link -> ServiceLogger.LOGGER.info(link.rel() + " => "
                    + link.method() + ":" + link.href()));
            return capture_id;
        }
        catch (IOException e) {
            if (e instanceof HttpException) {
                // Something went wrong server-side
                HttpException he = (HttpException)e;
                ServiceLogger.LOGGER.warning(he.getMessage());
                he.headers().forEach(x -> ServiceLogger.LOGGER.warning(x + " :" +
                        he.headers().header(x)));
            }
            else {
                // Something went wrong client-side
                ServiceLogger.LOGGER.warning("Something went wrong client-side.");
            }
        }
        return null;
    }

    public static Json getOrder(String orderID, PayPalOrderClient orderClient)
        throws IOException {
            OrdersGetRequest request = new OrdersGetRequest(orderID);
            HttpResponse<Order> response = orderClient.client.execute(request);
            return new Json();
            //ServiceLogger.LOGGER.info("Full response body:" +
                   // new Json().serialize(response.result()));
            // System.out.println(new JSONObject(new Json().serailize(
            //    response.result())).toString(4));
    }
}