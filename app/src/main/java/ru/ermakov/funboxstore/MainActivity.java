package ru.ermakov.funboxstore;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.ermakov.funboxstore.db.StoreDbHelper;
import ru.ermakov.funboxstore.fragments.storefront.ProductListFragment;

/**
 * Главное Activity приложения.
 */
public class MainActivity extends AppCompatActivity {

    private static final String KEY_IS_STORE_FRONT_ACTIVE = "KEY_IS_STORE_FRONT_ACTIVE";

    @BindView(R.id.btn_open_store_front) Button mOpenStoreFrontButton;
    @BindView(R.id.btn_open_back_end) Button mOpenBackEndButton;

    private boolean mIsStoreFrontActive;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            openStoreFrontWindow();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_STORE_FRONT_ACTIVE, mIsStoreFrontActive);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mIsStoreFrontActive = savedInstanceState.getBoolean(KEY_IS_STORE_FRONT_ACTIVE);
        setNavButtonsState(mIsStoreFrontActive);
    }

    @Override
    protected void onDestroy() {
        StoreDbHelper.getInstance(this).close();
        super.onDestroy();
    }

    @OnClick(R.id.btn_open_store_front)
    public void onOpenStoreFrontClick() {
        openStoreFrontWindow();
    }

    @OnClick(R.id.btn_open_back_end)
    public void onOpenBackEndClick() {
        openBackEndWindow();
    }

    private void openStoreFrontWindow() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container,
                        new ProductListFragment())
                .commit();

        mIsStoreFrontActive = true;
        setNavButtonsState(mIsStoreFrontActive);
    }

    private void openBackEndWindow() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container,
                        new ru.ermakov.funboxstore.fragments.backend.ProductListFragment())
                .commit();

        mIsStoreFrontActive = false;
        setNavButtonsState(mIsStoreFrontActive);
    }

    /**
     * Устанавливаем активность кнопок навигации в зависимости от отображаемого окна.
     * @param isStoreFrontActive активно ли окно StoreFront.
     */
    private void setNavButtonsState(boolean isStoreFrontActive) {
        mOpenStoreFrontButton.setActivated(isStoreFrontActive);
        mOpenBackEndButton.setActivated(!isStoreFrontActive);
    }
}
