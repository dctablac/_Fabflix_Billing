package edu.uci.ics.dtablac.service.billing.core;

import edu.uci.ics.dtablac.service.billing.BillingService;
import edu.uci.ics.dtablac.service.billing.logger.ServiceLogger;
import edu.uci.ics.dtablac.service.billing.models.base.ResponseModel;
import edu.uci.ics.dtablac.service.billing.models.base.Result;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CartQuery {
    //// Inserts an item into the cart
    public PreparedStatement buildInsertQuery(String email, String movie_id, Integer quantity) {
        String INSERTINTO = "\nINSERT INTO cart (email, movie_id, quantity)\n";
        String VALUES = "VALUES (?, ?, ?);";

        String query = INSERTINTO + VALUES;

        try {
            PreparedStatement ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1,email);
            ps.setString(2, movie_id);
            ps.setInt(3, quantity);
            return ps;
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query to insert.");
        }
        return null;
    }

    //// Checks for duplicate item insertion
    public PreparedStatement buildExistenceQuery(String movie_id, String email) {
        String SELECT = "\nSELECT *\n";
        String FROM = "FROM cart\n";
        String WHERE = "WHERE email = ? && movie_id = ?;";

        String query = SELECT + FROM + WHERE;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, movie_id);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query to check for duplicates.");
        }
        return ps;
    }

    public PreparedStatement buildUpdateQuery(String email, String movie_id, Integer quantity) {
        String UPDATE = "\nUPDATE cart\n";
        String SET = "SET quantity = ?\n";
        String WHERE = "WHERE email = ? && movie_id = ?;";

        String query = UPDATE + SET + WHERE;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setInt(1, quantity);
            ps.setString(2, email);
            ps.setString(3, movie_id);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query to update the cart.");
        }
        return ps;
    }

    // Used in 'delete' and 'clear' endpoints.
    // If movie_id is null, then 'clear' is executing. Otherwise, 'delete' only a specific movie from cart
    public PreparedStatement buildDeleteQuery(String email, String movie_id) {
        String DELETE = "\nDELETE\n";
        String FROM = "FROM cart\n";
        String WHERE = "WHERE email = ?";

        if (movie_id != null) {
            WHERE += " && movie_id = ?";
        }
        else {
            WHERE += ";";
        }

        String query = DELETE + FROM + WHERE;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
            if (movie_id != null) {
                ps.setString(2, movie_id);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            if (movie_id != null) {
                ServiceLogger.LOGGER.warning("Unable to build query to delete item from the cart.");
            }
            else {
                ServiceLogger.LOGGER.warning("Unable to build query to clear the entire cart.");
            }
        }
        return ps;
    }

    // Use in 'retrieve' endpoint
    // Retrieve email, unit_price, discount, quantity, movie_id
    public PreparedStatement buildRetrieveQuery(String email) {
        String SELECT = "\nSELECT c.email AS EMAIL, mp.unit_price AS UNIT_PRICE, mp.discount AS DISCOUNT,\n" +
                          "       c.quantity AS QUANTITY, c.movie_id AS MOVIE_ID\n";
        String FROM   =   "FROM cart as c\n";
        String JOIN   =   "INNER JOIN movie_price AS mp\n"+
                          "    ON c.movie_id = mp.movie_id\n";
        String WHERE  =   "WHERE c.email = ?;";

        String query = SELECT + FROM + JOIN + WHERE;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query to retrieve the user's cart.");
        }
        return ps;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Sends updates to table 'cart'. Exceptions print based on call made to update the table.
    // String 'call' describes the action that failed to happen with the cart.
    public ResponseModel sendUpdate(PreparedStatement ps, String call) {
        try {
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            int updateCode = ps.executeUpdate();
            if (updateCode > 0) {           // Greater than zero means cart operation succeeds.
                ServiceLogger.LOGGER.info("Query succeeded.");
                if (call.equals("insert into")) {
                    return new ResponseModel(Result.CART_INSERT_SUCCESS);
                }
                if (call.equals("update item in")) {
                    return new ResponseModel(Result.CART_UPDATE_SUCCESS);
                }
                if (call.equals("delete item from")) {
                    return new ResponseModel(Result.ITEM_DELETE_SUCCESS);
                }
                if (call.equals("clear")) {
                    return new ResponseModel(Result.CART_CLEAR_SUCCESS);
                }
            }
            else { // No rows were affected (for 'clear' operation)
                return new ResponseModel(Result.ITEM_DOES_NOT_EXIST);
            }
        } catch (SQLException SQLE) {
            SQLE.printStackTrace();
            ServiceLogger.LOGGER.warning(String.format("Query failed: Unable to %s cart.", call));
        }
        return new ResponseModel(Result.CART_OP_FAIL);  // Cart operation failed.
    }

    // Sends queries to table 'cart' to retrieve records.
    public ResultSet sendQuery(PreparedStatement ps) {
        try {
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            return ps.executeQuery();
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve cart.");
        }
        return null;
    }
}
