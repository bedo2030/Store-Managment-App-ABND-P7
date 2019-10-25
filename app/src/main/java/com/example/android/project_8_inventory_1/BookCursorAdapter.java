package com.example.android.project_8_inventory_1;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.project_8_inventory_1.data.BookContract.BookEntry;

import java.io.ByteArrayInputStream;

/**
 * Created by Affandy on 31/07/2018.
 */

public class BookCursorAdapter extends CursorAdapter {
    private Context mContext;
    private TextView mQuantityTextView;
    public BookCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
        mContext = context;
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
        int imageUriColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_IMAGE_URI);

        String name = cursor.getString(nameColumnIndex);
        float price = cursor.getFloat(priceColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        byte[] bookThumbnailArray = cursor.getBlob(imageUriColumnIndex);

        ImageView bookThumbnailImageView = view.findViewById(R.id.imgView_book_thumbnail);
        if(bookThumbnailArray != null){
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bookThumbnailArray);
            Bitmap bookThumbnail = BitmapFactory.decodeStream(byteArrayInputStream);
            bookThumbnailImageView.setImageBitmap(bookThumbnail);

        }else
        {
            bookThumbnailImageView.setImageResource(R.drawable.default_book);
        }

        TextView nameTextView = view.findViewById(R.id.textView_book_title);
        nameTextView.setText(name);

        TextView priceTextView = view.findViewById(R.id.textView_book_price);
        priceTextView.setText("Price: "+String.valueOf(price) + "$");

        mQuantityTextView = view.findViewById(R.id.textView_book_quantity);
        mQuantityTextView.setText("Quantity: " + String.valueOf(quantity));

        String bookID = cursor.getString(cursor.getColumnIndex(BookEntry._ID));
        final Uri uri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, Long.parseLong(bookID));

        Button saleButton = view.findViewById(R.id.btn_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quantity == 0){
                    Toast.makeText(mContext, "This book doesn't have any units in inventory", Toast.LENGTH_SHORT).show();
                }else{
                    int newQuantity = quantity - 1;
                    mQuantityTextView.setText("Quantity: " + String.valueOf(newQuantity));
                    decreaseQuantity(uri , newQuantity);
                }
            }
        });
    }
    private void decreaseQuantity(Uri uri, int nQuantity){
        ContentValues values = new ContentValues();

        values.put(BookEntry.COLUMN_QUANTITY, nQuantity);
        String selection = BookEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        int updatedRows = mContext.getContentResolver().update(uri, values, selection, selectionArgs);
        if(updatedRows > 0){
            Toast.makeText(mContext, "Quantity decreased by 1", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(mContext, "Error Quantity didn't decrease by 1", Toast.LENGTH_SHORT).show();
        }
    }
}
