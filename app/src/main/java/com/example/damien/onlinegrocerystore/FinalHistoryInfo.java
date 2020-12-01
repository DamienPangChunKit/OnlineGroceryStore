package com.example.damien.onlinegrocerystore;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FinalHistoryInfo extends AppCompatActivity {
    TextView TVcartID;
    TextView TVcartAddress;
    TextView TVcartItemID;
    TextView TVcartItemName;
    TextView TVcartDescription;
    TextView TVcartItemPrice;
    TextView TVcartTotalItem;
    TextView TVcartTotalPrice;
    TextView TVcartPaymentDate;
    TextView TVcartPaymentTime;
    TextView TVcartStatus;
    TextView TVwarning;
    ImageView backButton;
    ImageView imgBackground;
    Button btnReceived;

    private int customer_id;
    private String cartID;
    private String cartAddress;
    private String cartItemID;
    private String cartItemName;
    private String cartDescription;
    private String cartItemPrice;
    private String cartTotalItem;
    private String cartTotalPrice;
    private String cartPaymentDate;
    private String cartPaymentTime;
    private String cartStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_history_info);

        TVwarning = findViewById(R.id.tvWarning);
        imgBackground = findViewById(R.id.imgBackground);
        btnReceived = findViewById(R.id.btnReceive);
        backButton = findViewById(R.id.btnBack);
        TVcartID = findViewById(R.id.tvCartID);
        TVcartAddress = findViewById(R.id.tvCartAddress);
        TVcartItemID = findViewById(R.id.tvCartItemID);
        TVcartItemName = findViewById(R.id.tvCartItemName);
        TVcartDescription = findViewById(R.id.tvCartDescription);
        TVcartItemPrice = findViewById(R.id.tvCartItemPrice);
        TVcartTotalItem = findViewById(R.id.tvCartTotalItem);
        TVcartTotalPrice = findViewById(R.id.tvCartTotalPrice);
        TVcartPaymentDate = findViewById(R.id.tvCartPaymentDate);
        TVcartPaymentTime = findViewById(R.id.tvCartPaymentTime);
        TVcartStatus = findViewById(R.id.tvCartStatus);

        Intent i = getIntent();
        customer_id = getIntent().getIntExtra(Login.EXTRA_ID, -1);
        cartID = i.getStringExtra("cartID");
        cartAddress = i.getStringExtra("cartaddress");
        cartItemID = i.getStringExtra("cartItemID");
        cartItemName = i.getStringExtra("cartItemName");
        cartDescription = i.getStringExtra("cartDescription");
        cartItemPrice = i.getStringExtra("cartItemPrice");
        cartTotalItem = i.getStringExtra("cartTotalItem");
        cartTotalPrice = i.getStringExtra("cartTotalPrice");
        cartPaymentDate = i.getStringExtra("cartPaymentDate");
        cartPaymentTime = i.getStringExtra("cartPaymentTime");
        cartStatus = i.getStringExtra("cartStatus");

        TVcartID.setText(cartID + " ");
        TVcartAddress.setText(cartAddress + " ");
        TVcartItemID.setText(cartItemID + " ");
        TVcartItemName.setText(cartItemName + " ");
        TVcartDescription.setText(cartDescription + " ");
        TVcartItemPrice.setText(cartItemPrice + " ");
        TVcartTotalItem.setText(cartTotalItem + " ");
        TVcartTotalPrice.setText(cartTotalPrice + " ");
        TVcartPaymentDate.setText(cartPaymentDate + " ");
        TVcartPaymentTime.setText(cartPaymentTime + " ");
        TVcartStatus.setText(cartStatus + " ");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (cartStatus.equals("completed")){
            btnReceived.setVisibility(View.INVISIBLE);
            TVwarning.setVisibility(View.INVISIBLE);
            imgBackground.setVisibility(View.INVISIBLE);
        }
    }

    public void btnReceive_onClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FinalHistoryInfo.this);
        builder.setTitle("Are you sure your item has been receive ?");
        builder.setMessage("Changes cannot be made after confirm!");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Background bg = new Background();
                bg.execute();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setIcon(R.drawable.ic_payment);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public class Background extends AsyncTask<String, Void, ResultSet> {
        private static final String LIBRARY = "com.mysql.jdbc.Driver";
        private static final String USERNAME = "sql12377787";
        private static final String DB_NAME = "sql12377787";
        private static final String PASSWORD = "WsCZjPsSEW";
        private static final String SERVER = "sql12.freemysqlhosting.net";

        private Connection conn;
        private PreparedStatement stmt, stmt2;
        private ProgressDialog progressDialog;

        public Background() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        @Override
        protected void onPostExecute(ResultSet result) {
            super.onPostExecute(result);

            try {
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                finish();
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(FinalHistoryInfo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
            finally {
                progressDialog.hide();
                try { result.close(); } catch (Exception e) { /* ignored */ }
                closeConn();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(FinalHistoryInfo.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing data");
            progressDialog.show();
        }

        @Override
        protected ResultSet doInBackground(String... strings) {
            conn = connectDB();
            ResultSet result = null;

            if (conn == null) {
                return null;
            }
            try {
                String query = "UPDATE customer_order SET status = ? WHERE customer_id = ? AND status = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, "completed");
                stmt.setInt(2, customer_id);
                stmt.setString(3, "delivering");
                stmt.executeUpdate();
            }
            catch (Exception e) {
                Log.e("ERROR MySQL Statement", e.getMessage());
            }
            return result;
        }

        private Connection connectDB(){
            try {
                Class.forName(LIBRARY);
                return DriverManager.getConnection("jdbc:mysql://" + SERVER + "/" + DB_NAME, USERNAME, PASSWORD);
            } catch (Exception e) {
                Log.e("Error on Connection", e.getMessage());
                return null;
            }
        }

        public void closeConn() {
            try {
                stmt.close();
            } catch (Exception e) {
                /* ignored */
            }
            try {
                conn.close();
            } catch (Exception e) { /* ignored */ }
        }
    }
}
