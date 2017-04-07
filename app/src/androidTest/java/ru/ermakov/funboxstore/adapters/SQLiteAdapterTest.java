package ru.ermakov.funboxstore.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.db.ProductEntry;
import ru.ermakov.funboxstore.db.StoreDbHelper;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SQLiteAdapterTest {

    private final Context mContext = InstrumentationRegistry.getTargetContext();

    @Before
    public void before() {
        mContext.deleteDatabase(StoreDbHelper.DATABASE_NAME);
    }

    @After
    public void after() {
        mContext.deleteDatabase(StoreDbHelper.DATABASE_NAME);
    }

    /**
     * Проверяем нормальную работу обновления данных о продуктах.
     */
    @Test
    public void testUpdateProductOk() throws Exception {

        final Product oldTestProduct = createProduct_Old();
        final Product newTestProduct = createProduct_New();

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(mContext);
        insertProduct(dbHelper, oldTestProduct);

        SQLiteAdapter repo = new SQLiteAdapter(dbHelper);
        boolean isUpdated = repo.updateProduct(oldTestProduct, newTestProduct);
        assertTrue(isUpdated);
    }

    @Test
    public void testAddProductOk() throws Exception {

        final Product testProduct = createProduct_1();

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(mContext);
        SQLiteAdapter repo = new SQLiteAdapter(dbHelper);
        boolean isAdded = repo.addProduct(testProduct);
        assertTrue(isAdded);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(ProductEntry.TABLE_NAME, null,
                ProductEntry.COLUMN_NAME + " LIKE " + "'" + testProduct.getName()  + "'",
                null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                String readProductName = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME));
                float readProductCost = cursor.getFloat(cursor.getColumnIndex(ProductEntry.COLUMN_COST));
                int readProductNum = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_NUM));

                assertEquals(testProduct.getName(), readProductName);
                assertEquals(testProduct.getCost(), readProductCost, 0.1);
                assertEquals(testProduct.getNum(), readProductNum);
            }
            else {
                Assert.fail();
            }
        }
        finally {
            cursor.close();
            db.close();
            deleteProduct(dbHelper, testProduct);
        }
    }

    /**
     * Проверяем работу добавления продукта, когда новое имя совпадает с именем одного из продуктов.
     */
    @Test
    public void testAddProductError() throws Exception {
        final Product testProduct = createProduct_1();

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(mContext);

        insertProduct(dbHelper, testProduct);

        // Добавляем продукт с тем же именем.
        SQLiteAdapter repo = new SQLiteAdapter(dbHelper);
        boolean isAdded = repo.addProduct(testProduct);
        assertFalse(isAdded);

        deleteProduct(dbHelper, testProduct);
    }

    /**
     * Проверяем работу обновления данных, когда новое имя совпадает с именем одного из продуктов.
     */
    @Test
    public void testUpdateProductError() throws Exception {

        final Product testProduct = createProduct_1();
        final Product oldTestProduct = createProduct_Old();
        final Product newTestProduct = createProduct_New();

        // Устанавливаем совпадающее имя с другим продуктом.
        newTestProduct.setName(testProduct.getName());

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(mContext);
        insertProduct(dbHelper, testProduct);
        insertProduct(dbHelper, oldTestProduct);

        SQLiteAdapter repo = new SQLiteAdapter(dbHelper);
        boolean isUpdated = repo.updateProduct(oldTestProduct, newTestProduct);
        // Проверяем, что не удалось вставить продукт с тем же именем.
        assertFalse(isUpdated);

        // Удаляем созданные продукты.
        deleteProduct(dbHelper, testProduct);
        deleteProduct(dbHelper, oldTestProduct);
    }

    @Test
    public void testDeleteProductOk() throws Exception {

        final Product testProduct = createProduct_1();

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(mContext);
        insertProduct(dbHelper, testProduct);

        SQLiteAdapter repo = new SQLiteAdapter(dbHelper);
        boolean isDeleted = repo.deleteProduct(testProduct);
        assertTrue(isDeleted);

        dbHelper.getWritableDatabase().close();
    }

    /**
     * Добавить продукт в БД.
     * @param dbHelper экземпляр типа {@link SQLiteOpenHelper}.
     * @param product продукт который необходимо добавить.
     */
    private void insertProduct(SQLiteOpenHelper dbHelper, Product product) {

        ContentValues valuesForInsert = new ContentValues();
        valuesForInsert.put(ProductEntry.COLUMN_NAME, product.getName());
        valuesForInsert.put(ProductEntry.COLUMN_COST, product.getCost());
        valuesForInsert.put(ProductEntry.COLUMN_NUM, product.getNum());

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long rowId = db.insert(ProductEntry.TABLE_NAME, null, valuesForInsert);
        assertNotEquals(-1, rowId);

        db.close();
    }

    /**
     * Удалить продукт из БД.
     * @param dbHelper экземпляр типа {@link SQLiteOpenHelper}.
     * @param product продукт который необходимо добавить.
     */
    private void deleteProduct(SQLiteOpenHelper dbHelper, Product product) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int nDeletedRow = db.delete(ProductEntry.TABLE_NAME,
                ProductEntry.COLUMN_NAME + " LIKE " + "'" + product.getName() + "'", null);
        assertEquals(1, nDeletedRow);

        db.close();
    }

    private Product createProduct_1() {
        final String oldTestName = "product1";
        final float oldTestCost = 1.1f;
        final int oldTestNum = 1;
        return new Product(oldTestName, oldTestCost, oldTestNum);
    }

    private Product createProduct_Old() {
        final String oldTestName = "oldTestName";
        final float oldTestCost = 123.4f;
        final int oldTestNum = 5;
        return new Product(oldTestName, oldTestCost, oldTestNum);
    }

    private Product createProduct_New() {
        final String newTestName = "newTestName";
        final float newTestCost = 678.9f;
        final int newTestNum = 0;
        return new Product(newTestName, newTestCost, newTestNum);
    }
}