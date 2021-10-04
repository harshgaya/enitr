package com.harsh.enitr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RequestBookActivity extends AppCompatActivity {

    private Spinner spinner;
    private EditText nameofBook, authorofBook, MobileNo, Address, Addresss;
    private Button submit;
    private String[] conditionofBook;
    private String selectedConditionofBook;
    private Dialog loadingDialog;
    private String Request_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_book);

        //spinner = findViewById(R.id.spinner);
        nameofBook = findViewById(R.id.nameofBook);
        authorofBook = findViewById(R.id.authorofBook);
        MobileNo = findViewById(R.id.MobileNo);
        submit = findViewById(R.id.submit);
        Address = findViewById(R.id.Address);
        Addresss = findViewById(R.id.Addresss);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Request Your Book");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Request_id = UUID.randomUUID().toString().substring(0, 28);

        /////////// loading dialog
        loadingDialog = new Dialog(RequestBookActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ///// loading dialog

        conditionofBook = getResources().getStringArray(R.array.conditionofBook);

        nameofBook.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        authorofBook.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        MobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        Address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        Addresss.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }


            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.show();
                sendEmail();
                submitToDatabase();
                sendSMS();
                Toast.makeText(RequestBookActivity.this, "Your request for the book " + nameofBook.getText().toString() + " has been submitted successfully!", Toast.LENGTH_SHORT).show();

            }
        });


    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(nameofBook.getText().toString())) {
            if (!TextUtils.isEmpty(authorofBook.getText().toString())) {
                if (!TextUtils.isEmpty(MobileNo.getText().toString())) {
                    if (!TextUtils.isEmpty(Address.getText().toString())) {

                        submit.setEnabled(true);
                        submit.setTextColor(Color.rgb(255, 255, 255));

                    }else{
                        submit.setEnabled(false);
                        submit.setTextColor(Color.argb(50, 255, 255, 255));
                    }
                } else {
                    submit.setEnabled(false);
                    submit.setTextColor(Color.argb(50, 255, 255, 255));
                }
            } else {
                submit.setEnabled(false);
                submit.setTextColor(Color.argb(50, 255, 255, 255));
            }
        } else {
            submit.setEnabled(false);
            submit.setTextColor(Color.argb(50, 255, 255, 255));
        }
    }

    private void sendEmail() {

        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    final String Selectedcity = task.getResult().getString("city");

                    final String nameOfBook = nameofBook.getText().toString();
                    final String authorOfBook = authorofBook.getText().toString();
                    final String mobileNo = MobileNo.getText().toString();
                    final String Area = Address.getText().toString();
                    final String Nearby = Addresss.getText().toString();
                    final String RequestID = Request_id;
//
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                GMailSender sender = new GMailSender(
                                        "yourenitr@gmail.com",
                                        "acglwvstettmursu");
                                //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                sender.sendMail("Request of the Book", "Request of the Book -" + "\n"
                                                + "Name of The Book :- " + nameOfBook + "\n"
                                                + "Author of Book :- " + authorOfBook + "\n"
                                                + "Mobile No:- " + mobileNo + "\n"
                                                + "Request ID :- " + RequestID + "\n"
                                                + "City :- " + Selectedcity + "\n"
                                                + "Area :- " + Area + "\n"
                                                + "Nearby :- " + Nearby + "\n"

                                                + "This is a system generated mail don't reply to these messages." + "\n",


                                        "yourenitr@gmail.com",
                                        "harshgaya42@gmail.com");


                            } catch (Exception e) {
                                Toast.makeText(RequestBookActivity.this, "Error", Toast.LENGTH_LONG).show();
                            }
                        }

                    }).start();

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                GMailSender sender = new GMailSender(
                                        "thecarthive@gmail.com",
                                        "rvittdvpwjaqwgwn");
                                //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                sender.sendMail("Request of the Book", "Request of the Book -" + "\n"
                                                + "Name of The Book :- " + nameOfBook + "\n"
                                                + "Author of Book :- " + authorOfBook + "\n"
                                                + "Mobile No :- " + mobileNo + "\n"
                                                + "Your City :- " + Selectedcity + "\n"
                                                + "Area :- " + Area + "\n"
                                                + "Nearby :- " + Nearby + "\n"
                                                + "Request ID :- " + RequestID + "\n"

                                                + "Your request for the book " + nameOfBook + " has been submitted ." + " We will call you if the book is available. " + "\n"

                                                + "Thanks for shopping with Us !" + "\n"
                                                + "This is a system generated mail don't reply to these messages." + "\n",


                                        "thecarthive@gmail.com",
                                        DBqueries.email);


                            } catch (Exception e) {
                                Toast.makeText(RequestBookActivity.this, "Error", Toast.LENGTH_LONG).show();
                            }
                        }

                    }).start();

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                GMailSender sender = new GMailSender(
                                        "yourenitr@gmail.com",
                                        "acglwvstettmursu");
                                //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                sender.sendMail("Request of the Book", "Request of the Book -" + "\n"
                                                + "Name of The Book :- " + nameOfBook + "\n"
                                                + "Author of Book :- " + authorOfBook + "\n"
                                                + "Mobile No:- " + mobileNo + "\n"
                                                + "Request ID :- " + RequestID + "\n"
                                                + "City :- " + Selectedcity + "\n"
                                                + "Area :- " + Area + "\n"
                                                + "Nearby :- " + Nearby + "\n"

                                                + "This is a system generated mail don't reply to these messages." + "\n",


                                        "yourenitr@gmail.com",
                                        "rakeshraushan258@gmail.com");


                            } catch (Exception e) {
                                Toast.makeText(RequestBookActivity.this, "Error", Toast.LENGTH_LONG).show();
                            }
                        }

                    }).start();
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                GMailSender sender = new GMailSender(
                                        "yourenitr@gmail.com",
                                        "acglwvstettmursu");
                                //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                sender.sendMail("Request of the Book", "Request of the Book -" + "\n"
                                                + "Name of The Book :- " + nameOfBook + "\n"
                                                + "Author of Book :- " + authorOfBook + "\n"
                                                + "Mobile No:- " + mobileNo + "\n"
                                                + "Request ID :- " + RequestID + "\n"
                                                + "City :- " + Selectedcity + "\n"
                                                + "Area :- " + Area + "\n"
                                                + "Nearby :- " + Nearby + "\n"

                                                + "This is a system generated mail don't reply to these messages." + "\n",


                                        "yourenitr@gmail.com",
                                        "kumardinesh0316@gmail.com");


                            } catch (Exception e) {
                                Toast.makeText(RequestBookActivity.this, "Error", Toast.LENGTH_LONG).show();
                            }
                        }

                    }).start();
                }
            }
        });
