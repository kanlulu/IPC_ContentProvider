### [Android 进程间通信——ContentProvider](https://blog.csdn.net/qq_36046305/article/details/84987765)

在前面的文章[Android进程间通信机制之AIDL](https://blog.csdn.net/qq_36046305/article/details/84769536)中我们简单的介绍了AIDL的使用。在Android中我们还可以使用其他的进程间通信方式：

#### 使用ContentProvider进行进程间通信

ContentProvider的底层也是用Binder实现的。

通过服务端的ContentProvider建立数据库，然后向外提供接口，在客户端使用getContentResolver()获取到的ContentResolver去对数据库进行增删改查的操作，以此来实现进程间的通信。

代码实现：

**1. 继承SQLiteOpenHelper创建DbHelper类**

ContentProvider与ContentResolver之间的通信其实是通过操作数据表实现的，所以先创建一个DBHelper类。

```java
public class ProviderDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ipc_provider.db";
    private static final String TABLE_NAME = "book";
    private static final int DB_VERSION = 1;

    private final String SQL_CREATE_TABLE = "create table if not exists " + TABLE_NAME + " (_id integer primary key, name TEXT, description TEXT)";

    public ProviderDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

```



**2. 继承ContentProvider创建自定义Provider**

ContentProvider使用Uri标识数据，它包括两个部分：

1. anthority：符号名称
2. path：指向表的路径/名称

内容Uri的形式为：

`content://authority/path`

在我们使用ContentResolver方法来访问ContentProvider的数据表时，需要传递URI来区分要操作的内容。Android中提供了UriMatcher来解析Uri。

UriMatcher用来匹配Uri：

> 1. 注册需要匹配的Uri路径，可以添加匹配码；
> 2. 使用uriMatcher.match(uri)方法对输入的uri进行匹配，使用返回的匹配码进行匹配。

```java
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

```

ContentProvider是Android中的四大组件之一，我们还需要在AndroidManifest.xml文件中声明provider标签：

```xml
<provider
            android:name=".provider.IPCProvider"
            android:authorities="com.kanlulu.ipc_contentprovider.provider.IPCProvider"
            android:exported="false"
            android:grantUriPermissions="true"
            android:process=":ipc_provider" />
```

**3. 在其他进程中通过getContentResolver对数据表进行增删改查**

```java
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

```

如此便完成了使用ContentProvider进行进程间的通信。

