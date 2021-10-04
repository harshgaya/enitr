package com.harsh.enitr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.MediaRouteButton;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    String[] cityNames={"Gaya","Patna","Rourkela","NIT Rourkela","Jehanabad","Daudnagar"};

    private EditText nameet;
    private Button registerbtn;
    private  String selectedCity;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar progressBar;
    private Dialog loadingDialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


                           nameet = findViewById(R.id.nameet);
        registerbtn = findViewById(R.id.registerbtn);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.sign_up_progressbar);

        /////////// loading dialog
        loadingDialog = new Dialog(RegisterActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ///// loading dialog

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.show();
                Register();
            }
        });

        nameet.addTextChangedListener(new TextWatcher() {
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

//        Spinner spin = (Spinner) findViewById(R.id.simpleSpinner);
//        spin.setOnItemSelectedListener(RegisterActivity.this);


        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,cityNames);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //spin.setAdapter(aa);
    }


//    @Override
//    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
//        selectedCity = cityNames[position];
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> adapterView) {
//
//        registerbtn.setEnabled(false);
//    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(nameet.getText())) {
            registerbtn.setEnabled(true);
            registerbtn.setTextColor(Color.rgb(255, 255, 255));

        } else {
            registerbtn.setEnabled(false);
            registerbtn.setTextColor(Color.argb( 50,255, 255, 255));
        }
    }

    private void Register() {
        Map<String, Object> userdata = new HashMap<>();
        userdata.put("fullname", nameet.getText().toString());
        userdata.put("email",firebaseAuth.getCurrentUser().getEmail());
        //userdata.put("city",selectedCity);
        userdata.put("profile","");

        firebaseFirestore.collection("USERS").document(firebaseAuth.getUid())
                .set(userdata).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "Registered !", Toast.LENGTH_SHORT).show();
                }
            }
        });


        firebaseFirestore.collection("USERS").document(firebaseAuth.getUid())
                .set(userdata)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            CollectionReference userDataReference = firebaseFirestore.collection("USERS").document(firebaseAuth.getUid()).collection("USER_DATA");

                            ///////Maps
                            Map<String, Object> wishlistMap = new HashMap<>();
                            wishlistMap.put("list_size", (long) 0);

                            Map<String, Object> ratingsMap = new HashMap<>();
                            ratingsMap.put("list_size", (long) 0);

                            Map<String, Object> cartMap = new HashMap<>();
                            cartMap.put("list_size", (long) 0);

                            Map<String, Object> myAddressesMap = new HashMap<>();
                            myAddressesMap.put("list_size", (long) 0);

                            Map<String, Object> notificationsMap = new HashMap<>();
                            myAddressesMap.put("list_size", (long) 0);
                            /////Maps

                            final List<String> documentNames = new ArrayList<>();
                            documentNames.add("MY_WISHLIST");
                            documentNames.add("MY_RATINGS");
                            documentNames.add("MY_CART");
                            documentNames.add("MY_ADDRESSES");
                            documentNames.add("MY_NOTIFICATIONS");


                            List<Map<String, Object>> documentFields = new ArrayList<>();
                            documentFields.add(wishlistMap);
                            documentFields.add(ratingsMap);
                            documentFields.add(cartMap);
                            documentFields.add(myAddressesMap);
                            documentFields.add(notificationsMap);


                            for (int x = 0; x < documentNames.size(); x++) {
                                final int finalX = x;
                                userDataReference.document(documentNames.get(x))
                                        .set(documentFields.get(x)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            if (finalX == documentNames.size() - 1) {
                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(mainIntent);
                                                RegisterActivity.this.finish();
                                                loadingDialog.dismiss();
                                            }

                                        } else {
                                            loadingDialog.dismiss();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            registerbtn.setEnabled(true);
                                            registerbtn.setTextColor(Color.rgb(255, 255, 255));
                                            String error = task.getException().getMessage();
                                            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }


                        } else {

                            String error = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });



}


}