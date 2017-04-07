package ru.ermakov.funboxstore.db;

import android.provider.BaseColumns;

/**
 * Класс который определяет содержимое таблицы Item.
 */
public class ProductEntry implements BaseColumns {

    public static final String TABLE_NAME = "product";

    /** Наименование товара. */
    public static final String COLUMN_NAME = "name";
    /** Стоимость товара. */
    public static final String COLUMN_COST = "cost";
    /** Количество товара. */
    public static final String COLUMN_NUM = "num";
}
