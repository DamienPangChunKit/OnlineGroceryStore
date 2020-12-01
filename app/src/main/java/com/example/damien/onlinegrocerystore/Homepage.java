package com.example.damien.onlinegrocerystore;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class Homepage extends AppCompatActivity {
    RecyclerView topSalesRecyclerView;
    TopSalesAdapter mTopSalesAdapter;

    RecyclerView categoryRecyclerView;
    CategoriesAdapter mCategoriesAdapter;

    BottomNavigationView mBottomNavigationView;
    TextView mTVMoney;

    private int id;
    private String username;
    private String password;
    private String phoneNo;
    private float totalBalance;

    public static final int REQUEST_CODE2 = 2;
    public static final int REQUEST_CODE3 = 3;
    public static final int REQUEST_CODE4 = 4;
    public static final int REQUEST_CODE5 = 5;
    public static final int REQUEST_CODE6 = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        topSalesRecyclerView = findViewById(R.id.recyclerTopSales);
        categoryRecyclerView = findViewById(R.id.recyclerCategories);
        mBottomNavigationView = findViewById(R.id.bottom_nav_bar);
        mTVMoney = findViewById(R.id.tvMoney);

        id = getIntent().getIntExtra(Login.EXTRA_ID, -1);
        username = getIntent().getStringExtra(Login.EXTRA_USERNAME);
        password = getIntent().getStringExtra(Login.EXTRA_PASSWORD);
        phoneNo = getIntent().getStringExtra(Login.EXTRA_PHONE);
        totalBalance = getIntent().getFloatExtra(Login.EXTRA_WALLET_BALANCE, 1);

        mTVMoney.setText("RM " + totalBalance);

        mBottomNavigationView.setSelectedItemId(R.id.nav_home);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        return true;
                    case R.id.nav_profile:
                        Intent b = new Intent(Homepage.this, Profile.class);
                        b.putExtra(Login.EXTRA_ID, id);
                        b.putExtra(Profile.USERNAME, username);
                        b.putExtra(Profile.PASSWORD, password);
                        b.putExtra(Profile.PHONE, phoneNo);
                        b.putExtra(Login.EXTRA_WALLET_BALANCE, totalBalance);
                        startActivityForResult(b, REQUEST_CODE2);
                        return true;
                    case R.id.nav_history:
                        Intent c = new Intent(Homepage.this, History.class);
                        c.putExtra(Login.EXTRA_ID, id);
                        c.putExtra(Profile.USERNAME, username);
                        c.putExtra(Profile.PASSWORD, password);
                        c.putExtra(Profile.PHONE, phoneNo);
                        c.putExtra(Login.EXTRA_WALLET_BALANCE, totalBalance);
                        startActivity(c);
                        return true;
                    case R.id.nav_logout:
                        Intent d = new Intent(Homepage.this, Login.class);
                        startActivity(d);
                        return true;
                }
                return false;
            }
        });

        // top sales item
        Background bg = new Background(Background.FETCH_TOP_SALES_ITEM);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        topSalesRecyclerView.setLayoutManager(layoutManager);
        mTopSalesAdapter = new TopSalesAdapter(bg);
        topSalesRecyclerView.setAdapter(mTopSalesAdapter);

        // categories item
        Background bg2 = new Background(Background.FETCH_CATEGORIES_ITEM);
        RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        categoryRecyclerView.setLayoutManager(layoutManager2);
        mCategoriesAdapter = new CategoriesAdapter(bg2);
        categoryRecyclerView.setAdapter(mCategoriesAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE2){
            if (resultCode == RESULT_OK){
                username = data.getStringExtra("USERNAME_EDIT");
                password = data.getStringExtra("PASSWORD_EDIT");
                phoneNo = data.getStringExtra("PHONE_EDIT");
                Toast.makeText(this, "Profile save successfully!", Toast.LENGTH_SHORT).show();
                mBottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        } else if (requestCode == REQUEST_CODE3 || requestCode == REQUEST_CODE5){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "Item added in the cart successfully!", Toast.LENGTH_SHORT).show();
                mBottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        } else if (requestCode == REQUEST_CODE6){
            if (resultCode == RESULT_OK){
                totalBalance = data.getFloatExtra("total_balance", 0);
                mTVMoney.setText("RM " + totalBalance);
                Toast.makeText(this, "Reload successfully!", Toast.LENGTH_SHORT).show();
                mBottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        } else if (requestCode == REQUEST_CODE4){
            if (resultCode == RESULT_OK){
                totalBalance = data.getFloatExtra("TOTAL_BALANCE_AFTER_PAID", 0);
                mTVMoney.setText("RM " + totalBalance);
                Toast.makeText(this, "Order made successfully, please check history for more details!", Toast.LENGTH_SHORT).show();
                mBottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    public void btnShoppingCart_onClicked(View view) {
        Intent i = new Intent(Homepage.this, ShoppingCart.class);
        i.putExtra(Login.EXTRA_ID, id);
        i.putExtra(Login.EXTRA_WALLET_BALANCE, totalBalance);
        startActivityForResult(i, REQUEST_CODE4);
    }

    public void btnReloadMoney_onClicked(View view) {
        Intent i = new Intent(Homepage.this, Reload.class);
        i.putExtra(Login.EXTRA_ID, id);
        i.putExtra(Login.EXTRA_WALLET_BALANCE, totalBalance);
        i.putExtra(Login.EXTRA_PASSWORD, password);
        startActivityForResult(i, REQUEST_CODE6);
    }

    public class Background extends AsyncTask<String, Void, ResultSet> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";
        private static final String USERNAME = "sql12377787";
        private static final String DB_NAME = "sql12377787";
        private static final String PASSWORD = "WsCZjPsSEW";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt;
        private int method;

        public static final int FETCH_TOP_SALES_ITEM = 1;
        public static final int FETCH_CATEGORIES_ITEM = 2;

        public Background(int method) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            this.method = method;
        }

        @Override
        protected ResultSet doInBackground(String... strings) {
            conn = connectDB();
            ResultSet result = null;

            if (conn == null) {
                return null;
            }
            try {
                String query;

                switch(method){
                    case FETCH_TOP_SALES_ITEM:
                        query = "SELECT id, name, description, quantity, price, item_id FROM top_sales_item";
                        stmt = conn.prepareStatement(query);
                        result = stmt.executeQuery();
                        return result;

                    case FETCH_CATEGORIES_ITEM:
                        query = "SELECT id, name FROM category_item";
                        stmt = conn.prepareStatement(query);
                        result = stmt.executeQuery();
                        return result;
                }
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return null;
        }

        private Connection connectDB() {
            try {
                Class.forName(LIBRARY);
                return DriverManager.getConnection("jdbc:mysql://" + SERVER + "/" + DB_NAME, USERNAME, PASSWORD);
            }
            catch (Exception e) {
                Log.e("Error on Connection", e.getMessage());
                return null;
            }
        }

        public void closeConn () {
            try { stmt.close(); } catch (Exception e) { /* ignored */ }
            try { conn.close(); } catch (Exception e) { /* ignored */ }
        }
    }

    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private class TopSalesAdapter extends RecyclerView.Adapter<TopSalesAdapter.TopSalesViewHolder>{
        private LayoutInflater mInflater;
        private int itemCount;
        private Background bg;
        private ResultSet result;

        public TopSalesAdapter(Background bg) {
            this.bg = bg;
            updateResultSet();
            mInflater = LayoutInflater.from(Homepage.this);
        }

        class TopSalesViewHolder extends RecyclerView.ViewHolder{
            TextView name, description, price, quantity, unit;
            ConstraintLayout background;

            final TopSalesAdapter mAdapter;

            public TopSalesViewHolder(@NonNull View itemView, TopSalesAdapter adapter){
                super(itemView);
                name = itemView.findViewById(R.id.name);
                description = itemView.findViewById(R.id.description);
                price = itemView.findViewById(R.id.price);
                quantity = itemView.findViewById(R.id.quantity);
                unit = itemView.findViewById(R.id.unit);
                background = itemView.findViewById(R.id.layout_topSales);

                this.mAdapter = adapter;
            }
        }

        @NonNull
        @Override
        public TopSalesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.top_sales_row_items, viewGroup, false);
            return new TopSalesViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull TopSalesViewHolder topSalesViewHolder, int position) {
            try {
                result.first();
                for (int i = 0; i < position; i++) {
                    result.next();
                }

                final int topID = result.getInt(1);
                final String topName = result.getString(2);
                final String topDescription = result.getString(3);
                final String topQuantity = result.getString(4);
                final float topPrice = result.getFloat(5);
                final int topSalesItemID = result.getInt(6);

                topSalesViewHolder.name.setText(topName);
                topSalesViewHolder.description.setText(topDescription);
                topSalesViewHolder.quantity.setText(topQuantity);
                topSalesViewHolder.price.setText(topPrice + "");

                switch (topID){
                    case 1:
                        topSalesViewHolder.background.setBackgroundResource(R.drawable.apple);
                        break;
                    case 2:
                        topSalesViewHolder.background.setBackgroundResource(R.drawable.kiwi);
                        break;
                    case 3:
                        topSalesViewHolder.background.setBackgroundResource(R.drawable.strawberry);
                        break;
                    case 4:
                        topSalesViewHolder.background.setBackgroundResource(R.drawable.lemon);
                        break;
                    case 5:
                        topSalesViewHolder.background.setBackgroundResource(R.drawable.orange);
                        break;
                }

                topSalesViewHolder.background.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Homepage.this, TopSalesProductDetails.class);
                        i.putExtra(Login.EXTRA_ID, id);
                        i.putExtra("topSalesID", topID);
                        i.putExtra("topSalesName", topName);
                        i.putExtra("topSalesDescription", topDescription);
                        i.putExtra("topSalesQuantity", topQuantity);
                        i.putExtra("topSalesPrice", topPrice);
                        i.putExtra("topSalesItemID", topSalesItemID);
                        startActivityForResult(i, REQUEST_CODE3);
                    }
                });
            }
            catch (SQLException e) {
                Log.d("ERROR BIND VIEW", e.getMessage());
            }
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        private int getResultCount() {
            try {
                result.last();
                int count = result.getRow();
                result.first();
                return count;
            } catch (SQLException e) {

            }
            return 0;
        }

        public void updateResultSet() {
            try {
                bg.closeConn();
                bg = new Background(Background.FETCH_TOP_SALES_ITEM);
                this.result = this.bg.execute().get();
                itemCount = getResultCount();
            } catch (ExecutionException e) {
                Log.e("ERROR EXECUTION", e.getMessage());
            } catch (InterruptedException e) {
                Log.e("ERROR INTERRUPTED", e.getMessage());
            }
        }
    }

    private class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {
        private LayoutInflater mInflater;
        private int itemCount;
        private Background bg2;
        private ResultSet result;

        public CategoriesAdapter(Background bg2) {
            this.bg2 = bg2;
            updateResultSet();
            mInflater = LayoutInflater.from(Homepage.this);
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder{
            TextView categoryName;
            ImageView imgCategory;
            ConstraintLayout mConstraintLayout;

            final CategoriesAdapter mAdapter;

            public CategoryViewHolder(@NonNull View itemView, CategoriesAdapter adapter){
                super(itemView);
                categoryName = itemView.findViewById(R.id.tvCategoryName);
                imgCategory = itemView.findViewById(R.id.imgCategory);
                mConstraintLayout = itemView.findViewById(R.id.layout_categories);

                this.mAdapter = adapter;
            }
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View view = mInflater.inflate(R.layout.categories_row_items, parent, false);
            return new CategoryViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder categoryViewHolder, final int position) {
            try {
                result.first();
                for (int i = 0; i < position; i++) {
                    result.next();
                }

                final int categoryID = result.getInt(1);
                final String categoryName = result.getString(2);

                categoryViewHolder.categoryName.setText(categoryName);

                switch (categoryID){
                    case 1:
                        categoryViewHolder.imgCategory.setImageResource(R.drawable.lettuce);
                        break;
                    case 2:
                        categoryViewHolder.imgCategory.setImageResource(R.drawable.peach);
                        break;
                    case 3:
                        categoryViewHolder.imgCategory.setImageResource(R.drawable.broccoli);
                        break;
                    case 4:
                        categoryViewHolder.imgCategory.setImageResource(R.drawable.beetroot);
                        break;
                    case 5:
                        categoryViewHolder.imgCategory.setImageResource(R.drawable.raspberry);
                        break;
                }

                categoryViewHolder.mConstraintLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Homepage.this, CategoriesItems.class);
                        i.putExtra("categoryID", categoryID);
                        i.putExtra("categoryName", categoryName);
                        i.putExtra(Login.EXTRA_ID, id);
                        startActivityForResult(i, REQUEST_CODE5);
                    }
                });
            }
            catch (SQLException e) {
                Log.d("ERROR BIND VIEW", e.getMessage());
            }
        }

        @Override
        public int getItemCount() {
            return itemCount;
        }

        private int getResultCount() {
            try {
                result.last();
                int count = result.getRow();
                result.first();
                return count;
            } catch (SQLException e) {

            }
            return 0;
        }

        public void updateResultSet() {
            try {
                bg2.closeConn();
                bg2 = new Background(Background.FETCH_CATEGORIES_ITEM);
                this.result = this.bg2.execute().get();
                itemCount = getResultCount();
            } catch (ExecutionException e) {
                Log.e("ERROR EXECUTION", e.getMessage());
            } catch (InterruptedException e) {
                Log.e("ERROR INTERRUPTED", e.getMessage());
            }
        }
    }
}
