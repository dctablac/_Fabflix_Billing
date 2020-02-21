package edu.uci.ics.dtablac.service.billing.core;

import edu.uci.ics.dtablac.service.billing.BillingService;
import edu.uci.ics.dtablac.service.billing.logger.ServiceLogger;
import edu.uci.ics.dtablac.service.billing.models.base.ResponseModel;
import edu.uci.ics.dtablac.service.billing.models.base.Result;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderQuery {
    /// Queries and updates to 'sale' and 'transaction' tables

    // Inserts a new entry into the sale table
    public PreparedStatement buildSaleQuery(String email, String movie_id, Integer quantity, Date sale_date) {
        String INSERTINTO = "\nINSERT INTO sale(email, movie_id, quantity, sale_date)\n";
        String VALUES = "VALUES (?, ?, ?, ?);";

        String query = INSERTINTO + VALUES;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, movie_id);
            ps.setInt(3, quantity);
            ps.setDate(4, sale_date);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query that marks a sale.");
        }

        return ps;
    }

    // Gets the sale_id to reference a new entry into the transaction table
    public PreparedStatement buildSaleIDQuery(String email, String movie_id) {
        String SELECT = "\nSELECT MAX(sale_id) AS sale_id\n";
        String FROM = "FROM sale\n";
        String WHERE = "WHERE email=? && movie_id=?;";

        String query = SELECT + FROM + WHERE;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, movie_id);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query to get sale_id of a sale.");
        }
        return ps;
    }

    public PreparedStatement buildTransactionQuery(int sale_id, String token) {
        String INSERTINTO = "\nINSERT INTO transaction(sale_id, token)\n";
        String VALUES = "VALUES (?, ?);";

        String query = INSERTINTO + VALUES;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setInt(1, sale_id);
            ps.setString(2, token);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query to make a new transaction entry.");
        }
        return ps;
    }

    // Updates the capture_id in a 'transaction' entry.
    public PreparedStatement buildUpdateCaptureIDQuery(String capture_id, String token) {
        String UPDATE = "\nUPDATE transaction\n";
        String SET = "SET capture_id = ?\n";
        String WHERE = "WHERE token = ?;";

        String query = UPDATE + SET + WHERE;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, capture_id);
            ps.setString(2, token);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query to update a capture_id.");
        }
        return ps;
    }

    public PreparedStatement buildMapTokenToEmailQuery(String token) {
        String SELECT = "\nSELECT DISTINCT email\n";
        String FROM   = "FROM transaction as t\n";
        String JOIN   = "INNER JOIN sale as s\n" +
                        "    ON s.sale_id = t.sale_id\n";
        String WHERE  = "WHERE token = ?;";

        String query = SELECT + FROM + JOIN + WHERE;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, token);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query to map transaction to email in sale table.");
        }
        return ps;
    }

    public PreparedStatement buildEmailToCaptureIDQuery(String email) {
        String SELECT = "\nSELECT DISTINCT t.capture_id as capture_id\n";
        String FROM   = "FROM transaction as t\n";
        String JOIN   = "INNER JOIN sale as s\n" +
                        "    ON t.sale_id = s.sale_id\n";
        String WHERE  = "WHERE s.email = ?;";

        String query  = SELECT + FROM + JOIN + WHERE;

        PreparedStatement ps = null;
        try {
            ps = BillingService.getCon().prepareStatement(query);
            ps.setString(1, email);
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Unable to build query that gets capture_ids from email.");
        }
        return ps;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // call: The action being done to the table.
    // table: 'sale' or 'transaction'
    public void sendUpdate(PreparedStatement ps, String call, String table) {
        try {
            ServiceLogger.LOGGER.info("Trying update: " + ps.toString());
            ps.executeUpdate();
            ServiceLogger.LOGGER.info("Query succeeded.");
            //return null;
        } catch (SQLException SQLE) {
            SQLE.printStackTrace();
            ServiceLogger.LOGGER.warning(String.format("Query failed: Unable to %s %s", call, table));
        }
        //return new ResponseModel(Result.ORDER_CREATION_FAIL);  // Sale or transaction operation failed.
    }

    // Sends queries to table 'sale' to retrieve sales.
    public int sendSaleIDQuery(PreparedStatement ps) {
        try {
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            rs.next(); // skips rs header
            return rs.getInt("sale_id");
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve sale records(s).");
        }
        return -1;
    }

    // Sends queries to get email mapped from transaction token
    public String sendTokenToEmailQuery(PreparedStatement ps) {
        try {
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString("email");
        }
        catch (SQLException e) {
            e.printStackTrace();
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve email mapped by token.");
        }
        return null;
    }

    // Sends queries to get capture_ids mapped by email
    public ResultSet sendEmailToCaptureIDQuery(PreparedStatement ps) {
        try {
            ServiceLogger.LOGGER.info("Trying query: " + ps.toString());
            ResultSet rs = ps.executeQuery();
            ServiceLogger.LOGGER.info("Query succeeded.");
            return rs;
        }
        catch (SQLException e) {
            ServiceLogger.LOGGER.warning("Query failed: Unable to retrieve capture_id from an email.");
        }
        return null; // if no capture_ids are found with an email;
    }
}
