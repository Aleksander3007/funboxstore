package ru.ermakov.funboxstore.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.ermakov.funboxstore.data.Product;
import ru.ermakov.funboxstore.R;

/**
 * Адаптер для отображения продуктов в RecyclerView.
 */
public class ProductRecyclerViewAdapter extends RecyclerView.Adapter<ProductRecyclerViewAdapter.ViewHolder> {

    private List<Product> mProducts;

    /**
     * Интерфейс, который определяет методы для обработки нажатий на элементы.
     */
    public interface OnProductClickListener {
        /**
         * Обработка нажатия на элемент списка.
         * @param product продукт, на который было выполнено нажатие.
         */
        void onProductClick(Product product);
    }
    private final OnProductClickListener mProductClickListener;

    /**
     * Конструктор.
     * @param products список продуктов.
     * @param mProductClickListener слушатель нажатия на продукт.
     */
    public ProductRecyclerViewAdapter(List<Product> products,
                                      ProductRecyclerViewAdapter.OnProductClickListener mProductClickListener) {
        this.mProducts = products;
        this.mProductClickListener = mProductClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Product product = mProducts.get(position);
        holder.setProductName(product.getName());
        holder.setProductNum(product.getNum());
    }

    @Override
    public int getItemCount() {
        return (mProducts != null) ? mProducts.size() : 0;
    }

    /**
     * Заменить oldProduct на newProduct.
     * @param oldProduct продукт, который необходимо заменить.
     * @param newProduct продукт НА который необходимо заменить.
     */
    public void replaceProduct(Product oldProduct, Product newProduct) {
        int productIndex = mProducts.indexOf(oldProduct);
        if (productIndex != -1) {
            mProducts.set(productIndex, newProduct);
            notifyItemChanged(productIndex);
        }
    }

    /**
     * Добавить новый продукт.
     * @param product продукт, который необходимо добавить.
     */
    public void addProduct(Product product) {
        mProducts.add(product);
        notifyItemInserted(mProducts.size() - 1);
    }

    public void setProducts(List<Product> products) {
        mProducts = products;
        notifyDataSetChanged();
    }

    /**
     * Удаляем продукт из списка.
     * @param productIndex индекс продукта, который необходимо удалить.
     */
    public Product removeProduct(int productIndex) {
        if (productIndex >= 0 && productIndex < mProducts.size()) {
            Product removedProduct = mProducts.remove(productIndex);
            notifyItemRemoved(productIndex);

            return removedProduct;
        }
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_product_name) TextView mProductNameTextView;
        @BindView(R.id.tv_product_num) TextView mProductNumTextView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void setProductName(String productName) {
            mProductNameTextView.setText(productName);
        }

        void setProductNum(int productNum) {
            mProductNumTextView.setText(
                    String.format("%d %s", productNum,
                            itemView.getResources().getString(R.string.units)));
        }

        @Override
        public void onClick(View v) {
            if (mProductClickListener != null)
                mProductClickListener.onProductClick(mProducts.get(getAdapterPosition()));
        }
    }
}
