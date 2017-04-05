package com.example.caxidy.tourishare;

import android.app.ListActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;

public class PrincipalActivity extends ListActivity implements AppCompatCallback {

    private AppCompatDelegate delegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        delegate = AppCompatDelegate.create(this,this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {}

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {}

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }
}
