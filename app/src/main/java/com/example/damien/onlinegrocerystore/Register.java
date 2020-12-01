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
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[a-zA-Z])" +
                    "(.{8,})" +
                    "$");

    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;
    private TextInputLayout textInputConfirmPassword;
    private TextInputLayout textInputPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textInputUsername = findViewById(R.id.textInputUsername);
        textInputPassword = findViewById(R.id.textInputPassword);
        textInputConfirmPassword = findViewById(R.id.textInputConfirmPassword);
        textInputPhone = findViewById(R.id.textInputPhone);
    }

    private boolean validateUsername() {
        String usernameInput = textInputUsername.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()) {
            textInputUsername.setError("This field cannot be empty!");
            return false;
        } else if (usernameInput.length() > 15) {
            textInputUsername.setError("Username was too long!");
            return false;
        } else {
            textInputUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            textInputPassword.setError("This field cannot be empty!");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            textInputPassword.setError("Password must contain at least 8 character, letter and number!");
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }
    }

    private boolean validateConfirmPassword(){
        String confirmPassInput = textInputConfirmPassword.getEditText().getText().toString().trim();
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();

        if (confirmPassInput.isEmpty()) {
            textInputConfirmPassword.setError("This field cannot be empty!");
            return false;
        } else if (!confirmPassInput.equals(passwordInput)) {
            textInputConfirmPassword.setError("The Password does not match!");
            return false;
        } else {
            textInputConfirmPassword.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phoneInput = textInputPhone.getEditText().getText().toString().trim();

        if (phoneInput.isEmpty()) {
            textInputPhone.setError("This field cannot be empty!");
            return false;
        } else if (phoneInput.charAt(0) != '0' |
                phoneInput.charAt(1) != '1' |
                phoneInput.length() < 10 |
                phoneInput.length() > 11) {
            textInputPhone.setError("Please input phone number format as 0123456789");
            return false;
        } else {
            textInputPhone.setError(null);
            return true;
        }
    }

    public void btnRegister_onClicked(View view) {
        if (!validateUsername() | !validatePassword() | !validatePhone() | !validateConfirmPassword()){
            return;
        } else {
            String passwordInput = textInputPassword.getEditText().getText().toString().trim();
            String usernameInput = textInputUsername.getEditText().getText().toString().trim();
            String phoneInput = textInputPhone.getEditText().getText().toString().trim();
            String hashed_password = MD5(passwordInput);

            Background bg = new Background();
            bg.execute(usernameInput, hashed_password, phoneInput);

        }
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

    public class Background extends AsyncTask<String, Void, String> {
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();

            try {
                if (result.isEmpty()){
                    startActivity(new Intent(Register.this, Login.class));
                } else {
                    if (result.equals(getString(R.string.name_and_phone_exists))){
                        textInputUsername.setError(getString(R.string.name_exists));
                        textInputPhone.setError(getString(R.string.phone_exists));

                    } else if (result.equals(getString(R.string.phone_exists))){
                        textInputPhone.setError(getString(R.string.phone_exists));

                    } else if (result.equals(getString(R.string.name_exists))){
                        textInputUsername.setError(getString(R.string.name_exists));
                    }
                }
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(Register.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Register.this);
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
                String username = strings[0];
                String password = strings[1];
                String phone_no = strings[2];

                String query2 = "SELECT username, phone_no FROM account WHERE (username LIKE ? OR phone_no = ?) ";
                stmt2 = conn.prepareStatement(query2);
                stmt2.setString(1, username);
                stmt2.setString(2, phone_no);
                ResultSet resultSet = stmt2.executeQuery();

                if (resultSet.next()) {
                    String userName = resultSet.getString(1);
                    String phNo = resultSet.getString(2);

                    if (resultSet.next() || userName.toLowerCase().equals(username.toLowerCase()) && phNo.equals(phone_no)) {
                        return getString(R.string.name_and_phone_exists);
                    }

                    if (phNo.equals(phone_no)) {
                        return getString(R.string.phone_exists);
                    }

                    return getString(R.string.name_exists);


                } else {
                    String query = "insert into account (username, password, phone_no) values (?, ?, ?)";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, phone_no);
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
