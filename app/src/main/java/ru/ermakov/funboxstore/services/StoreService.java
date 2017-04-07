package ru.ermakov.funboxstore.services;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.ermakov.funboxstore.R;
import ru.ermakov.funboxstore.adapters.SQLiteAdapter;
import ru.ermakov.funboxstore.adapters.StoreRepository;
import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.db.StoreDbHelper;

/**
 * Класс реализующий Service для асинхронного доступа к хранилищу продуктов.
 */
public class StoreService extends Service {

    public static final String TAG = StoreService.class.getSimpleName();

    // По заданию Task 3 редактирование занимает 5 сек!
    private static final int TASK_3_DELAY_UPDATE_PRODUCT = 5000;
    // По заданию Task 3 покупка занимает 3 сек!
    private static final int TASK_3_DELAY_BUY_PRODUCT = 3000;

    public static final String ACTION_GET_PRODUCTS = "ru.ermakov.funboxstore.ACTION_GET_PRODUCTS";
    public static final String ACTION_UPDATE_PRODUCT = "ru.ermakov.funboxstore.ACTION_UPDATE_PRODUCT";
    public static final String ACTION_ADD_PRODUCT = "ru.ermakov.funboxstore.ACTION_ADD_PRODUCT";
    public static final String ACTION_DELETE_PRODUCT = "ru.ermakov.funboxstore.ACTION_DELETE_PRODUCT";
    public static final String ACTION_BUY_PRODUCT = "ru.ermakov.funboxstore.ACTION_BUY_PRODUCT";

    public static final String EXTRA_PRODUCTS = "EXTRA_PRODUCTS";
    public static final String EXTRA_PRODUCT = "EXTRA_PRODUCT";
    public static final String EXTRA_OLD_PRODUCT = "EXTRA_OLD_PRODUCT";
    public static final String EXTRA_NEW_PRODUCT = "EXTRA_NEW_PRODUCT";

