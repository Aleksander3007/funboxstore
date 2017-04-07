package ru.ermakov.funboxstore.fragments.storefront;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.R;

/**
 * Фрагмент для отображения конкретного товара.
 */
public class ProductDetailFragment extends Fragment {

    public static final String ARG_PRODUCT = "ARG_PRODUCT";

    public interface OnProductDetailFragmentListener {
        /**
         * Событие - покупка продукта.
         * @param product продукт который покупают.
         */
        void onBuyProduct(Product product);
    }
    private OnProductDetailFragmentListener mListener;

    @BindView(R.id.tv_product_name) TextView mProductNameTextView;
    @BindView(R.id.tv_product_cost) TextView mProductCostTextView;
    @BindView(R.id.tv_product_num) TextView mProductNumTextView;

    private Unbinder mUnbinder;

    /** Продукт, который отображен в данном фрагменте. */
    private Product mProduct;

    public static ProductDetailFragment newInstance(OnProductDetailFragmentListener listener,
                                                    Product product) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PRODUCT, product);

        ProductDetailFragment fragment = new ProductDetailFragment();
        fragment.setListener(listener);
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(OnProductDetailFragmentListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storefront_product_detail, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mProduct = getArguments().getParcelable(ARG_PRODUCT);
        mProductNameTextView.setText(mProduct.getName());
        setProductCost(mProduct.getCost());
        setProductNum(mProduct.getNum());

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.btn_buy_product)
    public void onBuyProductClick() {

        // Продукт до покупки.
        Product oldProduct = mProduct.clone();

        mProduct.setNum(mProduct.getNum() - 1);
        setProductNum(mProduct.getNum());

        if (mListener != null) mListener.onBuyProduct(oldProduct);
    }

    private void setProductCost(float productCost) {
        // Если стоимость продукта не имеет дробной части, не выводим ее.
        if (productCost == (long) productCost) {
            mProductCostTextView.setText(String.format("%d %s",
                    (long) productCost,
                    getResources().getString(R.string.rubles)));
        }
        else {
            mProductCostTextView.setText(String.format("%.2f %s",
                    productCost,
                    getResources().getString(R.string.rubles)));
        }
    }

    private void setProductNum(int productNum) {
        mProductNumTextView.setText(String.valueOf(productNum) + " " +
                getResources().getString(R.string.units));
    }
}
