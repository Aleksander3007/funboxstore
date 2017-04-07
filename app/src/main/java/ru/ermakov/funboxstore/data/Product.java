package ru.ermakov.funboxstore.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Класс описывающий продукт.
 */
public class Product implements Parcelable {
    private String mName;
    private float mCost;
    private int mNum;

    public Product(String name, float cost, int num) {
        this.setName(name);
        this.setCost(cost);
        this.setNum(num);
    }

    protected Product(Parcel in) {
        mName = in.readString();
        mCost = in.readFloat();
        mNum = in.readInt();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public float getCost() {
        return mCost;
    }

    public void setCost(float cost) {
        mCost = cost;
    }

    public int getNum() {
        return mNum;
    }

    public void setNum(int num) {
        mNum = num;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeFloat(mCost);
        dest.writeInt(mNum);
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return (getName().equals(((Product)obj).getName()));
        }
        catch (ClassCastException ccex) {
            return false;
        }
    }

    @Override
    public Product clone() {
        return new Product(getName(), getCost(), getNum());
    }
}
