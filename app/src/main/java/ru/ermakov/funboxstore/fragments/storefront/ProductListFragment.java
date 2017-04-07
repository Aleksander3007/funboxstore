package ru.ermakov.funboxstore.fragments.storefront;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.R;
import ru.ermakov.funboxstore.adapters.ProductViewPagerAdapter;
import ru.ermakov.funboxstore.receivers.StoreBroadcastReceiver;
import ru.ermakov.funboxstore.services.StoreService;

/**
 * Фрагмент для отображения доступных товаров клиенту.
 */
public class ProductListFragment extends Fragment
        implements ProductViewPagerAdapter.ProductViewPagerAdapterListener,
        StoreBroadcastReceiver.StoreListener {

    public static final String TAG = ProductListFragment.class.getSimpleName();

    @BindView(R.id.vp_products) ViewPager mProductsViewPager;

    private Unbinder mUnbinder;
    private ProductViewPagerAdapter mPagerAdapter;
    private BroadcastReceiver mStoreReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        registerStoreBroadcastReceiver();
        hideActionBar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storefront_product_list, container, false);
        mUnbinder= ButterKnife.bind(this, view);

        mPagerAdapter = new ProductViewPagerAdapter(getFragmentManager(), null, this);
        mProductsViewPager.setAdapter(mPagerAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startGetProductsService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mStoreReceiver);
        super.onDestroy();
    }

    @Override
    public void onBuyProduct(Product product) {
        Intent intent = new Intent(getContext(), StoreService.class);
        intent.setAction(StoreService.ACTION_BUY_PRODUCT);
        intent.putExtra(StoreService.EXTRA_PRODUCT, product);
        getContext().startService(intent);
    }

    @Override
    public void onProductsReceived(List<Product> products) {
        if (products != null && mProductsViewPager != null) {
            mPagerAdapter.setProducts(products);
        }
    }

    @Override
    public void onProductUpdated(Product oldProduct, Product newProduct) {
        if (mProductsViewPager != null) {
            mPagerAdapter.replaceProduct(oldProduct, newProduct);
        }
    }

    @Override
    public void onProductAdded(Product addedProduct) {
        if (mProductsViewPager != null) {
            mPagerAdapter.addProduct(addedProduct);
        }
    }

    @Override
    public void onProductDeleted(Product deletedProduct) {
        if (deletedProduct != null && mProductsViewPager != null) {
            mPagerAdapter.removeProduct(deletedProduct);
        }
    }

    @Override
    public void onProductBought(Product boughtProduct) {
        if (boughtProduct != null && mProductsViewPager != null) {
            // Обновляем информацию о купленном товаре после покупки.
            mPagerAdapter.replaceProduct(boughtProduct, boughtProduct);
        }
    }

    private void hideActionBar() {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (actionBar != null && actionBar.isShowing()) {
                actionBar.setShowHideAnimationEnabled(false);
                actionBar.hide();
            }
        }
    }

    /**
     * Регистрируем BroadcastReceiver для получения оповещений от StoreService.
     */
    private void registerStoreBroadcastReceiver() {
        mStoreReceiver = new StoreBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StoreService.ACTION_GET_PRODUCTS);
        intentFilter.addAction(StoreService.ACTION_UPDATE_PRODUCT);
        intentFilter.addAction(StoreService.ACTION_ADD_PRODUCT);
        intentFilter.addAction(StoreService.ACTION_DELETE_PRODUCT);
        intentFilter.addAction(StoreService.ACTION_BUY_PRODUCT);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mStoreReceiver, intentFilter);
    }

    /**
     * Запуск StoreService для получения продуктов.
     */
    private void startGetProductsService() {
        Intent intent = new Intent(getContext(), StoreService.class);
        intent.setAction(StoreService.ACTION_GET_PRODUCTS);
        getContext().startService(intent);
    }
}
