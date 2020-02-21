package edu.uci.ics.dtablac.service.billing.models.data;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;

public class PayPalOrderClient {
    // Client id and secret retrieved from sandbox.
    //"The Client ID from PayPal Sandbox";
    private static final String clientId =
            "ASZcMaIo0PFX-p9NcugEElhBdX-ag1l_hTz6-b_QvbTeDK8eRybT42YHFEsxgsOzgVFipHhLhJHBSBF4";
    //"The Secret Client Key from PayPal Sandbox";
    private static final String clientSecret =
            "EO7D1pK8cNMtzVNie-ilx1F5Oty24g9tOp3U9zN8cHwATT6tSYIVN5aQBT-shpINjbK0T63c32Yhm4c4";

    public PayPalOrderClient() {}

    // Set up paypal environment
    public PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);

    // Create client for environment
    public PayPalHttpClient client = new PayPalHttpClient(environment);
}
