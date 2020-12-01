package com.example.damien.onlinegrocerystore;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class Profile extends AppCompatActivity {
    BottomNavigationView mBottomNavigationView;
    TextView tvUsername;
    TextView tvPassword;
    TextView tvPhone;

    public static final int REQUEST_CODE1 = 1;
    public static final String USERNAME = "com.example.damien.onlinegrocerystore.USERNAME";
    public static final String PASSWORD = "com.example.damien.onlinegrocerystore.PASSWORD";
    public static final String PHONE = "com.example.damien.onlinegrocerystore.PHONE";

    private int id;
    private String username;
    private String password;
    private String phoneNo;
    private Float totalBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mBottomNavigationView = findViewById(R.id.bottom_nav_bar);
        tvUsername = findViewById(R.id.tvUsername);
        tvPassword = findViewById(R.id.tvPassword);
        tvPhone = findViewById(R.id.tvPhone);

        id = getIntent().getIntExtra(Login.EXTRA_ID, -1);
        username = getIntent().getStringExtra(Login.EXTRA_USERNAME);
        password = getIntent().getStringExtra(Login.EXTRA_PASSWORD);
        phoneNo = getIntent().getStringExtra(Login.EXTRA_PHONE);
        totalBalance = getIntent().getFloatExtra(Login.EXTRA_WALLET_BALANCE, 1);

        tvUsername.setText(username);
        tvPassword.setText(password);
        tvPhone.setText(phoneNo);

        mBottomNavigationView.setSelectedItemId(R.id.nav_profile);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        Intent a = new Intent(Profile.this, Homepage.class);
                        a.putExtra(Login.EXTRA_ID, id);
                        a.putExtra(Profile.USERNAME, username);
                        a.putExtra(Profile.PASSWORD, password);
                        a.putExtra(Profile.PHONE, phoneNo);
                        a.putExtra(Login.EXTRA_WALLET_BALANCE, totalBalance);
                        startActivity(a);
                        return true;
                    case R.id.nav_profile:
                        return true;
                    case R.id.nav_history:
                        Intent c = new Intent(Profile.this, History.class);
                        c.putExtra(Login.EXTRA_ID, id);
                        c.putExtra(Profile.USERNAME, username);
                        c.putExtra(Profile.PASSWORD, password);
                        c.putExtra(Profile.PHONE, phoneNo);
                        c.putExtra(Login.EXTRA_WALLET_BALANCE, totalBalance);
                        startActivity(c);
                        return true;
                    case R.id.nav_logout:
                        Intent d = new Intent(Profile.this, Login.class);
                        startActivity(d);
                        return true;
                }
                return false;
            }
        });
    }

    public void btnEditProfile_onClicked(View view) {
        Intent i = new Intent(Profile.this, EditProfile.class);
        i.putExtra(Login.EXTRA_ID, id);
        i.putExtra(Profile.USERNAME, username);
        i.putExtra(Profile.PASSWORD, password);
        i.putExtra(Profile.PHONE, phoneNo);
        startActivityForResult(i, REQUEST_CODE1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE1){
            if (resultCode == RESULT_OK){
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }
}
