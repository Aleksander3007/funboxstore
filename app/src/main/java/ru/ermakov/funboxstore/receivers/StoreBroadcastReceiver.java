package ru.ermakov.funboxstore.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.services.StoreService;

/**
 * BroadcastReceiver для приема данных от StoreService.
 */
public class StoreBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = StoreBroadcastReceiver.class.getSimpleName();

    /**
     * Содержит callbacks, приходящие после обработки запроса в StoreService.
     */
    public interface StoreListener {
        /**
         * Callback после получения продуктов StoreService.
         * @param products полученные продукты.
         */
        void onProductsReceived(List<Product> products);

        /**
         * Callback после обновления продукта.
         * @param oldProduct продукт, который необходимо было изменить.
         * @param newProduct продукт НА который необходимо было изменить.
         */
        void onProductUpdated(Product oldProduct, Product newProduct);

        /**
         * Callback после добавления продукта.
         * @param addedProduct добавленный продукт.
         */
        void onProductAdded(Product addedProduct);

        /**
         * Callback после удаления продукта.
         * @param deletedProduct удаленный продукт.
         */
        void onProductDeleted(Product deletedProduct);

        /**
         * Callback после покупки продукта.
         * @param boughtProduct купленный продукт.
         */
        void onProductBought(Product boughtProduct);
    }
    private StoreListener mListener;

    public StoreBroadcastReceiver(StoreListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // Если нас никто не слушает, то незачем дальше обрабатывать полученные данные.
        if (mListener == null) return;

        switch (intent.getAction()) {
            case StoreService.ACTION_GET_PRODUCTS:
                List<Product> products = intent.getParcelableArrayListExtra(StoreService.EXTRA_PRODUCTS);
                mListener.onProductsReceived(products);
                break;

            case StoreService.ACTION_UPDATE_PRODUCT:
                Product oldProduct = intent.getParcelableExtra(StoreService.EXTRA_OLD_PRODUCT);
                Product newProduct = intent.getParcelableExtra(StoreService.EXTRA_NEW_PRODUCT);
                mListener.onProductUpdated(oldProduct, newProduct);
                break;

            case StoreService.ACTION_ADD_PRODUCT:
                Product addedProduct = intent.getParcelableExtra(StoreService.EXTRA_NEW_PRODUCT);
                mListener.onProductAdded(addedProduct);
                break;

            case StoreService.ACTION_DELETE_PRODUCT:
                Product deletedProduct = intent.getParcelableExtra(StoreService.EXTRA_PRODUCT);
                mListener.onProductDeleted(deletedProduct);
                break;

            case StoreService.ACTION_BUY_PRODUCT:
                Product boughtProduct = intent.getParcelableExtra(StoreService.EXTRA_PRODUCT);
                mListener.onProductBought(boughtProduct);
                break;
        }
    }
}
