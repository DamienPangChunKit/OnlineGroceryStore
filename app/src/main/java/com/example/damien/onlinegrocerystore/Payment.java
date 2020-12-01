package com.example.damien.onlinegrocerystore;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Payment extends AppCompatActivity {
    TextInputLayout textInputAddress;
    TextInputLayout textInputDescription;

    private int customer_id;
    private float finalPrice;
    private float myBalance;
    private String paymentDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        textInputAddress = findViewById(R.id.textInputAddress);
        textInputDescription = findViewById(R.id.textInputDescription);

        Intent i = getIntent();
        customer_id = i.getIntExtra(Login.EXTRA_ID, 0);
        finalPrice = i.getFloatExtra("finalPrice", 0);
        myBalance = i.getFloatExtra(Login.EXTRA_WALLET_BALANCE, 0);
    }

    private boolean validateAddress(){
        String addressInput = textInputAddress.getEditText().getText().toString().trim();

        if (addressInput.isEmpty()){
            textInputAddress.setError("This field cannot be empty!");
            return false;
        } else {
            textInputAddress.setError(null);
            return true;
        }
    }

    private boolean validateMoney(){
        if (myBalance < finalPrice){
            Toast.makeText(this, "Please make sure you have enough money in the apps before making payment!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public void btnMadePayment_onClicked(View view) {
        if (!validateAddress() || !validateMoney()){
            return;
        } else {
            openConfirmationDialog();
        }
    }

    private void openConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Payment.this);
        builder.setTitle("Are you sure want to order the following item ?");
        builder.setMessage("Make sure all the information input are correct!");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String addressInput = textInputAddress.getEditText().getText().toString();
                String descriptionInput = textInputDescription.getEditText().getText().toString();
                myBalance = myBalance - finalPrice;

                if (descriptionInput.isEmpty()){
                    descriptionInput = "-";
                }

                Date currentDateTime = Calendar.getInstance().getTime();
                paymentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentDateTime);

                Background bg = new Background();
                bg.execute(addressInput, descriptionInput, String.valueOf(myBalance), paymentDateTime);
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
                i.putExtra("TOTAL_BALANCE_AFTER_PAID", myBalance);
                setResult(RESULT_OK, i);
                finish();
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(Payment.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(Payment.this);
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
                String query = "UPDATE customer_order SET address = ?, description = ?, payment_datetime = ?, status = ? WHERE customer_id = ? AND status = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, strings[0]);
                stmt.setString(2, strings[1]);
                stmt.setString(3, strings[3]);
                stmt.setString(4, "delivering");
                stmt.setInt(5, customer_id);
                stmt.setString(6, "inside cart");

                String query2 = "UPDATE account SET wallet_balance = ? WHERE id = ?";
                stmt2 = conn.prepareStatement(query2);
                stmt2.setFloat(1, Float.parseFloat(strings[2]));
                stmt2.setInt(2, customer_id);

                stmt.executeUpdate();
                stmt2.executeUpdate();
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
