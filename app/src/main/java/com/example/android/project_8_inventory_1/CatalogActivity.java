package com.example.android.project_8_inventory_1;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.project_8_inventory_1.data.BookContract.BookEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private ListView mListView;
    private BookCursorAdapter mBookCursorAdapter;
    private static final String [] PROJECTION = {BookEntry._ID, BookEntry.COLUMN_BOOK_NAME,
            BookEntry.COLUMN_PRICE, BookEntry.COLUMN_QUANTITY, BookEntry.COLUMN_IMAGE_URI};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        mListView = findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        mListView.setEmptyView(emptyView);

        mBookCursorAdapter = new BookCursorAdapter(this, null);
        mListView.setAdapter(mBookCursorAdapter);

        getLoaderManager().initLoader(0,null,this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);
                intent.setData(ContentUris.withAppendedId(BookEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, BookEntry.CONTENT_URI, PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBookCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBookCursorAdapter.swapCursor(null);
    }
}
