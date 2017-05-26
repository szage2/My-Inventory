package com.example.gerda.myinventory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gerda.myinventory.data.InventoryContract;

import java.util.Vector;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter{

    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryCursorAdapter.class.getSimpleName();
    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) { super(context, c, 0 /* flags*/); }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the product TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.product);
        TextView attributeTextView = (TextView) view.findViewById(R.id.summary);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.unit_pcs);
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.picture);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        // Find the columns of product attributes that we're interested in
        int productColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int descriptionColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        final int quantityColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int pictureColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE);
        int itemIdColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(productColumnIndex);
        String productDescription = cursor.getString(descriptionColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);
        String productPicture = cursor.getString(pictureColumnIndex);
        final long itemId = cursor.getLong(itemIdColumnIndex);

        // If the product description is empty string or null, then use some default text
        // that says "Unknown attributes", so the TextView isn't blank.
        if (TextUtils.isEmpty(productDescription)) {
            productDescription = context.getString(R.string.unknown_attributes);
        }

        // If the product quantity is empty string or null, then use some default text
        // that says "No on stock", so the TextView isn't blank.
        if (TextUtils.isEmpty(productQuantity)) {
            productQuantity = context.getString(R.string.no_on_stock);
        }

        nameTextView.setText(productName);
        attributeTextView.setText(productDescription);
        priceTextView.setText(productPrice);
        quantityTextView.setText(productQuantity);

        // Decreases the quantity of the product by 1
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentResolver resolver = view.getContext().getContentResolver();
                ContentValues values = new ContentValues();

                Log.i(LOG_TAG, "TEST: Sell onClick called");

                TextView quantityView = (TextView) view.findViewById(R.id.unit_pcs);
                String quantity = quantityView.getText().toString();

                int quantityNumber = Integer.parseInt(quantity);

                if (quantityNumber > 0) {
                    quantityNumber -= 1;
                    String quantityString = Integer.toString(quantityNumber);
                    //quantityTextView.setText(quantityString);

                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);


                    Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                    resolver.update(
                            uri,
                            values,
                            null,
                            null);

                } else {
                    Toast.makeText(context, context.getString(R.string.decrement_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display the product's image in the list view
        Glide.with(context).load(productPicture).into(pictureImageView);
    }


}