////kumardinesh0316@gmail.com


    }

    private void sendSMS() {
        //sent confirmation SMS
        final String nameOfBook = nameofBook.getText().toString();
        String SMS_API = "https://www.fast2sms.com/dev/bulk";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SMS_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                /////////// nothing
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ////////nothing
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authorization", "eQkdT7BD4FrzCx5lHUvsWiaA1pqYZ8EmPIRjK2Nch63J0yVnGwUX2herZqH10QRaNMfBtCcTOolVKn7P");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> body = new HashMap<>();
                body.put("sender_id", "FSTSMS");
                body.put("language", "english");
                body.put("route", "qt");
                body.put("numbers", "9304136129");
                body.put("message", "22612");
                body.put("variables", "{#FF#}");
                body.put("variables_values", nameOfBook);


                return body;

            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        //sent confirmation SMS


    }

    private void submitToDatabase() {

        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    //final String Selectedcity = task.getResult().getString("city");

                    Map<String, Object> userdata = new HashMap<>();
                    userdata.put("Name of Book", nameofBook.getText().toString());
                    userdata.put("Author of Book", authorofBook.getText().toString());
                    userdata.put("Mobile No", MobileNo.getText().toString());
                    userdata.put("User Mail", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    userdata.put("Time", FieldValue.serverTimestamp());
                    //userdata.put("City", Selectedcity);
                    userdata.put("Area", Address.getText().toString());
                    userdata.put("Nearby", Addresss.getText().toString());

                    FirebaseFirestore.getInstance().collection("BOOK_REQUESTS").document(Request_id).
                            set(userdata)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        loadingDialog.dismiss();
                                        nameofBook.setText(null);
                                        authorofBook.setText(null);
                                        MobileNo.setText(null);
                                        Address.setText(null);
                                        Addresss.setText(null);
                                        Toast.makeText(RequestBookActivity.this, "Request for your book have been submitted successfully .", Toast.LENGTH_SHORT).show();


                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(RequestBookActivity.this, error, Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                }
                            });
                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}