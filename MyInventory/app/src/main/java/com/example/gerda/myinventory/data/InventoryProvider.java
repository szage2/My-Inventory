package com.example.gerda.myinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Gerda on 12/16/2016.
 */

public class InventoryProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the product table */
    private static final int INVENTORY = 100;

    /** URI matcher code for the content URI for a single product in the inventory table */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.inventory/products" will map to the
        // integer code {@link #PRODUCTS}. This URI is used to provide access to MULTIPLE rows
        // of the inventory table.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);

        // The content URI of the form "content://com.example.android.inventory/inventory/#" will map to the
        // integer code {@link #PRODUCT_ID}. This URI is used to provide access to ONE single row
        // of the product table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.inventory/inventory/3" matches, but
        // "content://com.example.android.inventory/inventory" (without a number at the end) doesn't match.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", PRODUCT_ID);
    }

    /** Database helper object */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // For the INVENTORY code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventory/inventory/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the inventory table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String picture = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE);
        // Check that the picture URL is not null
        if (picture == null) {
            throw new IllegalArgumentException("Product requires a picture URL");
        }

        String contact = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_CONTACT);
        // Check that the picture URL is not null
        if (name == null) {
            throw new IllegalArgumentException("Product requires a picture URL");
        }

        // Check that the status is valid
        Integer status = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_STATUS);
        if (status == null || !InventoryContract.InventoryEntry.isValidStatus(status)) {
            throw new IllegalArgumentException("Product requires valid status" + status);
        }

        // Check that the sale is valid
        Integer sale = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SALE);
        if (sale == null || !InventoryContract.InventoryEntry.isValidSale(sale)) {
            throw new IllegalArgumentException("Product requires valid sale" + sale);
        }

        // Check that the delivery is valid
        Integer delivery = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DELIVERY);
        if (delivery == null || !InventoryContract.InventoryEntry.isValidDelivery(delivery)) {
            throw new IllegalArgumentException("Product requires valid status" + delivery);
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Product requires valid weight");
        }

        // No need to check the description, any value is valid (including null)

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection,
                              String[] selectionArgs) {
        // If the {@link InventoryEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT_DESCRIPTION} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a valid description");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_PICTURE} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE)) {
            String picture = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE);
            if (picture == null) {
                throw new IllegalArgumentException("Product requires a picture URL");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT_PICTURE} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_CONTACT)) {
            String contact = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_CONTACT);
            if (contact == null) {
                throw new IllegalArgumentException("Product requires a contact of supplier");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT_SALE} key is present,
        // check that the status value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_STATUS)) {
            Integer status = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_STATUS);
            if (status == null || !InventoryContract.InventoryEntry.isValidSale(status)) {
                throw new IllegalArgumentException("Product requires valid status");
            }
        }


        // If the {@link InventoryEntry#COLUMN_PRODUCT_SALE} key is present,
        // check that the sale value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SALE)) {
            Integer sale = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SALE);
            if (sale == null || !InventoryContract.InventoryEntry.isValidSale(sale)) {
                throw new IllegalArgumentException("Product requires valid sale");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT_SALE} key is present,
        // check that the sale value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DELIVERY)) {
            Integer delivery = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DELIVERY);
            if (delivery == null || !InventoryContract.InventoryEntry.isValidSale(delivery)) {
                throw new IllegalArgumentException("Product requires valid delivery");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT} key is present,
        // check that the height value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WEIGHT)) {
            // Check that the height is greater than or equal to 0 cm
            Integer height = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_HEIGHT);
            if (height != null && height < 0) {
                throw new IllegalArgumentException("Product requires valid height");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT} key is present,
        // check that the width value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WIDTH)) {
            // Check that the width is greater than or equal to 0 cm
            Integer width = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WIDTH);
            if (width != null && width < 0) {
                throw new IllegalArgumentException("Product requires valid width");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT} key is present,
        // check that the length value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_LENGTH)) {
            // Check that the length is greater than or equal to 0 cm
            Integer length = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_LENGTH);
            if (length != null && length < 0) {
                throw new IllegalArgumentException("Product requires valid length");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Product requires valid weight");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT} key is present,
        // check that the price value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            // Check that the price is greater than or equal to 0 $
            Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRODUCT} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            // Check that the quantity is greater than or equal to 0 pcs
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        // No need to check the description, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);


        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
