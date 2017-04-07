package ru.ermakov.funboxstore.fragments.backend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.R;
import ru.ermakov.funboxstore.services.StoreService;

/**
 * Фрагмент для редактирования конкретного товара.
 */
public class ProductDetailFragment extends Fragment {

    public static final String TAG = ProductDetailFragment.class.getSimpleName();

    public static final String ARG_PRODUCT = "ARG_PRODUCT";

    @BindView(R.id.et_product_name) EditText mProductNameEditText;
    @BindView(R.id.et_product_cost) EditText mProductCostEditText;
    @BindView(R.id.et_product_num) EditText mProductNumEditText;

    private Unbinder mUnbinder;

    /** Добавляется новый продукт. true - если добавляем новый продукт. */
    private boolean isNewProduct;

    public static ProductDetailFragment newInstance(Product product) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_PRODUCT, product);

        ProductDetailFragment fragment = new ProductDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_backend_product_detail, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getArguments() != null && getArguments().containsKey(ARG_PRODUCT)) {
            isNewProduct = false;
            Product product = getArguments().getParcelable(ARG_PRODUCT);
            mProductNameEditText.setText(product.getName());
            mProductCostEditText.setText(String.valueOf(product.getCost()));
            mProductNumEditText.setText(String.valueOf(product.getNum()));
        }
        else {
            isNewProduct = true;
            // Запрос фокуса и отображение soft keyboard принудительно.
            mProductNameEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }

        return view;
    }

    @Override
    public void onDestroyView() {

        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        super.onDestroyView();

        mUnbinder.unbind();
        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null)
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_item_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_product:
                saveProduct();
                return true;
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Сохранить продукт.
     */
    private void saveProduct() {
        Product product = getProduct();
        if (product != null) {
            if (isNewProduct) {
                addProductToRepo(product);
                isNewProduct = false;
            }
            // Если не новый, обновляем данные.
            else {
                Product oldProductData = getArguments().getParcelable(ARG_PRODUCT);
                updateProductInRepo(oldProductData, product);
            }
        }
    }

    /**
     * Добавить продукт в хранилище.
     * @param product продукт, который необходимо добавить.
     */
    private void addProductToRepo(Product product) {

        Intent addProductIntent = new Intent(getContext(), StoreService.class);
        addProductIntent.setAction(StoreService.ACTION_ADD_PRODUCT);
        addProductIntent.putExtra(StoreService.EXTRA_NEW_PRODUCT, product);

        getContext().startService(addProductIntent);
    }

    /**
     * Обновить информацию о продукте в хранилище.
     * @param oldProduct продукт который необходимо обновить.
     * @param newProduct новая информация о продукте.
     */
    private void updateProductInRepo(Product oldProduct, Product newProduct) {

        Intent updateProductIntent = new Intent(getContext(), StoreService.class);
        updateProductIntent.setAction(StoreService.ACTION_UPDATE_PRODUCT);
        updateProductIntent.putExtra(StoreService.EXTRA_OLD_PRODUCT, oldProduct);
        updateProductIntent.putExtra(StoreService.EXTRA_NEW_PRODUCT, newProduct);

        getContext().startService(updateProductIntent);
    }

    /**
     * Считываем данные введенные в пользовательском интерфейсе.
     * @return продукт, null если данные введены неккоректно.
     */
    private Product getProduct() {
        String productName = mProductNameEditText.getText().toString();
        String productCostStr = mProductCostEditText.getText().toString();
        String productNumStr = mProductNumEditText.getText().toString();

        if (TextUtils.isEmpty(productName)) {
            mProductNameEditText.setError(getResources().getString(R.string.error_empty_field));
            return null;
        }
        if (TextUtils.isEmpty(productCostStr)) {
            mProductCostEditText.setError(getResources().getString(R.string.error_empty_field));
            return null;
        }
        if (TextUtils.isEmpty(productNumStr)) {
            mProductNumEditText.setError(getResources().getString(R.string.error_empty_field));
            return null;
        }

        float productCost = Float.parseFloat(productCostStr);
        int  productNum = Integer.parseInt(productNumStr);

        return new Product(productName, productCost, productNum);
    }
}
