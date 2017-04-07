package ru.ermakov.funboxstore.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StoreDbHelperTest {

    private final Context mContext = InstrumentationRegistry.getTargetContext();

    @Before
    public void before() {
        mContext.deleteDatabase(StoreDbHelper.DATABASE_NAME);
    }

    @After
    public void after() {
        mContext.deleteDatabase(StoreDbHelper.DATABASE_NAME);
    }

    @Test
    public void testCreateDatabase() {
        StoreDbHelper dbHelper = StoreDbHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assertTrue(db.isOpen());
        db.close();
    }

    @Test
    public void testExistColumnsDatabse() {

        final String testName = "testName";
        final float testCost = 12_000.25f;
        final int testNum = 5;

        StoreDbHelper dbHelper = StoreDbHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues testValues = new ContentValues();
            testValues.put(ProductEntry.COLUMN_NAME, testName);
            testValues.put(ProductEntry.COLUMN_COST, testCost);
            testValues.put(ProductEntry.COLUMN_NUM, testNum);

            long rowId = db.insert(ProductEntry.TABLE_NAME, null, testValues);
            assertNotEquals(-1, rowId);

            int deleteCount = db.delete(ProductEntry.TABLE_NAME,
                    ProductEntry.COLUMN_NAME + " LIKE " + "'" + testName+ "'" + " AND " +
                            ProductEntry.COLUMN_COST + " = " + testCost + " AND " +
                            ProductEntry.COLUMN_NUM + " = " + testNum + "",
                    null);

            assertEquals(1, deleteCount);
        }
        finally {
            db.close();
        }
    }

    @Test
    public void testUniqueName() {
        final String testName = "testName";
        final float testCost = 12_000.25f;
        final int testNum = 5;

        StoreDbHelper dbHelper = StoreDbHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues testValues = new ContentValues();
            testValues.put(ProductEntry.COLUMN_NAME, testName);
            testValues.put(ProductEntry.COLUMN_COST, testCost);
            testValues.put(ProductEntry.COLUMN_NUM, testNum);

            long rowId = db.insert(ProductEntry.TABLE_NAME, null, testValues);
            assertNotEquals(-1, rowId);

            // Добавляем второй раз с тем же значением для COLUMN_NAME,
            // должны получить ошибку, т.к. этот столбец должен содержать только уникальные данные.
            rowId = db.insert(ProductEntry.TABLE_NAME, null, testValues);
            assertEquals(-1, rowId);
        }
        finally {
            db.close();
        }
    }

}