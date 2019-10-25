package com.example.android.project_8_inventory_1;

import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project_8_inventory_1.data.BookContract.BookEntry;

import java.io.ByteArrayInputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private Uri mBookUri;
    private static final String [] PROJECTION = {BookEntry._ID,
            BookEntry.COLUMN_BOOK_NAME, BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY,
            BookEntry.COLUMN_IMAGE_URI, BookEntry.COLUMN_SUPPLIER_NAME, BookEntry.COLUMN_SUPLLIER_PHONE_NUMBER};

    private byte[] mBookThumbnailArray;

    @BindView(R.id.details_text_book_name)
    TextView mBookName;
    @BindView(R.id.details_text_book_price)
    TextView mBookPrice;
    @BindView(R.id.details_text_book_quantity)
    TextView mBookQuantity;
    @BindView(R.id.details_text_book_supplier_name)
    TextView mSupplierName;
    @BindView(R.id.details_text_book_supplier_phone_number)
    TextView mSupplierPhoneNumber;
    @BindView(R.id.details_img_book_thumbnail)
    ImageView mBookThumbnail;
    @BindView(R.id.details_button_quantity_decrease)
    Button mDecreaseQuantity;
    @BindView(R.id.details_button_quantity_increase)
    Button mIncreaseQuantity;
    @BindView(R.id.details_button_delete)
    Button mDeleteBookButton;
    @BindView(R.id.details_button_order)
    Button mOrderBookButton;
    @BindView(R.id.details_button_edit)
    Button mEditBookButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        Intent intent =  getIntent();
        mBookUri = intent.getData();
        if(mBookUri == null) {
            finish();
        }
        getLoaderManager().initLoader(0, null, this);
        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseQuantity();
            }
        });

        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseQuantity();
            }
        });

        mDeleteBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });

        mOrderBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderBook();
            }
        });

        mEditBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBook();
            }
        });
    }

    private void editBook(){
        Intent goToEditorActivity = new Intent(DetailsActivity.this, EditorActivity.class);
        goToEditorActivity.setData(mBookUri);
        Log.v(DetailsActivity.class.getSimpleName(), "Book URI: " + mBookUri);
        startActivity(goToEditorActivity);
    }
    private void orderBook(){
        String supplierPhoneNumber = mSupplierPhoneNumber.getText().toString().trim();
        try{
            Intent callSupplier = new Intent(Intent.ACTION_DIAL);
            callSupplier.setData(Uri.parse("tel:" + supplierPhoneNumber));
            startActivity(callSupplier);
        }catch (ActivityNotFoundException e){
            Toast.makeText(this, "Call Failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
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
     * Perform the deletion of the pet in the database.
     */
    private void deleteBook() {
        if(mBookUri == null) return;
        int deletedBook = getContentResolver().delete(mBookUri, null, null);
        if(deletedBook > 0){
            Toast.makeText(this, getString(R.string.editor_delete_book_successful), Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(this, getString(R.string.editor_delete_book_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(this, mBookUri, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(data.getCount() > 0){
            data.moveToFirst();
            int nameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = data.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = data.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = data.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supploerPhoneNumberColumnIndex = data.getColumnIndex(BookEntry.COLUMN_SUPLLIER_PHONE_NUMBER);
            int thumbnailColumnIndex = data.getColumnIndex(BookEntry.COLUMN_IMAGE_URI);

            String name = data.getString(nameColumnIndex);
            float price = data.getFloat(priceColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);
            String supplierName = data.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = data.getString(supploerPhoneNumberColumnIndex);
            mBookThumbnailArray = data.getBlob(thumbnailColumnIndex);

            mBookName.setText(name);
            mBookPrice.setText(String.valueOf(price));
            mBookQuantity.setText(String.valueOf(quantity));
            mSupplierName.setText(supplierName);
            mSupplierPhoneNumber.setText(supplierPhoneNumber);

            if(mBookThumbnailArray != null){
                ByteArrayInputStream stream = new ByteArrayInputStream(mBookThumbnailArray);
                Bitmap thumbnail = BitmapFactory.decodeStream(stream);
                mBookThumbnail.setImageBitmap(thumbnail);
            }else{
                mBookThumbnail.setImageResource(R.drawable.default_book);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mBookName.setText(null);
        mBookPrice.setText(null);
        mBookQuantity.setText(null);
        mSupplierName.setText(null);
        mSupplierPhoneNumber.setText(null);
        mBookThumbnail.setImageResource(R.drawable.default_book);
    }

    private void decreaseQuantity(){
        int quantity = Integer.parseInt(mBookQuantity.getText().toString().trim());
        if(quantity == 0){
            Toast.makeText(this, "Can't decrease quantity anymore!", Toast.LENGTH_SHORT).show();
            return;
        }
        int newQuantity = quantity -1;
        mBookQuantity.setText(String.valueOf(newQuantity));
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_QUANTITY, newQuantity);
        String selection = BookEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mBookUri))};
        int updatedRows = getContentResolver().update(mBookUri, values, selection, selectionArgs);
        if(updatedRows > 0){
            Toast.makeText(this, "Quantity decreased by 1", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Error Quantity didn't decrease by 1", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseQuantity(){
        int quantity = Integer.parseInt(mBookQuantity.getText().toString().trim());
        int newQuantity = quantity +1;
        mBookQuantity.setText(String.valueOf(newQuantity));
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_QUANTITY, newQuantity);
        String selection = BookEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mBookUri))};
        int updatedRows = getContentResolver().update(mBookUri, values, selection, selectionArgs);
        if(updatedRows > 0){
            Toast.makeText(this, "Quantity increased by 1", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Error Quantity didn't increase by 1", Toast.LENGTH_SHORT).show();
        }
    }
}