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
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;

public class EditProfile extends AppCompatActivity {
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[a-zA-Z])" +
                    "(.{8,})" +
                    "$");

    TextInputLayout layoutUsername;
    TextInputLayout layoutPassword;
    TextInputLayout layoutPhone;
    EditText mETUsername;
    EditText mETPassword;
    EditText mETPhone;

    private int id;
    private String username;
    private String password;
    private String phoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        layoutUsername = findViewById(R.id.textInputUsername);
        layoutPassword = findViewById(R.id.textInputPassword);
        layoutPhone = findViewById(R.id.textInputPhone);
        mETUsername = findViewById(R.id.etUsername);
        mETPassword = findViewById(R.id.etPassword);
        mETPhone = findViewById(R.id.etPhone);

        Intent i = getIntent();
        id = i.getIntExtra(Login.EXTRA_ID, -1);
        username = i.getStringExtra(Profile.USERNAME);
        password = i.getStringExtra(Profile.PASSWORD);
        phoneNo = i.getStringExtra(Profile.PHONE);

        mETUsername.setText(username);
        mETPassword.setText(password);
        mETPhone.setText(phoneNo);

    }

    private boolean validateUsername() {
        String usernameInput = layoutUsername.getEditText().getText().toString().trim();

        if (usernameInput.isEmpty()) {
            layoutUsername.setError("This field cannot be empty!");
            return false;
        } else if (usernameInput.length() > 15) {
            layoutUsername.setError("Username was too long!");
            return false;
        } else {
            layoutUsername.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = layoutPassword.getEditText().getText().toString().trim();

        if (passwordInput.isEmpty()) {
            layoutPassword.setError("This field cannot be empty!");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            layoutPassword.setError("Password must contain at least 8 character, letter and number!");
            return false;
        } else {
            layoutPassword.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phoneInput = layoutPhone.getEditText().getText().toString().trim();

        if (phoneInput.isEmpty()) {
            layoutPhone.setError("This field cannot be empty!");
            return false;
        } else if (phoneInput.charAt(0) != '0' |
                phoneInput.charAt(1) != '1' |
                phoneInput.length() < 10 |
                phoneInput.length() > 11) {
            layoutPhone.setError("Please input phone number format as 0123456789");
            return false;
        } else {
            layoutPhone.setError(null);
            return true;
        }
    }

    public void btnSave_onClicked(View view) {
        if (!validateUsername() | !validatePassword() | !validatePhone()){
            return;
        } else {
            String usernameInput = layoutUsername.getEditText().getText().toString().trim();
            String passwordInput = layoutPassword.getEditText().getText().toString().trim();
            String phoneInput = layoutPhone.getEditText().getText().toString().trim();
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
        private PreparedStatement stmt;
        private ProgressDialog progressDialog;
        private String usernameEdit;
        private String passwordEdit;
        private String phoneEdit;

        public Background() {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            closeConn();

            try {
                if (result.isEmpty()) {
                    String passwordInput = layoutPassword.getEditText().getText().toString();

                    Intent i = new Intent();
                    i.putExtra("USERNAME_EDIT", usernameEdit);
                    i.putExtra("PASSWORD_EDIT", passwordInput);
                    i.putExtra("PHONE_EDIT", phoneEdit);
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    Toast.makeText(EditProfile.this, result, Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                Log.e("ERROR BACKGROUND", e.getMessage());
                Toast.makeText(EditProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EditProfile.this);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage("Processing data");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            conn = connectDB();
            usernameEdit = strings[0];
            passwordEdit = strings[1];
            phoneEdit = strings[2];

            if (conn == null) {
                return null;
            }
            try {
                String query2 = "SELECT username, phone_no FROM account WHERE (username LIKE ? OR phone_no = ?) AND id <> ?";
                stmt = conn.prepareStatement(query2);
                stmt.setString(1, usernameEdit);
                stmt.setString(2, phoneEdit);
                stmt.setInt(3, id);
                ResultSet resultSet = stmt.executeQuery();

                if (resultSet.next()) {
                    String userName = resultSet.getString(1);
                    String phNo = resultSet.getString(2);

                    if (resultSet.next() || userName.toLowerCase().equals(username.toLowerCase()) && phNo.equals(phoneEdit)) {
                        return getString(R.string.name_and_phone_exists);
                    }

                    if (phNo.equals(phoneEdit)) {
                        return getString(R.string.phone_exists);
                    }

                    return getString(R.string.name_exists);

                } else {
                    String query = "UPDATE account SET username=?, password=?, phone_no=? WHERE id=?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, usernameEdit);
                    stmt.setString(2, passwordEdit);
                    stmt.setString(3, phoneEdit);
                    stmt.setInt(4, id);
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