    private ExecutorService mExecutorService;
    /** Handler на главный поток, для отправки Toast. */
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(getMainLooper());
        mExecutorService = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            switch (intent.getAction()) {
                // Получить продукты.
                case ACTION_GET_PRODUCTS:

                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            requestGetProducts();
                        }
                    });
                    break;

                // Обновить информацию о продукте.
                case ACTION_UPDATE_PRODUCT:

                    final Product oldProduct = intent.getParcelableExtra(EXTRA_OLD_PRODUCT);
                    final Product newProduct = intent.getParcelableExtra(EXTRA_NEW_PRODUCT);
                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // По заданию Task 3 редактирование занимает 5 сек!
                                Thread.sleep(TASK_3_DELAY_UPDATE_PRODUCT);
                                requestUpdateProduct(oldProduct, newProduct);
                            }
                            catch (InterruptedException iex) {/* Если прервали, просто завершаем поток. */}
                        }
                    });
                    break;

                // Добавить новый продукт.
                case ACTION_ADD_PRODUCT:

                    final Product productForAdd = intent.getParcelableExtra(EXTRA_NEW_PRODUCT);
                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // По заданию Task 3 редактирование занимает 5 сек!
                                Thread.sleep(TASK_3_DELAY_UPDATE_PRODUCT);
                                requestAddProduct(productForAdd);
                            }
                            catch (InterruptedException iex) {/* Если прервали, просто завершаем поток. */}
                        }
                    });
                    break;

                // Удалить продукт.
                case ACTION_DELETE_PRODUCT:

                    final Product productForDelete = intent.getParcelableExtra(EXTRA_PRODUCT);
                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // По заданию Task 3 редактирование занимает 5 сек!
                                Thread.sleep(TASK_3_DELAY_UPDATE_PRODUCT);
                                requestDeleteProduct(productForDelete);
                            }
                            catch (InterruptedException iex) {/* Если прервали, просто завершаем поток. */}
                        }
                    });

                    break;

                // Купить продукт.
                case ACTION_BUY_PRODUCT:

                    final Product productForBuy = intent.getParcelableExtra(EXTRA_PRODUCT);
                    mExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // По заданию Task 3 покупка занимает 3 сек!
                                Thread.sleep(TASK_3_DELAY_BUY_PRODUCT);
                                requestBuyProduct(productForBuy);
                            }
                            catch (InterruptedException iex) {/* Если прервали, просто завершаем поток. */}
                        }
                    });
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Запрос на получение продуктов.
     */
    private void requestGetProducts() {

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(getApplicationContext());
        StoreRepository repo = new SQLiteAdapter(dbHelper);
        ArrayList<Product> products = (ArrayList<Product>) repo.getAllData();

        Intent sendProductIntent = new Intent(ACTION_GET_PRODUCTS);
        sendProductIntent.putParcelableArrayListExtra(EXTRA_PRODUCTS, products);

        LocalBroadcastManager.getInstance(this).sendBroadcast(sendProductIntent);
    }

    /**
     * Запрос на обновление данных о продукте.
     * @param oldProduct продукт который необходимо изменить.
     * @param newProduct измененная информация о продукте.
     */
    private void requestUpdateProduct(Product oldProduct, Product newProduct) {

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(getApplicationContext());
        StoreRepository repo = new SQLiteAdapter(dbHelper);

        Intent updateProductIntent = new Intent(ACTION_UPDATE_PRODUCT);
        updateProductIntent.putExtra(EXTRA_OLD_PRODUCT, oldProduct);

        boolean isUpdated = repo.updateProduct(oldProduct, newProduct);
        if (isUpdated) {
            updateProductIntent.putExtra(EXTRA_NEW_PRODUCT, newProduct);

            showToast(getResources().getString(R.string.success_update_product));
        }
        else {
            // Если не удалось обновить данные, то в качестве новых данных выставляем старые.
            updateProductIntent.putExtra(EXTRA_NEW_PRODUCT, oldProduct);

            showToast(getResources().getString(R.string.error_update_product));
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(updateProductIntent);
    }

    /**
     * Запрос на добавление продукта.
     * @param product продукт, который необходимо добавить.
     */
    private void requestAddProduct(Product product) {

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(getApplicationContext());
        StoreRepository repo = new SQLiteAdapter(dbHelper);

        boolean isAdded = repo.addProduct(product);
        if (isAdded) {
            Intent addProductIntent = new Intent(ACTION_ADD_PRODUCT);
            addProductIntent.putExtra(EXTRA_NEW_PRODUCT, product);
            LocalBroadcastManager.getInstance(this).sendBroadcast(addProductIntent);

            showToast(getResources().getString(R.string.success_add_product));
        }
        else {
            showToast(getResources().getString(R.string.error_add_product));
        }
    }

    /**
     * Запрос на удаление продукта.
     * @param product продукт, который необходимо удалить.
     */
    private void requestDeleteProduct(Product product) {

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(getApplicationContext());
        StoreRepository repo = new SQLiteAdapter(dbHelper);

        boolean isDeleted = repo.deleteProduct(product);
        if (isDeleted) {
            Intent deleteProductIntent = new Intent(ACTION_DELETE_PRODUCT);
            deleteProductIntent.putExtra(EXTRA_PRODUCT, product);
            LocalBroadcastManager.getInstance(this).sendBroadcast(deleteProductIntent);

            showToast(getResources().getString(R.string.success_delete_product));
        }
        else {
            showToast(getResources().getString(R.string.error_delete_product));
        }

    }

    private void requestBuyProduct(Product product) {

        SQLiteOpenHelper dbHelper = StoreDbHelper.getInstance(getApplicationContext());
        StoreRepository repo = new SQLiteAdapter(dbHelper);

        boolean isBuy = repo.decrementProductNum(product, 0);
        if (isBuy) {
            showToast(getResources().getString(R.string.success_buy_product));
        }
        else {
            showToast(getResources().getString(R.string.error_buy_product));
        }

        // Считываем получившийся продукт после изменения.
        Product modifProduct = repo.getProductByName(product.getName());

        Intent intent = new Intent(ACTION_BUY_PRODUCT);
        intent.putExtra(EXTRA_PRODUCT, modifProduct);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void showToast(final CharSequence toastMsg) {

        // Отображание Toast должно быть в главном потоке приложения.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
