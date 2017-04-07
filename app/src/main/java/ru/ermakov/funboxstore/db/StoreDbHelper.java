package ru.ermakov.funboxstore.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ru.ermakov.funboxstore.R;

/**
 * Класс для работы с базой данных.
 * Паттерн Singleton.
 */
public class StoreDbHelper extends SQLiteOpenHelper {

    private static volatile StoreDbHelper sInstance;

    public static final String TAG = StoreDbHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "store.db";
    public static final int DATABASE_VERSION = 1;

    private final List<ContentValues> initProducts = new ArrayList<>();

    public synchronized static StoreDbHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (StoreDbHelper.class) {
                if (sInstance == null)
                    sInstance = new StoreDbHelper(context);
            }
        }
        return sInstance;
    }

    private StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        createInitData(context);
    }

    /**
     * Создать начальные продукты.
     */
    private void createInitData(Context context) {

        String[] productNames = context.getResources().getStringArray(R.array.product_names);
        String[] productCosts = context.getResources().getStringArray(R.array.product_costs);
        int[] productNums = context.getResources().getIntArray(R.array.product_nums);

        for (int iItem = 0; iItem < productNames.length; iItem++) {
            ContentValues itemValues = new ContentValues();
            itemValues.put(ProductEntry.COLUMN_NAME, productNames[iItem]);
            itemValues.put(ProductEntry.COLUMN_COST, productCosts[iItem]);
            itemValues.put(ProductEntry.COLUMN_NUM, productNums[iItem]);
            initProducts.add(itemValues);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_TABLE_ITEM =
                "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                        ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ProductEntry.COLUMN_NAME + " TEXT NOT NULL UNIQUE, " +
                        ProductEntry.COLUMN_COST + " REAL NOT NULL, " +
                        ProductEntry.COLUMN_NUM + " INTEGER NOT NULL"
                + ");";

        db.execSQL(SQL_CREATE_TABLE_ITEM);

        addInitDataInDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Добавить начальные продукты в БД.
     */
    private void addInitDataInDatabase(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (ContentValues item : initProducts) {
                db.insertOrThrow(ProductEntry.TABLE_NAME, null, item);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException sglex) {
            sglex.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
    }
}
