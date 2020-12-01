package com.example.damien.onlinegrocerystore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
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

import static com.example.damien.onlinegrocerystore.CategoriesItems.Background.INSERT_ITEM;

public class CategoriesItems extends AppCompatActivity {
    RecyclerView categoryItemsRecyclerView;
    CategoriesProductAdapter mCategoriesProductAdapter;

    TextView categoriesTitle;
    ImageView backButton;

    private int categoryID;
    private int customer_id;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_item);

        categoriesTitle = findViewById(R.id.tvCategoriesTitle);
        backButton = findViewById(R.id.btnBack);
        categoryItemsRecyclerView = findViewById(R.id.categoriesItemsRecycler);

        Intent i = getIntent();
        categoryID = i.getIntExtra("categoryID", 0);
        categoryName = i.getStringExtra("categoryName");
        customer_id = i.getIntExtra(Login.EXTRA_ID, 0);

        categoriesTitle.setText(categoryName);

        Background bg = new Background(Background.FETCH_CATEGORY_ITEM);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        categoryItemsRecyclerView.setLayoutManager(layoutManager);
        mCategoriesProductAdapter = new CategoriesProductAdapter(bg);
        categoryItemsRecyclerView.setAdapter(mCategoriesProductAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public class Background extends AsyncTask<String, Void, ResultSet> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";
        private static final String USERNAME = "sql12377787";
        private static final String DB_NAME = "sql12377787";
        private static final String PASSWORD = "WsCZjPsSEW";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt;
        private ProgressDialog progressDialog;
        private int method;

        public static final int FETCH_CATEGORY_ITEM = 1;
        public static final int INSERT_ITEM = 2;

        public Background(int method) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            this.method = method;
        }

        @Override
        protected void onPostExecute(ResultSet result) {
            super.onPostExecute(result);

            try {
                switch (this.method){
                    case INSERT_ITEM:
                        Intent i = new Intent();
                        setResult(RESULT_OK, i);
                        finish();
                }
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(CategoriesItems.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CategoriesItems.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing data");
        }

        @Override
        protected ResultSet doInBackground(String... strings) {
            conn = connectDB();
            ResultSet result = null;

            if (conn == null) {
                return null;
            }
            try {
                String query = "";

                switch(this.method){
                    case FETCH_CATEGORY_ITEM:
                        query = "SELECT id, name, quantity, price, type, category FROM grocery_item WHERE type = ? OR category = ?";
                        stmt = conn.prepareStatement(query);
                        stmt.setString(1, categoryName);
                        stmt.setString(2, categoryName);
                        result = stmt.executeQuery();
                        return result;

                    case INSERT_ITEM:
                        query = "insert into customer_order (customer_id, item_id, item_name, item_price, total_price, total_item, status) values (?, ?, ?, ?, ?, ?, ?)";
                        stmt = conn.prepareStatement(query);
                        stmt.setInt(1, Integer.parseInt(strings[0]));
                        stmt.setInt(2, Integer.parseInt(strings[1]));
                        stmt.setString(3, strings[2]);
                        stmt.setFloat(4, Float.parseFloat(strings[3]));
                        stmt.setFloat(5, Float.parseFloat(strings[4]));
                        stmt.setInt(6, Integer.parseInt(strings[5]));
                        stmt.setString(7, "inside cart");
                        stmt.executeUpdate();
                }
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return result;
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

    private class CategoriesProductAdapter extends RecyclerView.Adapter<CategoriesProductAdapter.CategoryProductViewHolder> {
        private LayoutInflater mInflater;
        private int itemCount;
        private Background bg;
        private ResultSet result;

        public CategoriesProductAdapter(Background bg) {
            this.bg = bg;
            updateResultSet();
            mInflater = LayoutInflater.from(CategoriesItems.this);
        }

        class CategoryProductViewHolder extends RecyclerView.ViewHolder{
            TextView categoryProductName;
            TextView categoryProductQuantity;
            TextView categoryProductPrice;
            ImageView imgCategory;
            ImageView imgAddToCart;

            final CategoriesProductAdapter mAdapter;

            public CategoryProductViewHolder(@NonNull View itemView, CategoriesProductAdapter adapter){
                super(itemView);
                categoryProductName = itemView.findViewById(R.id.tvCategoriesName);
                categoryProductQuantity = itemView.findViewById(R.id.tvCategoriesQuantity);
                categoryProductPrice = itemView.findViewById(R.id.tvCategoriesPrice);
                imgCategory = itemView.findViewById(R.id.imgCategoriesItems);
                imgAddToCart = itemView.findViewById(R.id.imgAddtoCart);

                this.mAdapter = adapter;
            }
        }

        @NonNull
        @Override
        public CategoryProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View view = mInflater.inflate(R.layout.categories_product_row_items, parent, false);
            return new CategoryProductViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryProductViewHolder categoryProductViewHolder, final int position) {
            try {
                result.first();
                for (int i = 0; i < position; i++) {
                    result.next();
                }

                final int categoryItemID = result.getInt(1);
                final String categoryItemName = result.getString(2);
                final int categoryItemQuantity = result.getInt(3);
                final float categoryItemPrice = result.getFloat(4);
                final String categoryItemType = result.getString(5);
                final String categoryItemCategory = result.getString(6);
                final int totalItem = 1;

                categoryProductViewHolder.categoryProductName.setText(categoryItemName);
                categoryProductViewHolder.categoryProductQuantity.setText(categoryItemQuantity + "");
                categoryProductViewHolder.categoryProductPrice.setText(categoryItemPrice + "");
                categoryProductViewHolder.imgAddToCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Background bg2 = new Background(INSERT_ITEM);
                        bg2.execute(String.valueOf(customer_id), String.valueOf(categoryItemID), categoryItemName, String.valueOf(categoryItemPrice), String.valueOf(categoryItemPrice), String.valueOf(totalItem));
                    }
                });

                if (categoryItemType.equals("Vegetable") && categoryName.equals("Vegetable")){
                    switch (categoryItemID){
                        case 1:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.artichoke);
                            break;
                        case 2:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.asparagus);
                            break;
                        case 4:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.beetroot);
                            break;
                        case 5:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.bell_pepper);
                            break;
                        case 7:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.broccoli);
                            break;
                        case 8:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.brussels_sprout);
                            break;
                        case 10:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.cabbage);
                            break;
                        case 11:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.carrot);
                            break;
                        case 13:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.cauliflower);
                            break;
                        case 14:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.celery);
                            break;
                        case 16:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.corn);
                            break;
                        case 17:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.cucumber);
                            break;
                        case 19:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.eggplant);
                            break;
                        case 20:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.green_bean);
                            break;
                        case 22:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.lettuce);
                            break;
                        case 23:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.onion);
                            break;
                        case 25:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.mushroom);
                            break;
                        case 26:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.pea);
                            break;
                        case 28:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.potato);
                            break;
                        case 29:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.pumpkin);
                            break;
                        case 31:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.radish);
                            break;
                        case 32:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.sweet_potato);
                            break;
                        case 34:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.tomato);
                            break;
                        case 35:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.zuchini);
                            break;
                    }
                } else if (categoryItemType.equals("Fruit") && categoryName.equals("Fruit")){
                    switch (categoryItemID) {
                        case 3:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.apple);
                            break;
                        case 6:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.blueberry);
                            break;
                        case 9:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.fig);
                            break;
                        case 12:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.grape);
                            break;
                        case 15:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.green_grape);
                            break;
                        case 18:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.green_lemon);
                            break;
                        case 21:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.kiwi);
                            break;
                        case 24:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.lemon);
                            break;
                        case 27:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.mandarin);
                            break;
                        case 30:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.orange);
                            break;
                        case 33:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.peach);
                            break;
                        case 36:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.plum);
                            break;
                        case 37:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.raspberry);
                            break;
                        case 38:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.red_pear);
                            break;
                        case 39:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.strawberry);
                            break;
                    }
                } else if (categoryItemCategory.equals("Green vegetable") && categoryName.equals("Green vegetable")){
                    switch (categoryItemID){
                        case 1:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.artichoke);
                            break;
                        case 2:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.asparagus);
                            break;
                        case 7:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.broccoli);
                            break;
                        case 8:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.brussels_sprout);
                            break;
                        case 10:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.cabbage);
                            break;
                        case 14:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.celery);
                            break;
                        case 17:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.cucumber);
                            break;
                        case 20:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.green_bean);
                            break;
                        case 22:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.lettuce);
                            break;
                        case 26:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.pea);
                            break;
                        case 35:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.zuchini);
                            break;
                    }
                } else if (categoryItemCategory.equals("Roots") && categoryName.equals("Roots")){
                    switch (categoryItemID) {
                        case 4:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.beetroot);
                            break;
                        case 28:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.potato);
                            break;
                        case 29:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.pumpkin);
                            break;
                        case 32:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.sweet_potato);
                            break;
                    }
                } else if (categoryItemCategory.equals("Red fruit") && categoryName.equals("Red fruit")){
                    switch (categoryItemID) {
                        case 3:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.apple);
                            break;
                        case 36:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.plum);
                            break;
                        case 37:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.raspberry);
                            break;
                        case 38:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.red_pear);
                            break;
                        case 39:
                            categoryProductViewHolder.imgCategory.setImageResource(R.drawable.strawberry);
                            break;
                    }
                }
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
                bg = new Background(Background.FETCH_CATEGORY_ITEM);
                this.result = this.bg.execute().get();
                itemCount = getResultCount();
            } catch (ExecutionException e) {
                Log.e("ERROR EXECUTION", e.getMessage());
            } catch (InterruptedException e) {
                Log.e("ERROR INTERRUPTED", e.getMessage());
            }
        }
    }

}
