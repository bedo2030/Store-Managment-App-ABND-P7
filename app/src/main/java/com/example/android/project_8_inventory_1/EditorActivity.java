package com.example.android.project_8_inventory_1;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.project_8_inventory_1.data.BookContract.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private final static int SELECT_PHOTO_REQUEST_CODE = 200;
    private final static int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 666;
    private Bitmap bitmap;


    private static final String [] PROJECTION = {BookEntry._ID,
            BookEntry.COLUMN_BOOK_NAME, BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY,
            BookEntry.COLUMN_SUPPLIER_NAME, BookEntry.COLUMN_SUPLLIER_PHONE_NUMBER,
            BookEntry.COLUMN_IMAGE_URI};

    private Uri mBookUri;

    private boolean mBookHasChanged = false;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mBookHasChanged = true;
            return false;
        }
    };

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;
    private ImageView mBookThumbnail;
    private Button mBookThumbnailSelectButton;

    private byte[] mBookThumbnailByteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mBookUri = intent.getData();

        if(mBookUri != null){
            getSupportActionBar().setTitle("Edit Book");
        }else{
            getSupportActionBar().setTitle("Add Book");
        }
        // Initialize Book info and supplier views
        mNameEditText = findViewById(R.id.edit_book_name);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_book_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneNumberEditText = findViewById(R.id.edit_supplier_phone);
        mBookThumbnail = findViewById(R.id.img_book_thumbnail);
        mBookThumbnailSelectButton = findViewById(R.id.btn_add_book_thumbnail);

        mNameEditText.setOnTouchListener(mOnTouchListener);
        mPriceEditText.setOnTouchListener(mOnTouchListener);
        mQuantityEditText.setOnTouchListener(mOnTouchListener);
        mSupplierNameEditText.setOnTouchListener(mOnTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mOnTouchListener);
        mBookThumbnailSelectButton.setOnTouchListener(mOnTouchListener);

        // Select Book thumbnail from gallery
        mBookThumbnailSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelectionActivity();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    }

                }
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if(!mBookHasChanged){
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();

            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, projection, null, null, null);

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);

            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            bitmap = BitmapFactory.decodeFile(picturePath);

            bitmap = getBitmapFromUri(selectedImage);

            mBookThumbnail = findViewById(R.id.img_book_thumbnail);

            mBookThumbnail.setImageBitmap(bitmap);
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty()) {
            return null;
        }

        mBookThumbnail = (ImageView) findViewById(R.id.img_book_thumbnail);
        int targetW = mBookThumbnail.getWidth();
        int targetH = mBookThumbnail.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(EditorActivity.class.getSimpleName(), "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(EditorActivity.class.getSimpleName(), "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
    public void openImageSelectionActivity(){
        Intent intent;
        if(Build.VERSION.SDK_INT < 19){
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Book Thumbnail") , SELECT_PHOTO_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                saveBook();
                finish();
                break;
            case R.id.action_delete:
                if(mBookUri != null){
                    showDeleteConfirmationDialog();
                }else{
                    Toast.makeText(this, "You can't delete a book you didn't add yet.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if(!mBookHasChanged){
                    NavUtils.navigateUpFromSameTask(this);
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
            NavUtils.navigateUpFromSameTask(this);
        }else{
            Toast.makeText(this, getString(R.string.editor_delete_book_failed), Toast.LENGTH_SHORT).show();
        }
    }
    private void saveBook(){
        String bookName =  mNameEditText.getText().toString().trim();
        String supplierName = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumber = mSupplierPhoneNumberEditText.getText().toString().trim();
        String bookPrice = mPriceEditText.getText().toString().trim();
        String bookQuantity = mQuantityEditText.getText().toString().trim();
        if(mBookThumbnail != null){
            getByteArrayFromImageView();
        }
        ContentValues contentValues = new ContentValues();
        if(TextUtils.isEmpty(bookName) && TextUtils.isEmpty(supplierName)
                && TextUtils.isEmpty(supplierPhoneNumber) && TextUtils.isEmpty(bookPrice)){
            finish();
        }else{
            float price = 0;
            if(!TextUtils.isEmpty(bookPrice)){
                price = Float.parseFloat(bookPrice);
            }

            int quantity = 0;
            if(!TextUtils.isEmpty(bookQuantity)){
                quantity = Integer.parseInt(bookQuantity);
            }
            if(mBookUri == null){


                contentValues.put(BookEntry.COLUMN_BOOK_NAME, bookName);
                contentValues.put(BookEntry.COLUMN_PRICE, price);
                contentValues.put(BookEntry.COLUMN_QUANTITY, quantity);
                contentValues.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
                contentValues.put(BookEntry.COLUMN_SUPLLIER_PHONE_NUMBER, supplierPhoneNumber);
                contentValues.put(BookEntry.COLUMN_IMAGE_URI, mBookThumbnailByteArray);

                Uri uri = getContentResolver().insert(BookEntry.CONTENT_URI,contentValues);
                if(uri != null){
                    Toast.makeText(this, "Book Added successfully", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Book Adding Failed", Toast.LENGTH_SHORT).show();
                }
            }else {
                contentValues.put(BookEntry.COLUMN_BOOK_NAME, bookName);
                contentValues.put(BookEntry.COLUMN_PRICE, price);
                contentValues.put(BookEntry.COLUMN_QUANTITY, quantity);
                contentValues.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
                contentValues.put(BookEntry.COLUMN_SUPLLIER_PHONE_NUMBER, supplierPhoneNumber);
                contentValues.put(BookEntry.COLUMN_IMAGE_URI, mBookThumbnailByteArray);

                int updatedRows = getContentResolver().update(mBookUri,contentValues,null,null);
                if(updatedRows > 0){
                    Toast.makeText(this, "Book edited successfully", Toast.LENGTH_SHORT);
                }else{
                    Toast.makeText(this, "Book editing Failed", Toast.LENGTH_SHORT);
                }
            }
        }
    }

    private void getByteArrayFromImageView(){
        Bitmap bitmap1 = ((BitmapDrawable) mBookThumbnail.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream);
        mBookThumbnailByteArray = stream.toByteArray();

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        if(mBookUri == null){
            return new CursorLoader(this, BookEntry.CONTENT_URI, PROJECTION, null, null, null);
        }else {
            return new CursorLoader(this, mBookUri, PROJECTION, null, null, null);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if(data.getCount() > 0 && mBookUri != null){
            data.moveToFirst();

            String name = data.getString(data.getColumnIndex(BookEntry.COLUMN_BOOK_NAME));
            String price = String.valueOf(data.getFloat(data.getColumnIndex(BookEntry.COLUMN_PRICE)));
            String quantity = String.valueOf(data.getInt(data.getColumnIndex(BookEntry.COLUMN_QUANTITY)));
            String supplierName = data.getString(data.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME));
            String supplierPhoneNumber = data.getString(data.getColumnIndex(BookEntry.COLUMN_SUPLLIER_PHONE_NUMBER));

            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(quantity);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);
            mBookThumbnailByteArray = data.getBlob(data.getColumnIndex(BookEntry.COLUMN_IMAGE_URI));
            if(mBookThumbnailByteArray != null){
                ByteArrayInputStream stream = new ByteArrayInputStream(mBookThumbnailByteArray);
                Bitmap bitmap1 = BitmapFactory.decodeStream(stream);
                mBookThumbnail.setImageBitmap(bitmap1);
            }else{

                mBookThumbnail.setImageResource(R.drawable.default_book);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNameEditText.setText(null);
        mPriceEditText.setText(null);
        mQuantityEditText.setText(null);
        mSupplierNameEditText.setText(null);
        mSupplierPhoneNumberEditText.setText(null);
    }
}