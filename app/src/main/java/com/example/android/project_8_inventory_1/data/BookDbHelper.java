package com.example.android.project_8_inventory_1.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.project_8_inventory_1.data.BookContract.*;

/**
 * Created by Affandy on 15/07/2018.
 */

public class BookDbHelper extends SQLiteOpenHelper {
    // database name
    private static final String DB_NAME = "inventory.db";
    // database version
    private static final int DB_VERSION = 1;
    // query string to create Product table
    private static final String SQL_CREATE_BOOKs_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + "("
            + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + BookEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
            + BookEntry.COLUMN_PRICE + " FLOAT NOT NULL, "
            + BookEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0, "
            + BookEntry.COLUMN_IMAGE_URI + " BLOB DEFAULT NULL, "
            + BookEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
            + BookEntry.COLUMN_SUPLLIER_PHONE_NUMBER + " TEXT NOT NULL DEFAULT 0);";
    // query string to delte Product table
    private static final String SQL_DELETE_BOOKS_TABLE = "DROP TABLE IF EXISTS "
            + BookEntry.TABLE_NAME;
    //constructor to create the database file .db file
    public BookDbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    // create product table for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOKs_TABLE);
    }

    //delete all tables if database version increased
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_BOOKS_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
