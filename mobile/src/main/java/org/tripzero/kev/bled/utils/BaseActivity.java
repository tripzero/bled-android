package org.tripzero.kev.bled.utils;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import org.tripzero.kev.bled.R;

/**
 * Created by ammonrees on 10/22/14.
 */
public abstract class BaseActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private ImageView iconView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        iconView = (ImageView) toolbar.findViewById(R.id.icon);

      //  toolbar.setLogo(R.drawable.logo);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract int getLayoutResource();

    protected void setActionBarIcon(int iconRes) {

        toolbar.setNavigationIcon(iconRes);
    }


}
