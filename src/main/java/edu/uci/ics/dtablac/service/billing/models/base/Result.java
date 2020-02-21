package edu.uci.ics.dtablac.service.billing.models.base;

import javax.ws.rs.core.Response.Status;

public enum Result {
    JSON_PARSE_EXCEPTION    (-3, "JSON Parse Exception.", Status.BAD_REQUEST),
    JSON_MAPPING_EXCEPTION  (-2, "JSON Mapping Exception", Status.BAD_REQUEST),
    INTERNAL_SERVER_ERROR   (-1, "Internal Server Error", Status.BAD_REQUEST),

    USER_NOT_FOUND          (14, "User not found.", Status.OK),
    QUANTITY_INVALID_VALUE  (33, "Quantity has invalid value.", Status.OK),
    DUPLICATE_INSERTION     (311, "Duplicate insertion.", Status.OK),
    ITEM_DOES_NOT_EXIST     (312, "Shopping cart item does not exist.", Status.OK),
    ORDER_HISTORY_DNE       (313, "Order history does not exist..", Status.OK),
    ORDER_CREATION_FAIL     (342, "Order creation failed.", Status.OK),

    CART_INSERT_SUCCESS     (3100, "Shopping cart item inserted successfully.", Status.OK),
    CART_UPDATE_SUCCESS     (3110, "Shopping cart item updated successfully.", Status.OK),
    ITEM_DELETE_SUCCESS     (3120, "Shopping cart item deleted successfully.", Status.OK),
    CART_RETRIEVE_SUCCESS   (3130, "Shopping cart retrieved successfully.", Status.OK),
    CART_CLEAR_SUCCESS      (3140, "Shopping cart cleared successfully.", Status.OK),
    CART_OP_FAIL            (3150, "Shopping cart operation failed.", Status.OK),

    ORDER_PLACE_SUCCESS     (3400, "Order placed successfully.", Status.OK),
    ORDER_RETRIEVE_SUCCESS  (3410, "Orders retrieved successfully.", Status.OK),
    ORDER_COMPLETE          (3420, "Order is completed successfully.", Status.OK),
    TOKEN_NOT_FOUND         (3421, "Token not found.", Status.OK),
    ORDER_INCOMPLETE        (3422, "Order can not be completed.", Status.OK)
    ;

    private final int       resultCode;
    private final String    message;
    private final Status    status;

    Result(int resultCode, String message, Status status) {
        this.resultCode = resultCode;
        this.message = message;
        this.status = status;
    }

    public int getResultCode() { return resultCode; }

    public String getMessage() { return message; }

    public Status getStatus() { return status; }
}
