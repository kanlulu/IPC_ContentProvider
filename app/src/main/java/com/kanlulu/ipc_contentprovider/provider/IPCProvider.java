package com.kanlulu.ipc_contentprovider.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kanlulu.ipc_contentprovider.database.ProviderDbHelper;

/**
 * Created by kanlulu
 * DATE: 2018/12/13 11:14
 */
public class IPCProvider extends ContentProvider {
    //ContentProvider 的授权字符串
    public static final String AUTHORITY = "com.kanlulu.ipc_contentprovider.provider.IPCProvider";
    // 内容 URI 用于在 ContentProvider 中标识数据的 URI，可以使用 content:// + authority 作为 ContentProvider 的 URI
    public static final Uri uri = Uri.parse("content://" + AUTHORITY + "/book");

    //在 ContentProvider 中可以通过 UriMatcher 来为不同的 URI 关联不同的 code，便于后续根据 URI 找到对应的表
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int CODE_BOOK = 1;

    static {
        uriMatcher.addURI(AUTHORITY, "book", CODE_BOOK);
    }

    public Context mContext;
    public ProviderDbHelper dbHelper;
    public SQLiteDatabase mDatabase;
    public String mTableName;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        initProvider();
        return false;
    }

    private void initProvider() {
        mTableName = ProviderDbHelper.TABLE_NAME;
        dbHelper = new ProviderDbHelper(mContext);
        mDatabase = dbHelper.getWritableDatabase();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mDatabase.execSQL("delete from " + mTableName);
                mDatabase.execSQL("insert into " + mTableName + " values(1,'test_book_name','test_book_desc')");
            }
        }).start();

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String tableName = getTableName(uri);
        Log.e("debug", tableName + " 查询数据");
        return mDatabase.query(tableName, projection, selection, selectionArgs, null, sortOrder, null);
    }

    private String getTableName(Uri uri) {
        String tableName = "";
        int match = uriMatcher.match(uri);
        switch (match) {
            case CODE_BOOK:
                tableName = ProviderDbHelper.TABLE_NAME;
        }
        return tableName;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String tableName = getTableName(uri);
        Log.e("debug", tableName + " 插入数据");
        mDatabase.insert(tableName, null, values);
        mContext.getContentResolver().notifyChange(uri, null);
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableName(uri);
        Log.e("debug", tableName + " 删除数据");
        int deleteCount = mDatabase.delete(tableName, selection, selectionArgs);
        if (deleteCount > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableName(uri);
        int updateCount = mDatabase.update(tableName, values, selection, selectionArgs);
        if (updateCount > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }
}
