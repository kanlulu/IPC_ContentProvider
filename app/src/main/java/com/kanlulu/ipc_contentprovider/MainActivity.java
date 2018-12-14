package com.kanlulu.ipc_contentprovider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kanlulu.ipc_contentprovider.provider.IPCProvider;

public class MainActivity extends AppCompatActivity {

    public TextView mQueryResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mQueryResult = (TextView) findViewById(R.id.tv_query_result);
    }

    public void insert(View view) {
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        int id = (int) (Math.random() * 100);
        contentValues.put("_id", id);
        contentValues.put("name", "book-name-" + id);
        contentValues.put("description", "book-description-" + id);
        contentResolver.insert(IPCProvider.uri, contentValues);
    }

    public void query(View view) {
        mQueryResult.setText("");
        StringBuilder sb = new StringBuilder();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(IPCProvider.uri, new String[]{"name", "description"}, null, null, null);
        if (cursor == null) return;
        while (cursor.moveToNext()) {
            String result = cursor.getString(0) + " === " + cursor.getString(1);
            Log.e("debug", result);
            sb.append(result).append("\n");
        }
        mQueryResult.setText(sb.toString());
        cursor.close();
    }
}
