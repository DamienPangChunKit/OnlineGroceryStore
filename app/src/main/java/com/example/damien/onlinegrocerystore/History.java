package com.example.damien.onlinegrocerystore;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class History extends AppCompatActivity {
    BottomNavigationView mBottomNavigationView;

    private RecyclerView mRecyclerView;
    private historyAdapter mAdapter;

    private int customer_id;
    private String username;
    private String password;
    private String phoneNo;
    private float totalBalance;

    public static final int REQUEST_CODE8 = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mBottomNavigationView = findViewById(R.id.bottom_nav_bar);

        customer_id = getIntent().getIntExtra(Login.EXTRA_ID, -1);
        username = getIntent().getStringExtra(Login.EXTRA_USERNAME);
        password = getIntent().getStringExtra(Login.EXTRA_PASSWORD);
        phoneNo = getIntent().getStringExtra(Login.EXTRA_PHONE);
        totalBalance = getIntent().getFloatExtra(Login.EXTRA_WALLET_BALANCE, 1);

        Background bg = new Background(Background.FETCH_HISTORY);
        mRecyclerView = (RecyclerView) findViewById(R.id.historyRecycler);
        mAdapter = new historyAdapter(bg);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(History.this));

        mBottomNavigationView.setSelectedItemId(R.id.nav_history);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.nav_home:
                        Intent a = new Intent(History.this, Homepage.class);
                        a.putExtra(Login.EXTRA_ID, customer_id);
                        a.putExtra(Profile.USERNAME, username);
                        a.putExtra(Profile.PASSWORD, password);
                        a.putExtra(Profile.PHONE, phoneNo);
                        a.putExtra(Login.EXTRA_WALLET_BALANCE, totalBalance);
                        startActivity(a);
                        return true;
                    case R.id.nav_profile:
                        Intent b = new Intent(History.this, Profile.class);
                        b.putExtra(Login.EXTRA_ID, customer_id);
                        b.putExtra(Profile.USERNAME, username);
                        b.putExtra(Profile.PASSWORD, password);
                        b.putExtra(Profile.PHONE, phoneNo);
                        b.putExtra(Login.EXTRA_WALLET_BALANCE, totalBalance);
                        startActivity(b);
                        return true;
                    case R.id.nav_history:
                        return true;
                    case R.id.nav_logout:
                        Intent d = new Intent(History.this, Login.class);
                        startActivity(d);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE8){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "Your changes has been recorded, thank you for using our apps!", Toast.LENGTH_SHORT).show();
                mBottomNavigationView.setSelectedItemId(R.id.nav_history);
            }
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

        public static final int FETCH_HISTORY = 1;

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
                    case FETCH_HISTORY:
                        query = "SELECT id, address, item_id, item_price, total_price, total_item, description, payment_datetime, status, item_name FROM customer_order WHERE customer_id = ? AND (status = ? OR status = ?)";
                        stmt = conn.prepareStatement(query);
                        stmt.setInt(1, customer_id);
                        stmt.setString(2, "delivering");
                        stmt.setString(3, "completed");
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

    private class historyAdapter extends RecyclerView.Adapter<historyAdapter.historyHolder>{
        private LayoutInflater mInflater;
        private int itemCount;
        private Background bg;
        private ResultSet result;


        public historyAdapter(Background bg){
            this.bg = bg;
            updateResultSet();
            mInflater = LayoutInflater.from(History.this);
        }

        class historyHolder extends RecyclerView.ViewHolder{
            TextView TVshoppingCartID;
            TextView TVdateTime;
            TextView TVstatus;
            TableLayout mTableLayout;

            final historyAdapter mAdapter;

            public historyHolder(@NonNull View itemView, historyAdapter adapter){
                super(itemView);
                TVshoppingCartID = (TextView) itemView.findViewById(R.id.tvShoppingCartID);
                TVdateTime = (TextView) itemView.findViewById(R.id.tvDateTime);
                TVstatus = (TextView) itemView.findViewById(R.id.tvStatus);
                mTableLayout = (TableLayout) itemView.findViewById(R.id.layout_table);

                this.mAdapter = adapter;
            }
        }

        @NonNull
        @Override
        public historyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View mItemView = mInflater.inflate(R.layout.history_row, viewGroup, false);
            return new historyHolder(mItemView, this);
        }

        @Override
        public void onBindViewHolder(@NonNull historyHolder historyHolder, int position) {
            try {
                result.first();
                for (int i = 0; i < position; i++) {
                    result.next();
                }

                final String cartID = result.getString(1);
                final String cartaddress = result.getString(2);
                final String cartItemID = result.getString(3);
                final String cartItemPrice = result.getString(4);
                final String cartTotalPrice = result.getString(5);
                final String cartTotalItem = result.getString(6);
                final String cartDescription = result.getString(7);

                final String cartPaymentDateTime = result.getString(8);
                String[] separate = cartPaymentDateTime.split(" ");
                final String date = separate[0];
                final String time = separate[1];

                final String cartStatus = result.getString(9);
                final String cartItemName = result.getString(10);

                historyHolder.TVshoppingCartID.setText("" + cartID);
                historyHolder.TVdateTime.setText("" + date);
                historyHolder.TVstatus.setText("" + cartStatus);

                historyHolder.mTableLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(History.this, FinalHistoryInfo.class);
                        i.putExtra(Login.EXTRA_ID, customer_id);
                        i.putExtra("cartID", cartID);
                        i.putExtra("cartaddress", cartaddress);
                        i.putExtra("cartItemID", cartItemID);
                        i.putExtra("cartItemName", cartItemName);
                        i.putExtra("cartItemPrice", cartItemPrice);
                        i.putExtra("cartTotalPrice", cartTotalPrice);
                        i.putExtra("cartTotalItem", cartTotalItem);
                        i.putExtra("cartDescription", cartDescription);
                        i.putExtra("cartPaymentDate", date);
                        i.putExtra("cartPaymentTime", time.substring(0, time.length() - 2));
                        i.putExtra("cartStatus", cartStatus);
                        startActivityForResult(i, REQUEST_CODE8);
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
                bg = new Background(Background.FETCH_HISTORY);
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
