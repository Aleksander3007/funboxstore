package ru.ermakov.funboxstore.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.db.ProductEntry;

/**
 * Адаптирует работу с БД SQLite через целевой интерфейс StoreRepository.
 * Паттерн Адаптер.
 */
public class SQLiteAdapter implements StoreRepository {

    private SQLiteOpenHelper mDbHelper;

    public SQLiteAdapter(SQLiteOpenHelper dbHelper) {
        this.mDbHelper = dbHelper;
    }

    @Override
    public List<Product> getAllData() {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        List<Product> products = new ArrayList<>();
        Cursor dbCursor = db.query(ProductEntry.TABLE_NAME, null, null, null, null, null, null);
        if (dbCursor.moveToFirst()) {
            do {
                String productName = getProductName(dbCursor);
                float productCost = getProductCost(dbCursor);
                int productNum = getProductNum(dbCursor);
                products.add(new Product(productName, productCost, productNum));

            } while (dbCursor.moveToNext());
        }
        dbCursor.close();

        return products;
    }

    @Override
    public Product getProductByName(String productName) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor dbCursor = db.query(ProductEntry.TABLE_NAME, null,
                ProductEntry.COLUMN_NAME + " LIKE " + "'" + productName + "'",
                null, null, null, null);
        if (dbCursor.moveToFirst()) {
            float productCost = getProductCost(dbCursor);
            int productNum = getProductNum(dbCursor);
            dbCursor.close();

            return new Product(productName, productCost, productNum);
        }
        else {
            dbCursor.close();

            return null;
        }
    }

    @Override
    public boolean addProduct(Product product) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(ProductEntry.TABLE_NAME, null, createProductContentValues(product));

        return (rowId != -1);
    }

    @Override
    public boolean updateProduct(Product oldProduct, Product newProduct) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        boolean isUpdated;
        try {
            db.update(ProductEntry.TABLE_NAME,
                    createProductContentValues(newProduct),
                    ProductEntry.COLUMN_NAME + " LIKE " + "'" + oldProduct.getName() + "'", null);
            isUpdated = true;
        }
        catch (SQLiteConstraintException scex) {
            isUpdated = false;
        }

        return isUpdated;
    }

    @Override
    public boolean deleteProduct(Product product) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        boolean isDeleted;
        try {
            db.delete(ProductEntry.TABLE_NAME,
                    ProductEntry.COLUMN_NAME + " LIKE " + "'" + product.getName() + "'", null);
            isDeleted = true;
        }
        catch (SQLiteException scex) {
            isDeleted = false;
        }

        return isDeleted;

    }

    @Override
    public boolean decrementProductNum(Product product, int minNum) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor dbCursor = null;

        boolean isDecremented = false;
        try {
            db.beginTransaction();

            // Запрашиваем текущее значение "кол-во продукта" и если оно больше minNum
            // декрементируем данное значение.

            dbCursor = db.query(ProductEntry.TABLE_NAME,
                    new String[]{ProductEntry.COLUMN_NUM},
                    ProductEntry.COLUMN_NAME + " LIKE " + "'" + product.getName() + "'",
                    null, null, null, null);

            if (dbCursor.moveToFirst()) {

                Product readProduct = new Product(product.getName(),
                        product.getCost(), getProductNum(dbCursor));

                if (readProduct.getNum() > minNum) {
                    readProduct.setNum(readProduct.getNum() - 1);

                    db.update(ProductEntry.TABLE_NAME,
                            createProductContentValues(readProduct),
                            ProductEntry.COLUMN_NAME + " LIKE " + "'" + readProduct.getName() + "'", null);

                    db.setTransactionSuccessful();
                    isDecremented = true;
                }
                // Если количество меньше или равно минимально возможному.
                else isDecremented = false;
            }
            // Такого продукта уже нет.
            else isDecremented = false;
        }
        catch (Exception sqlex) {
            sqlex.printStackTrace();
            isDecremented = false;
        }
        finally {
            if (dbCursor != null) dbCursor.close();
            db.endTransaction();
        }

        return isDecremented;
    }

    private String getProductName(Cursor dbCursor) {
        return dbCursor.getString(dbCursor.getColumnIndex(ProductEntry.COLUMN_NAME));
    }

    private float getProductCost(Cursor dbCursor) {
        return dbCursor.getFloat(dbCursor.getColumnIndex(ProductEntry.COLUMN_COST));
    }

    private int getProductNum(Cursor dbCursor) {
        return dbCursor.getInt(dbCursor.getColumnIndex(ProductEntry.COLUMN_NUM));
    }

    private ContentValues createProductContentValues(Product product) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_NAME, product.getName());
        contentValues.put(ProductEntry.COLUMN_COST, product.getCost());
        contentValues.put(ProductEntry.COLUMN_NUM, product.getNum());

        return contentValues;
    }
}
