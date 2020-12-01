package com.example.damien.onlinegrocerystore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login extends AppCompatActivity {
    public static final String EXTRA_ID = "com.example.damien.onlinegrocerystore.ID";
    public static final String EXTRA_USERNAME = "com.example.damien.onlinegrocerystore.USERNAME";
    public static final String EXTRA_PASSWORD = "com.example.damien.onlinegrocerystore.PASSWORD";
    public static final String EXTRA_PHONE = "com.example.damien.onlinegrocerystore.PHONE";
    public static final String EXTRA_WALLET_BALANCE = "com.example.damien.onlinegrocerystore.WALLET_BALANCE";

    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputUsername = findViewById(R.id.textInputUsername);
        textInputPassword = findViewById(R.id.textInputPassword);
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private boolean validateUsername(){
        String usernameInput = textInputUsername.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()){
            textInputUsername.setError("This field cannot be empty!");
            return false;
        } else {
            textInputUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword(){
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()){
            textInputPassword.setError("This field cannot be empty!");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    public void btnLogin_onClicked(View view) {
        if (!validateUsername() | !validatePassword()){
            return;
        } else {
            String passwordInput = textInputPassword.getEditText().getText().toString().trim();
            String usernameInput = textInputUsername.getEditText().getText().toString().trim();

            Background bg = new Background();
            String hashed_password = MD5(passwordInput);
            bg.execute(usernameInput, hashed_password);
        }
    }

    public void btnRegister_onClicked(View view) {
        startActivity(new Intent(Login.this,Register.class));
    }

    public static String MD5(String password) {
        byte[] bytes = password.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {}
        byte[] hashed_password = md.digest(bytes);
        StringBuilder sb = new StringBuilder();

        for (byte b: hashed_password) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
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
            Intent i = new Intent(Login.this, Homepage.class);
            String passwordInput = textInputPassword.getEditText().getText().toString().trim();

            try {
                if (result.next()) {
                    i.putExtra(EXTRA_ID, result.getInt(1));
                    i.putExtra(EXTRA_USERNAME, result.getString(2));
                    i.putExtra(EXTRA_PASSWORD, passwordInput);
                    i.putExtra(EXTRA_PHONE, result.getString(4));
                    i.putExtra(EXTRA_WALLET_BALANCE, result.getFloat(5));
                    startActivity(i);
                }
                else {
                    Toast.makeText(Login.this, "Username or Password invalid, please try again!", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(Login.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(Login.this);
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
                String query = "SELECT id, username, password, phone_no , wallet_balance FROM account WHERE username LIKE ? AND password=?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, strings[0]);
                stmt.setString(2, strings[1]);
                result = stmt.executeQuery();
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
