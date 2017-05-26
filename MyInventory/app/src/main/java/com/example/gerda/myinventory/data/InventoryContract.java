package com.example.gerda.myinventory.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/** API Contract for the Products app. */
public final class InventoryContract {

    // Empty constructor.
    private InventoryContract() {}

    /**
     * The "Content authority" is a name for the entire content provider
     */
    public static final String CONTENT_AUTHORITY = "com.example.gerda.myinventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.inventory/inventory/ is a valid path for
     * looking at inventory data. content://com.example.android.
     */
    public static final String PATH_INVENTORY = "myinventory";

    /**
     * Inner class that defines constant values for the inventory database table.
     * Each entry in the table represents a single product.
     */
    public static final class InventoryEntry implements BaseColumns{

        /** The content URI to access the inventory data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /** Name of database table for inventory */
        public final static String TABLE_NAME = "inventory";


        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME = "product";

        /**
         * Description of the product.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_DESCRIPTION = "description";

        /**
         * Height of the product.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_HEIGHT = "height";

        /**
         * Width of the product.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_WIDTH = "width";

        /**
         * Length of the product.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_LENGTH = "length";

        /**
         * Weight of the product.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_WEIGHT = "weight";

        /**
         * Sale of the product.
         *
         * The only possible values are {@link #NO_SALE}, {@link #SMALL_SALE}, {@link #MEDIUM_SALE},
         * or {@link #HUGE_SALE}.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_SALE = "sale";

        /**
         * Delivery status of the product.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_STATUS = "status";

        /**
         * Estimated Delivery of the product.
         *
         * Type: ?????????????????????????????????????????????????????????????????????????????????
         */
        public static final String COLUMN_PRODUCT_DELIVERY = "delivery";

        /**
         * Picture of the product.
         *
         * Type: ?????????????????????????????????????????????????????????????????????????????????
         */
        public static final String COLUMN_PRODUCT_PICTURE = "picture";

        /**
         * Price of the product.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_PRICE = "price";

        /**
         * quantity of the product.
         *
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * quantity of the product.
         *
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_CONTACT = "contact";

        /**
         * Possible values for the sale of the product.
         */
        public static final int NO_SALE = 0;
        public static final int SMALL_SALE = 1;
        public static final int MEDIUM_SALE = 2;
        public static final int HUGE_SALE = 3;

        /**
         * Possible values for the status of the product.
         */
        public static final int NOT_SHIPPED = 0;
        public static final int SHIPPED = 1;
        public static final int IN_TRANSIT = 2;
        public static final int DELIVERED = 3;

        /**
         * Possible values for the delivery mode of the product.
         */
        public static final int USUAL_DELIVERY = 0;
        public static final int EXPEDITE_DELIVERY = 1;


        /**
         * Returns whether or not the given sale is {@link #NO_SALE}, {@link #SMALL_SALE}, {@link #MEDIUM_SALE},
         * or {@link #HUGE_SALE}.
         */
        public static boolean isValidSale (int sale) {
            if (sale == NO_SALE || sale == SMALL_SALE || sale == MEDIUM_SALE || sale == HUGE_SALE) {
                return true;
            }
            return false;
        }

        public static boolean isValidStatus (int status) {
            if (status == NOT_SHIPPED || status == SHIPPED || status == IN_TRANSIT || status == DELIVERED) {
                return true;
            }
            return false;
        }

        public static boolean isValidDelivery (int delivery) {
            if (delivery == USUAL_DELIVERY || delivery == EXPEDITE_DELIVERY) {
                return true;
            }
            return false;
        }
    }

}
