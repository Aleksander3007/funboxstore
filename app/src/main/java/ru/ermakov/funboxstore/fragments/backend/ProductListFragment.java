package ru.ermakov.funboxstore.fragments.backend;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.ermakov.funboxstore.receivers.StoreBroadcastReceiver;
import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.R;
import ru.ermakov.funboxstore.adapters.ProductRecyclerViewAdapter;
import ru.ermakov.funboxstore.services.StoreService;

/**
 * Фрагмент для работы со списком товаров.
 */
public class ProductListFragment extends Fragment
        implements ProductRecyclerViewAdapter.OnProductClickListener,
        StoreBroadcastReceiver.StoreListener,
        SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = ProductListFragment.class.getSimpleName();

    @BindView(R.id.rv_products) RecyclerView mProductsRecyclerView;
    @BindView(R.id.srl_products_refresh) SwipeRefreshLayout mProductsSwipeRefreshLayout;

    private Unbinder mUnbinder;
    private ProductRecyclerViewAdapter mProductAdapter;
    private BroadcastReceiver mStoreBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mProductAdapter = new ProductRecyclerViewAdapter(null, this);
        registerStoreBroadcastReceiver();
        startGetProductsService();
        hideActionBar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_backend_product_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mProductsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mProductsRecyclerView.setAdapter(mProductAdapter);
        addItemTouchHelperToRecyclerView();

        mProductsSwipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mStoreBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_product) {
            openCreateProductWindow();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onProductClick(Product product) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.fragment_container, ProductDetailFragment.newInstance(product))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onProductsReceived(List<Product> products) {
        mProductAdapter.setProducts(products);
        if (mProductsSwipeRefreshLayout != null) mProductsSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onProductUpdated(Product oldProduct, Product newProduct) {
        mProductAdapter.replaceProduct(oldProduct, newProduct);
    }

    @Override
    public void onProductAdded(Product addedProduct) {
        mProductAdapter.addProduct(addedProduct);
    }

    @Override
    public void onProductDeleted(Product deletedProduct) {
        // Если deletedProduct != null, то значит продукт успешно удален из хранилища,
        // а здесь во фрагменте он удален уже до этого, чтобы пользователь не мог его редактировать.
    }

    @Override
    public void onProductBought(Product boughtProduct) {
        if (boughtProduct != null) {
            mProductAdapter.replaceProduct(boughtProduct, boughtProduct);
        }
    }

    /**
     * Относится к SwipeRefreshLayout.
     */
    @Override
    public void onRefresh() {
        startGetProductsService();
    }


    private void hideActionBar() {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
            if (actionBar != null && !actionBar.isShowing()) {
                actionBar.setShowHideAnimationEnabled(false);
                actionBar.show();
            }
        }
    }

    /**
     * Регистрируем BroadcastReceiver для получения оповещений от StoreService.
     */
    private void registerStoreBroadcastReceiver() {
         mStoreBroadcastReceiver = new StoreBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StoreService.ACTION_GET_PRODUCTS);
        intentFilter.addAction(StoreService.ACTION_UPDATE_PRODUCT);
        intentFilter.addAction(StoreService.ACTION_ADD_PRODUCT);
        intentFilter.addAction(StoreService.ACTION_DELETE_PRODUCT);
        intentFilter.addAction(StoreService.ACTION_BUY_PRODUCT);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mStoreBroadcastReceiver, intentFilter);
    }

    /**
     * Запуск StoreService для получения продуктов.
     */
    private void startGetProductsService() {
        Intent intent = new Intent(getContext(), StoreService.class);
        intent.setAction(StoreService.ACTION_GET_PRODUCTS);
        getContext().startService(intent);
    }

    /**
     * Запуск StoreService для удаления указанного продукта.
     */
    private void startDeleteProductService(Product product) {
        Intent intent = new Intent(getContext(), StoreService.class);
        intent.setAction(StoreService.ACTION_DELETE_PRODUCT);
        intent.putExtra(StoreService.EXTRA_PRODUCT, product);
        getContext().startService(intent);
    }

    /**
     * Открыть окно добавления нового продукта.
     */
    private void openCreateProductWindow() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction
                .replace(R.id.fragment_container, new ProductDetailFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Добавить ItemTouchHelper к RecyclerView.
     */
    private void addItemTouchHelperToRecyclerView() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Product product = mProductAdapter.removeProduct(viewHolder.getAdapterPosition());
                startDeleteProductService(product);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mProductsRecyclerView);
    }
}
