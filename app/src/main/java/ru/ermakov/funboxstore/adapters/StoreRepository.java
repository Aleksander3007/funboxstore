package ru.ermakov.funboxstore.adapters;

import java.util.List;

import ru.ermakov.funboxstore.data.Product;

/**
 * Интерфейс, который содержит методы для работы с источником данных.
 */
public interface StoreRepository {
    /**
     * Получить список всех продуктов.
     * @return список всех продуктов.
     */
    List<Product> getAllData();

    /**
     * Получить продукт по имени.
     * @param productName имя продукта.
     * @return продукт, имеющий данное имя.
     */
    Product getProductByName(String productName);

    /**
     * Добавить продукт.
     * @param product продукт, который необходимо добавить.
     */
    boolean addProduct(Product product);

    /**
     * Обновить информацию о продукте.
     * @param oldProduct продукт который необходимо обновить.
     * @param newProduct новая информация о продукте.
     * @return true - если удалось обновить информацию о продукте.
     */
    boolean updateProduct(Product oldProduct, Product newProduct);

    /**
     * Удалить продукт.
     * @param product продукт, который необходимо удалить.
     */
    boolean deleteProduct(Product product);

    /**
     * Декремент значения количество указанного продукта, но не ниже указанного значения minNum.
     * @param product продукт.
     * @param minNum минимально возможное значение количества продукта.
     * @return false - если ошибка записи, либо значение при декременте получается ниже указанного minNum.
     */
    boolean decrementProductNum(Product product, int minNum);
}
