package ru.ermakov.funboxstore;

import android.os.Parcel;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import ru.ermakov.funboxstore.data.Product;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProductTest {

    @Test
    public void testParcel() {
        final String stubProductName = "stubName";
        final float stubProductCost = 1.2f;
        final int stubProductNum = 3;

        Product writeProduct = new Product(stubProductName, stubProductCost, stubProductNum);

        Parcel parcel = Parcel.obtain();
        writeProduct.writeToParcel(parcel, 0);

        // После записи сбрасываем parcel для дальнейшего чтения.
        parcel.setDataPosition(0);

        Product readProduct = Product.CREATOR.createFromParcel(parcel);
        assertEquals(writeProduct.getName(), readProduct.getName());
        assertEquals(writeProduct.getCost(), readProduct.getCost(), 0.1);
        assertEquals(writeProduct.getNum(), readProduct.getNum());
    }
}