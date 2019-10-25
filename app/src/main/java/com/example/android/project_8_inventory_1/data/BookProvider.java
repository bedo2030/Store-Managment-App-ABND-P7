package com.example.android.project_8_inventory_1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.project_8_inventory_1.data.BookContract.BookEntry;

import static android.R.attr.id;

/**
 * Created by Affandy on 02/08/2018.
 */

public class BookProvider extends ContentProvider{
    /** Tag for the log messages */
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    public static final int BOOKS = 100;
    public static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS+"/#", BOOK_ID);
    }

    // Database helper object.
    private BookDbHelper mBookDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */

    @Override
    public boolean onCreate() {
        mBookDbHelper = new BookDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mBookDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match){
            case BOOKS:
                cursor = db.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(BookEntry.TABLE_NAME,projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a book into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues values) {
        String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        float price = values.getAsFloat(BookEntry.COLUMN_PRICE);
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
        String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
        String supplierPhoneNumber = values.getAsString(BookEntry.COLUMN_SUPLLIER_PHONE_NUMBER);

        if(TextUtils.isEmpty(name)){
            throw new IllegalArgumentException("Book Name Required");
        }else if(quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Wrong Quantity");
        }else if(TextUtils.isEmpty(supplierName)){
            throw new IllegalArgumentException("Supplier name Required");
        }else if(TextUtils.isEmpty(supplierPhoneNumber)){
            throw new IllegalArgumentException("Supplier Phone Number Required");
        }
        // TODO: Insert a new pet into the pets database table with the given ContentValues
        SQLiteDatabase db = mBookDbHelper.getWritableDatabase();
        long insertedDataFlag = db.insert(BookEntry.TABLE_NAME, null, values);
        if(insertedDataFlag < 0){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsDeleted = 0;
        SQLiteDatabase db = mBookDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match){
            case BOOKS:
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(BookEntry.TABLE_NAME, selection,selectionArgs);
                if(rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case BOOKS:
                return updateBook(uri, values, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String [] selectionArgs){
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        /*String name = values.containsKey(PetEntry.COLUMN_NAME) ? values.getAsString(PetEntry.COLUMN_NAME):"not";
        Integer gender = values.containsKey(PetEntry.COLUMN_GENDER) ? values.getAsInteger(PetEntry.COLUMN_GENDER):3;
        Integer weight = values.containsKey(PetEntry.COLUMN_WEIGHT) ? values.getAsInteger(PetEntry.COLUMN_WEIGHT):0;

        if(!name.equals("not")){
            if(TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Pet Requires a name");
            }
        }else if(gender != 3) {
            if(gender == null || (gender != PetEntry.GENDER_UNKNOW && gender != PetEntry.GENDER_MALE && gender != PetEntry.GENDER_FEMALE)) {
                throw new IllegalArgumentException("Pet Requires a gender");
            }
        }else if(weight != 0){
            if(weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet Requires a weight");
            }
        }*/

        SQLiteDatabase db = mBookDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(BookEntry.TABLE_NAME,values,selection,selectionArgs);
        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
