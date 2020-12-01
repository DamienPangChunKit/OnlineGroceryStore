package com.example.damien.onlinegrocerystore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TopSalesProductDetails extends AppCompatActivity {
    private int customer_id;
    private int topSalesID;
    private int topSalesItemID;
    private String topSalesName;
    private String topSalesDescription;
    private Float topSalesPrice;
    private String topSalesQuantity;
    private Float totalPrice;
    private int totalItem = 0;

    ImageView topSalesProBackButton;
    ImageView topSalesProImage;
    TextView topSalesProName;
    TextView topSalesProDescription;
    TextView topSalesProPrice;
    TextView topSalesProQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_sales_product_details);

        topSalesProName = findViewById(R.id.tvName);
        topSalesProDescription = findViewById(R.id.tvDescription);
        topSalesProPrice = findViewById(R.id.tvPrice);
        topSalesProQuantity = findViewById(R.id.tvQuantity);
        topSalesProImage = findViewById(R.id.imgTopSalesProDetail);
        topSalesProBackButton = findViewById(R.id.btnBack);

        Intent i = getIntent();
        customer_id = i.getIntExtra(Login.EXTRA_ID, 0);
        topSalesID = i.getIntExtra("topSalesID", 0);
        topSalesName = i.getStringExtra("topSalesName");
        topSalesDescription = i.getStringExtra("topSalesDescription");
        topSalesQuantity = i.getStringExtra("topSalesQuantity");
        topSalesPrice = i.getFloatExtra("topSalesPrice", 0);
        topSalesItemID = i.getIntExtra("topSalesItemID", 0);

        topSalesProName.setText(topSalesName);
        topSalesProDescription.setText(topSalesDescription);
        topSalesProPrice.setText("RM " + topSalesPrice + "0");
        topSalesProQuantity.setText(topSalesQuantity);

        switch (topSalesID){
            case 1:
                topSalesProImage.setImageResource(R.drawable.apple);
                break;
            case 2:
                topSalesProImage.setImageResource(R.drawable.kiwi);
                break;
            case 3:
                topSalesProImage.setImageResource(R.drawable.strawberry);
                break;
            case 4:
                topSalesProImage.setImageResource(R.drawable.lemon);
                break;
            case 5:
                topSalesProImage.setImageResource(R.drawable.orange);
                break;
        }

        topSalesProBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void btnAddToCart_onClicked(View view) {
        totalItem = totalItem + 1;
        totalPrice = topSalesPrice;

        Background bg = new Background();
        bg.execute(topSalesName, String.valueOf(totalItem), String.valueOf(topSalesPrice), String.valueOf(totalPrice), String.valueOf(topSalesItemID));
    }

    public class Background extends AsyncTask<String, Void, String> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";
        private static final String USERNAME = "sql12377787";
        private static final String DB_NAME = "sql12377787";
        private static final String PASSWORD = "WsCZjPsSEW";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt, stmt2, stmt3;
        private ProgressDialog progressDialog;

        public Background() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();

            try {
                if (result.isEmpty()){
                    Intent i = new Intent();
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Toast.makeText(TopSalesProductDetails.this, result, Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(TopSalesProductDetails.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(TopSalesProductDetails.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing data");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            conn = connectDB();
            if (conn == null) {
                return null;
            }
            try {
                String productName = strings[0];
                String totalProduct = strings[1];
                String productPrice = strings[2];
                String productTotalPrice = strings[3];
                String productItemID = strings[4];

                String query2 = "SELECT customer_id, item_name, total_item FROM customer_order WHERE customer_id LIKE ? AND item_id = ?";
                stmt2 = conn.prepareStatement(query2);
                stmt2.setInt(1, customer_id);
                stmt2.setInt(2, Integer.parseInt(productItemID));
                ResultSet resultSet = stmt2.executeQuery();

                if (resultSet.next()){
                    return "Item is already in the cart!";

                } else {

                    String query = "insert into customer_order (customer_id, item_name, total_item, status, item_price, total_price, item_id) values (?, ?, ?, ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(query);
                    stmt.setInt(1, customer_id);
                    stmt.setString(2, productName);
                    stmt.setInt(3, Integer.parseInt(totalProduct));
                    stmt.setString(4, "inside cart");
                    stmt.setFloat(5, Float.parseFloat(productPrice));
                    stmt.setFloat(6, Float.parseFloat(productTotalPrice));
                    stmt.setInt(7, Integer.parseInt(productItemID));
                    stmt.executeUpdate();
                }
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return "";
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
}
