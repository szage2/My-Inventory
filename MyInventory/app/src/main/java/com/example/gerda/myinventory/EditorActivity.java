package com.example.gerda.myinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.gerda.myinventory.data.InventoryContract;

import java.net.URI;

/**
 * Allows user to create a new product or edit an existing one.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the product data loader */
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private static final String LOG_TAG = null;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    /** EditText field to enter the product name */
    private EditText mNameEditText;

    /** EditText field to enter the product's description */
    private EditText mDescriptionEditText;

    /** EditText field to enter the product's height */
    private EditText mHeightEditText;

    /** EditText field to enter the product's length */
    private EditText mLengthEditText;

    /** EditText field to enter the product's width */
    private EditText mWidthEditText;

    /** EditText field to enter the product's weight */
    private EditText mWeightEditText;

    /** Spinner field to enter the product's product's sale percentage */
    private Spinner mSaleSpinner;

    /** Spinner field to enter the delivery status of a product */
    private Spinner mStatusSpinner;

    /** Spinner field to enter the product's estimated delivery period */
    private Spinner mDeliverySpinner;

    /** EditText field to enter URL of picture for the particular product */
    private EditText mPictureUrlEditText;

    /** EditText field to enter the product's quantity */
    public EditText mQuantityEditText;

    /** EditText field to enter the product's price */
    private EditText mPriceEditText;

    /** EditText field to enter the supplier's contact information */
    private EditText mContactEditText;

    private ImageView mPictureImageView;

    /** String for the image URL */
    private String mPictureString;

    /** Button for ordering more of product */
    private Button mOrderMoreButton;

    /** Button for increasing the quantity of product  */
    private Button mIncrementButton;

    /** Button for decreasing the quantity of product  */
    private Button mDecrementButton;



    /**
     * Sale of the product. The possible valid values are in the InventoryContract.java file:
     * {@link InventoryContract.InventoryEntry#NO_SALE},{@link InventoryContract.InventoryEntry#SMALL_SALE},
     * {@link InventoryContract.InventoryEntry#MEDIUM_SALE}, or {@link InventoryContract.InventoryEntry#HUGE_SALE}.
     */
    private int mSale = InventoryContract.InventoryEntry.NO_SALE;

    private int mDelivery = InventoryContract.InventoryEntry.USUAL_DELIVERY;

    private int mStatus = InventoryContract.InventoryEntry.NOT_SHIPPED;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        final Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if(mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mOrderMoreButton = (Button) findViewById(R.id.order_more);

        mIncrementButton = (Button) findViewById(R.id.increment);

        mDecrementButton = (Button) findViewById(R.id.decrement);


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mHeightEditText = (EditText) findViewById(R.id.edit_product_height);
        mWidthEditText = (EditText) findViewById(R.id.edit_product_width);
        mLengthEditText = (EditText) findViewById(R.id.edit_product_length);
        mWeightEditText = (EditText) findViewById(R.id.edit_product_weight);
        mSaleSpinner = (Spinner) findViewById(R.id.spinner_sale);
        mStatusSpinner = (Spinner) findViewById(R.id.spinner_status);
        mDeliverySpinner = (Spinner) findViewById(R.id.spinner_delivery);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPictureUrlEditText = (EditText) findViewById(R.id.edit_product_picture);
        mContactEditText = (EditText) findViewById(R.id.edit_product_contact);

        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mHeightEditText.setOnTouchListener(mTouchListener);
        mWidthEditText.setOnTouchListener(mTouchListener);
        mLengthEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mSaleSpinner.setOnTouchListener(mTouchListener);
        mStatusSpinner.setOnTouchListener(mTouchListener);
        mDeliverySpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mContactEditText.setOnTouchListener(mTouchListener);

        mPictureString = mPictureUrlEditText.getText().toString().trim();

        // Find the Image View with the id image URL
        mPictureImageView = (ImageView) findViewById(R.id.picture);
        // Load the image with Glide based on the mPictureString (extracted from EditText)
        Glide.with(this).load(mPictureString).into(mPictureImageView);

        final String quantityToString = mQuantityEditText.getText().toString().trim();

        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current quantity
                TextView quantity = (TextView) findViewById(R.id.edit_product_quantity);
                int quantityConverted = Integer.parseInt(quantity.getText().toString());
                if (quantityConverted < 100) {
                    // Increase quantity by 1
                    quantityConverted += 1;

                    quantity.setText(Integer.toString(quantityConverted));

                } else {
                    Toast.makeText(getBaseContext(), "You cannot have more than 100 pcs", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current quantity
                TextView quantity = (TextView) findViewById(R.id.edit_product_quantity);
                int quantityConverted = Integer.parseInt(quantity.getText().toString());
                if (quantityConverted > 0) {
                    // Decrease quantity by 1
                    quantityConverted -= 1;

                    quantity.setText(Integer.toString(quantityConverted));

                } else {
                    Toast.makeText(getBaseContext(), "You cannot have less than 0 pcs", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Opens the contact app for the user
        mOrderMoreButton.setOnClickListener (new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contact = mContactEditText.getText().toString().trim();
                Uri call = Uri.parse("tel:" + contact);
                Intent orderMoreIntent = new Intent(Intent.ACTION_DIAL, call);
                startActivity(orderMoreIntent);
            }
        });

        setupSaleSpinner();
        setupStatusSpinner();
        setupDeliverySpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the sale of the product.
     */
    private void setupSaleSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter saleSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_sale_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        saleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSaleSpinner.setAdapter(saleSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.no_sale))) {
                        mSale = InventoryContract.InventoryEntry.NO_SALE; // no sale
                    } else if (selection.equals(getString(R.string.small_sale))) {
                        mSale = InventoryContract.InventoryEntry.SMALL_SALE; // small
                    } else if (selection.equals(getString(R.string.medium_sale))) {
                        mSale = InventoryContract.InventoryEntry.MEDIUM_SALE; // medium
                    } else {mSale = InventoryContract.InventoryEntry.HUGE_SALE; // huge
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSale = InventoryContract.InventoryEntry.NO_SALE;
            }
        });
    }

    private void setupStatusSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter statusSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_status_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mStatusSpinner.setAdapter(statusSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.status_not_shipped))) {
                        mStatus = InventoryContract.InventoryEntry.NOT_SHIPPED; // not shipped
                    } else if (selection.equals(getString(R.string.status_shipped))) {
                        mStatus = InventoryContract.InventoryEntry.SHIPPED; // shipped
                    } else if (selection.equals(getString(R.string.status_in_transit))) {
                        mStatus = InventoryContract.InventoryEntry.IN_TRANSIT; // in transit
                    } else {mStatus = InventoryContract.InventoryEntry.DELIVERED; // delivered
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mStatus = InventoryContract.InventoryEntry.NOT_SHIPPED;
            }
        });
    }

    private void setupDeliverySpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter deliverySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_delivery_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        deliverySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mDeliverySpinner.setAdapter(deliverySpinnerAdapter);

        // Set the integer mSelected to the constant values
        mDeliverySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.usual_delivery))) {
                        mDelivery = InventoryContract.InventoryEntry.USUAL_DELIVERY; // usual delivery period
                    } else {mDelivery = InventoryContract.InventoryEntry.EXPEDITE_DELIVERY; // expedite delivery period
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mDelivery = InventoryContract.InventoryEntry.USUAL_DELIVERY;
            }
        });
    }

    /**
     * Get user input from editor and save product into database.
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String heightString = mHeightEditText.getText().toString().trim();
        String widthString = mWidthEditText.getText().toString().trim();
        String lengthString = mLengthEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String contactString = mContactEditText.getText().toString().trim();

        mPictureString = mPictureUrlEditText.getText().toString().trim();

        // Find the Image View with the id image URL
        mPictureImageView = (ImageView) findViewById(R.id.picture);
        // load the image with Glide based on the pictureString (extracted from EditText)
        Glide.with(this).load(mPictureString).into(mPictureImageView);

        //ImageView pictureListImageView = (ImageView) findViewById(R.id.picture);

        // load the image with Glide based on the pictureString (extracted from EditText)
        //Glide.with(this).load(pictureString).into(pictureListImageView);



        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString) ||
                TextUtils.isEmpty(heightString) || TextUtils.isEmpty(widthString) ||
                TextUtils.isEmpty(lengthString) || TextUtils.isEmpty(weightString) ||
                TextUtils.isEmpty(priceString) || TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(mPictureString) && TextUtils.isEmpty(contactString) &&
                mSale == InventoryContract.InventoryEntry.NO_SALE &&
                mStatus == InventoryContract.InventoryEntry.NOT_SHIPPED &&
                mDelivery == InventoryContract.InventoryEntry.USUAL_DELIVERY) {
            // Since no fields were modified, we can return early without creating a new product.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Toast.makeText(getBaseContext(), "You did not filled out all blocks, please try again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SALE, mSale);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_STATUS, mStatus);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DELIVERY, mDelivery);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE, mPictureString);
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_CONTACT, contactString);

        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int weight = 0;
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WEIGHT, weight);

        int height = 0;
        if (!TextUtils.isEmpty(heightString)) {
            height = Integer.parseInt(heightString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_HEIGHT, height);

        int width = 0;
        if (!TextUtils.isEmpty(widthString)) {
            width = Integer.parseInt(widthString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WIDTH, width);

        int length = 0;
        if (!TextUtils.isEmpty(lengthString)) {
            length = Integer.parseInt(lengthString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_LENGTH, length);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, price);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null){
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPeroductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_HEIGHT,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_WIDTH,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_LENGTH,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_WEIGHT,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_SALE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_STATUS,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_DELIVERY,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_CONTACT};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            int heightColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_HEIGHT);
            int widthColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WIDTH);
            int lengthColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_LENGTH);
            int weightColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_WEIGHT);
            int saleColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SALE);
            int statusColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_STATUS);
            int deliveryColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_DELIVERY);
            int pictureColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PICTURE);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int contactColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_CONTACT);


            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int height = cursor.getInt(heightColumnIndex);
            int width = cursor.getInt(widthColumnIndex);
            int length = cursor.getInt(lengthColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);
            int sale = cursor.getInt(saleColumnIndex);
            int status = cursor.getInt(statusColumnIndex);
            int delivery = cursor.getInt(deliveryColumnIndex);
            String picture = cursor.getString(pictureColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String contact = cursor.getString(contactColumnIndex);


            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mHeightEditText.setText(Integer.toString(height));
            mWidthEditText.setText(Integer.toString(width));
            mLengthEditText.setText(Integer.toString(length));
            mWeightEditText.setText(Integer.toString(weight));
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mPictureUrlEditText.setText(picture);
            mContactEditText.setText(contact);
            mPictureImageView.setImageURI(Uri.parse(picture));

            // Sale is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (sale) {
                case InventoryContract.InventoryEntry.SMALL_SALE:
                    mSaleSpinner.setSelection(1);
                    break;
                case InventoryContract.InventoryEntry.MEDIUM_SALE:
                    mSaleSpinner.setSelection(2);
                    break;
                case InventoryContract.InventoryEntry.HUGE_SALE:
                    mSaleSpinner.setSelection(3);
                    break;
                default:
                    mSaleSpinner.setSelection(0);
                    break;
            }

            // Status is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (status) {
                case InventoryContract.InventoryEntry.SHIPPED:
                    mSaleSpinner.setSelection(1);
                    break;
                case InventoryContract.InventoryEntry.IN_TRANSIT:
                    mSaleSpinner.setSelection(2);
                    break;
                case InventoryContract.InventoryEntry.DELIVERED:
                    mSaleSpinner.setSelection(3);
                    break;
                default:
                    mSaleSpinner.setSelection(0);
                    break;
            }

            // delivery is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is usual delivery, 1 is expedite shipment, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (delivery) {
                case InventoryContract.InventoryEntry.EXPEDITE_DELIVERY:
                    mSaleSpinner.setSelection(1);
                    break;
                default:
                    mSaleSpinner.setSelection(0);
                    break;
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mPictureUrlEditText.setText("");
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mHeightEditText.setText("");
        mWidthEditText.setText("");
        mLengthEditText.setText("");
        mWeightEditText.setText("");
        mSaleSpinner.setSelection(0); // Select "Unknown" sale
        mStatusSpinner.setSelection(0); // Select "Unknown" status
        mDeliverySpinner.setSelection(0); // Select "Unknown" delivery
        mQuantityEditText.setText("");
        mContactEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
