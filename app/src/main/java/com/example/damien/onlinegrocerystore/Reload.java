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

public class Reload extends AppCompatActivity {
    TextInputLayout layoutAmount;
    TextInputLayout layoutPassword;

    private int id;
    private float totalBalance;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reload);

        layoutAmount = findViewById(R.id.textInputAmount);
        layoutPassword = findViewById(R.id.textInputPassword);

        Intent i = getIntent();
        id = i.getIntExtra(Login.EXTRA_ID, -1);
        totalBalance = i.getFloatExtra(Login.EXTRA_WALLET_BALANCE, -1);
        password = i.getStringExtra(Login.EXTRA_PASSWORD);
    }

    private boolean validatePassword() {
        String passwordInput = layoutPassword.getEditText().getText().toString();

        if (passwordInput.isEmpty()) {
            layoutPassword.setError("This field cannot be empty!");
            return false;
        } else if (!passwordInput.equals(password)) {
            layoutPassword.setError("Password does not match to the old one!");
            return false;
        } else {
            layoutPassword.setError(null);
            return true;
        }
    }

    private boolean validateAmount() {
        String amt = layoutAmount.getEditText().getText().toString();

        if (amt.isEmpty()){
            layoutAmount.setError("This field cannot be empty!");
            return false;
        } else {
            int amountTOP = Integer.parseInt(amt);
            if (amountTOP < 10) {
                layoutAmount.setError("Please reload at least RM 10!");
                return false;
            } else if (amountTOP > 1000) {
                layoutAmount.setError("Only can reload a maximum RM 1000!");
                return false;
            } else {
                layoutAmount.setError(null);
                return true;
            }
        }
    }

    public void btnReload_onClicked(View view) {
        if (!validateAmount() | !validatePassword()){
            return;
        } else {
            openConfirmationReload();
        }
    }

    private void openConfirmationReload() {
        String a = layoutAmount.getEditText().getText().toString();
        final int amount = Integer.parseInt(a);

        AlertDialog.Builder builder = new AlertDialog.Builder(Reload.this);
        builder.setTitle("Are you sure want to reload \nRM " + amount + " ?");
        builder.setMessage("Amount : RM " + amount + ".00");

        builder.setPositiveButton("Reload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                totalBalance = totalBalance + amount;

                Background bg = new Background();
                bg.execute(String.valueOf(totalBalance), String.valueOf(id));
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setIcon(R.drawable.ic_reload);

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
        private PreparedStatement stmt;
        private ProgressDialog progressDialog;

        public Background() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        @Override
        protected void onPostExecute(ResultSet result) {
            super.onPostExecute(result);
            Intent i = new Intent();

            try {
                i.putExtra("total_balance", totalBalance);
                setResult(RESULT_OK, i);
                finish();
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(Reload.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(Reload.this);
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
                String query = "UPDATE account SET wallet_balance = ? WHERE id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setFloat(1, Float.parseFloat(strings[0]));
                stmt.setInt(2, Integer.parseInt(strings[1]));
                stmt.executeUpdate();
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
}
