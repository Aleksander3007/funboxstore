package ru.ermakov.funboxstore.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.fragments.storefront.ProductDetailFragment;

/**
 * Адаптер для отображения продуктов в ViewPager.
 */
public class ProductViewPagerAdapter extends FragmentStatePagerAdapter implements
        ProductDetailFragment.OnProductDetailFragmentListener {

    public static final String TAG = ProductViewPagerAdapter.class.getSimpleName();

    public interface ProductViewPagerAdapterListener {
        /**
         * Событие - покупка продукта.
         * @param product продукт который покупают.
         */
        void onBuyProduct(Product product);
    }
    private ProductViewPagerAdapter.ProductViewPagerAdapterListener mListener;

    private List<Product> mAvailableProducts = new ArrayList<>();

    public ProductViewPagerAdapter(FragmentManager fragmentManager, List<Product> products,
                                   ProductViewPagerAdapter.ProductViewPagerAdapterListener listener) {
        super(fragmentManager);

        if (products != null) {
            // Отображаем только те, что есть в наличии (кол-во товара > 0).
            mAvailableProducts = getAvailableProducts(products);
        }

        mListener = listener;
    }

    @Override
    public Fragment getItem(int position) {
        return ProductDetailFragment.newInstance(this, mAvailableProducts.get(position));
    }

    @Override
    public int getCount() {
        return (mAvailableProducts != null) ? mAvailableProducts.size() : 0;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void onBuyProduct(Product boughtProduct) {

        // Ищем изменный продукт ...
        Iterator<Product> iterator = mAvailableProducts.iterator();
        while (iterator.hasNext()) {
            Product product = iterator.next();
            // ... и если его количество меньше одного, то перестаем его отображать.
            if (boughtProduct.equals(product) &&
                    boughtProduct.getNum() <= 1) {
                iterator.remove();
                notifyDataSetChanged();
                break;
            }
        }

        if (mListener != null) mListener.onBuyProduct(boughtProduct);
    }

    /**
     * Заменить oldProduct на newProduct.
     * @param oldProduct продукт, который необходимо заменить.
     * @param newProduct продукт НА который необходимо заменить.
     */
    public void replaceProduct(Product oldProduct, Product newProduct) {
        int productIndex = mAvailableProducts.indexOf(oldProduct);

        if (productIndex != -1) {
            if (newProduct.getNum() > 0) {
                mAvailableProducts.set(productIndex, newProduct);
            }
            else {
                mAvailableProducts.remove(productIndex);
            }
            notifyDataSetChanged();
        }
        else {
            addProduct(newProduct);
        }
    }

    /**
     * Добавить новый продукт.
     * @param product продукт, который необходимо добавить.
     */
    public void addProduct(Product product) {
        if (product.getNum() > 0) {
            mAvailableProducts.add(product);
            notifyDataSetChanged();
        }
    }

    /**
     * Удалить продукт.
     * @param deletedProduct продукт, который необходимо удалить.
     */
    public void removeProduct(Product deletedProduct) {
        if (deletedProduct.getNum() > 0) {
            mAvailableProducts.remove(deletedProduct);
            notifyDataSetChanged();
        }
    }

    public void setProducts(List<Product> products) {
        mAvailableProducts = getAvailableProducts(products);
        notifyDataSetChanged();
    }

    /**
     * Сформировать список доступных продуктов (кол-во > 0) из всех.
     * @param products список всех продуктов.
     * @return список доступных продуктов.
     */
    private List<Product> getAvailableProducts(List<Product> products) {
        List<Product> availableProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getNum() > 0) availableProducts.add(product);
        }
        return availableProducts;
    }
}
