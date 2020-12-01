package com.example.damien.onlinegrocerystore;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

import static com.example.damien.onlinegrocerystore.ShoppingCart.Background.CHANGE_ITEM_NUMBER_AND_PRICE;
import static com.example.damien.onlinegrocerystore.ShoppingCart.Background.DELETE_CART_ITEM;

public class ShoppingCart extends AppCompatActivity {
    RecyclerView shoppingCartRecyclerView;
    ShoppingCartAdapter mShoppingCartAdapter;

    private int customer_id;
    private float finalPrice;
    private float myBalance;

    ImageView shoppingCartBackButton;
    TextView allItemTotalPrice;

    public static final int REQUEST_CODE7 = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        shoppingCartRecyclerView = findViewById(R.id.recyclerShoppingCart);
        shoppingCartBackButton = findViewById(R.id.btnBack);
        allItemTotalPrice = findViewById(R.id.tvAllItemTotalPrice);

        Intent i = getIntent();
        customer_id = i.getIntExtra(Login.EXTRA_ID, 0);
        myBalance = i.getFloatExtra(Login.EXTRA_WALLET_BALANCE, 0);

        Background bg = new Background(Background.FETCH_CART_ITEM);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        shoppingCartRecyclerView.setLayoutManager(layoutManager);
        mShoppingCartAdapter = new ShoppingCartAdapter(bg);
        shoppingCartRecyclerView.setAdapter(mShoppingCartAdapter);

        shoppingCartBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE7){
            if (resultCode == RESULT_OK){
                setResult(RESULT_OK, data);
                finish();
            }
        }
    }

    public void btnOrderNow_onClicked(View view) {
        float finalPrice = Float.parseFloat(allItemTotalPrice.getText().toString());

        if (finalPrice == 0){
            Toast.makeText(this, "Please add item in the shopping cart before proceed to order!", Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent(ShoppingCart.this, Payment.class);
            i.putExtra(Login.EXTRA_ID, customer_id);
            i.putExtra("finalPrice", finalPrice);
            i.putExtra(Login.EXTRA_WALLET_BALANCE, myBalance);
            startActivityForResult(i, REQUEST_CODE7);
        }
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

        public static final int FETCH_CART_ITEM = 1;
        public static final int DELETE_CART_ITEM = 2;
        public static final int CHANGE_ITEM_NUMBER_AND_PRICE = 3;

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

                switch(this.method){
                    case FETCH_CART_ITEM:
                        query = "SELECT id, item_id, item_name, item_price, total_item, total_price FROM customer_order WHERE (customer_id = ? AND status = ?)";
                        stmt = conn.prepareStatement(query);
                        stmt.setInt(1, customer_id);
                        stmt.setString(2, "inside cart");
                        result = stmt.executeQuery();
                        return result;

                    case DELETE_CART_ITEM:
                        String cartItemID = strings[0];
                        query = "DELETE FROM customer_order WHERE (item_id = ? AND customer_id = ?)";
                        stmt = conn.prepareStatement(query);
                        stmt.setInt(1, Integer.parseInt(cartItemID));
                        stmt.setInt(2, customer_id);
                        stmt.executeUpdate();

                    case CHANGE_ITEM_NUMBER_AND_PRICE:
                        query = "UPDATE customer_order SET total_item=?, total_price=? WHERE (item_id = ? AND customer_id = ?)";
                        stmt = conn.prepareStatement(query);
                        stmt.setInt(1, Integer.parseInt(strings[0]));
                        stmt.setFloat(2, Float.parseFloat(strings[1]));
                        stmt.setInt(3, Integer.parseInt(strings[2]));
                        stmt.setInt(4, customer_id);
                        stmt.executeUpdate();
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

    private class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ShoppingCartViewHolder> {
        private LayoutInflater mInflater;
        private int itemCount;
        private Background bg;
        private ResultSet result;

        public ShoppingCartAdapter(Background bg) {
            this.bg = bg;
            updateResultSet();
            mInflater = LayoutInflater.from(ShoppingCart.this);
        }

        class ShoppingCartViewHolder extends RecyclerView.ViewHolder{
            TextView cartItemName;
            TextView cartItemNumber;
            TextView cartItemPrice;
            ImageView cartItemImg;
            Button removeCart;
            Button addNumber;
            Button minusNumber;

            final ShoppingCartAdapter mAdapter;

            public ShoppingCartViewHolder(@NonNull View itemView, ShoppingCartAdapter adapter){
                super(itemView);
                cartItemName = itemView.findViewById(R.id.tvCartName);
                cartItemNumber = itemView.findViewById(R.id.tvCartItemNumber);
                cartItemPrice = itemView.findViewById(R.id.tvCartPrice);
                cartItemImg = itemView.findViewById(R.id.imgShoppingCartItem);
                removeCart = itemView.findViewById(R.id.btnRemoveCart);
                addNumber = itemView.findViewById(R.id.btnIncrease);
                minusNumber = itemView.findViewById(R.id.btnDecrease);

                this.mAdapter = adapter;
            }
        }

        @NonNull
        @Override
        public ShoppingCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View view = mInflater.inflate(R.layout.shopping_cart_row_items, parent, false);
            return new ShoppingCartViewHolder(view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull ShoppingCartViewHolder shoppingCartViewHolder, int position) {
            try {
                result.first();
                for (int i = 0; i < position; i++) {
                    result.next();
                }

                final int cartID = result.getInt(1);
                final int cartItemID = result.getInt(2);
                final String cartProductName = result.getString(3);
                final float cartProductPrice = result.getFloat(4);
                final int cartProductTotalItem = result.getInt(5); // number of the item
                final float cartProductTotalPrice = result.getFloat(6); // total price of the specific item only

                finalPrice = finalPrice + cartProductTotalPrice;

                shoppingCartViewHolder.cartItemName.setText(cartProductName);
                shoppingCartViewHolder.cartItemNumber.setText(cartProductTotalItem + "");
                shoppingCartViewHolder.cartItemPrice.setText(String.format("%.2f", cartProductTotalPrice) + "");
                allItemTotalPrice.setText(String.format("%.2f", finalPrice) + "");

                switch (cartItemID){
                    case 1:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.artichoke);
                        break;
                    case 2:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.asparagus);
                        break;
                    case 3:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.apple);
                        break;
                    case 4:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.beetroot);
                        break;
                    case 5:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.bell_pepper);
                        break;
                    case 6:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.blueberry);
                        break;
                    case 7:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.broccoli);
                        break;
                    case 8:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.brussels_sprout);
                        break;
                    case 9:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.fig);
                        break;
                    case 10:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.cabbage);
                        break;
                    case 11:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.carrot);
                        break;
                    case 12:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.grape);
                        break;
                    case 13:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.cauliflower);
                        break;
                    case 14:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.celery);
                        break;
                    case 15:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.green_grape);
                        break;
                    case 16:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.corn);
                        break;
                    case 17:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.cucumber);
                        break;
                    case 18:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.green_lemon);
                        break;
                    case 19:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.eggplant);
                        break;
                    case 20:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.green_bean);
                        break;
                    case 21:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.kiwi);
                        break;
                    case 22:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.lettuce);
                        break;
                    case 23:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.onion);
                        break;
                    case 24:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.lemon);
                        break;
                    case 25:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.mushroom);
                        break;
                    case 26:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.pea);
                        break;
                    case 27:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.mandarin);
                        break;
                    case 28:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.potato);
                        break;
                    case 29:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.pumpkin);
                        break;
                    case 30:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.orange);
                        break;
                    case 31:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.radish);
                        break;
                    case 32:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.sweet_potato);
                        break;
                    case 33:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.peach);
                        break;
                    case 34:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.tomato);
                        break;
                    case 35:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.zuchini);
                        break;
                    case 36:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.plum);
                        break;
                    case 37:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.raspberry);
                        break;
                    case 38:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.red_pear);
                        break;
                    case 39:
                        shoppingCartViewHolder.cartItemImg.setImageResource(R.drawable.strawberry);
                        break;
                }

                final ShoppingCartViewHolder s = shoppingCartViewHolder;

                shoppingCartViewHolder.removeCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        float itemTotalPrice = Float.parseFloat(s.cartItemPrice.getText().toString());
                        float finalAllPrice = Float.parseFloat(allItemTotalPrice.getText().toString());

                        allItemTotalPrice.setText(String.format("%.2f",finalAllPrice - itemTotalPrice) + "");

                        Background bg2 = new Background(DELETE_CART_ITEM);
                        try {
                            bg2.execute(String.valueOf(cartItemID)).get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        updateResultSet();
                        notifyItemRemoved(s.getAdapterPosition());
                    }
                });

                shoppingCartViewHolder.addNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int num = Integer.parseInt(s.cartItemNumber.getText().toString());
                        float itemTotalPrice = Float.parseFloat(s.cartItemPrice.getText().toString());
                        float finalAllPrice = Float.parseFloat(allItemTotalPrice.getText().toString());
                        float newPrice;

                        num = num + 1;
                        newPrice = itemTotalPrice + cartProductPrice;

                        float displayPrice = finalAllPrice + cartProductPrice;

                        s.cartItemNumber.setText(num + "");
                        s.cartItemPrice.setText(String.format("%.2f",newPrice) + "");
                        allItemTotalPrice.setText(String.format("%.2f",displayPrice) + "");

                        Background bg3 = new Background(CHANGE_ITEM_NUMBER_AND_PRICE);
                        bg3.execute(String.valueOf(num), String.valueOf(newPrice), String.valueOf(cartItemID));
                    }
                });

                shoppingCartViewHolder.minusNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int num = Integer.parseInt(s.cartItemNumber.getText().toString());
                        float itemTotalPrice = Float.parseFloat(s.cartItemPrice.getText().toString());
                        float finalAllPrice = Float.parseFloat(allItemTotalPrice.getText().toString());
                        float newPrice = cartProductPrice;

                        num = num - 1;

                        if (num < 1){
                            num = 1;
                        } else {
                            newPrice = itemTotalPrice - cartProductPrice;
                            float displayPrice = finalAllPrice - cartProductPrice;
                            allItemTotalPrice.setText(String.format("%.2f",displayPrice) + "");
                        }

                        s.cartItemNumber.setText(num + "");
                        s.cartItemPrice.setText(String.format("%.2f",newPrice) + "");

                        Background bg3 = new Background(CHANGE_ITEM_NUMBER_AND_PRICE);
                        bg3.execute(String.valueOf(num), String.valueOf(newPrice), String.valueOf(cartItemID));
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
                bg = new Background(Background.FETCH_CART_ITEM);
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
