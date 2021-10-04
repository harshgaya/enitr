package com.harsh.enitr;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChangeCityFragment extends Fragment {

    String[] cityNames={"Gaya","Patna","Rourkela","NIT Rourkela","Jehanabad","Daudnagar"};

    private EditText nameet;
    private Button registerbtn;
    private  String selectedCity;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar progressBar;
    private Dialog loadingDialog;



    public ChangeCityFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_change_city, container, false);

        registerbtn = view.findViewById(R.id.registerbtn);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = view.findViewById(R.id.sign_up_progressbar);

        /////////// loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        //loadingDialog.getWindow().setBackgroundDrawable(R.drawable.slider_background);
        //loadingDialog.getWindow().setBackgroundDrawable(R.drawable.slider_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ///// loading dialog

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.show();
                Register();
            }
        });

        Spinner spin = (Spinner) view.findViewById(R.id.simpleSpinner);


        ArrayAdapter aa = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_item,cityNames);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                selectedCity = cityNames[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spin.setAdapter(aa);


        return view;
    }



//    private void Register() {
//        Map<String, Object> userdata = new HashMap<>();
//
//        userdata.put("city",selectedCity);
//
//        firebaseFirestore.collection("USERS").document(firebaseAuth.getUid())
//                .update(userdata).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()){
//                    DBqueries.clearData();
//                    Toast.makeText(getContext(), "Updated !", Toast.LENGTH_SHORT).show();
//                    loadingDialog.dismiss();
//
//
//                }
//            }
//        });
//
//    }

    private void Register() {
        Map<String, Object> userdata = new HashMap<>();
//        userdata.put("fullname", nameet.getText().toString());
//        userdata.put("email",firebaseAuth.getCurrentUser().getEmail());
        userdata.put("city",selectedCity);
//        userdata.put("profile","");

        firebaseFirestore.collection("USERS").document(firebaseAuth.getUid())
                .update(userdata).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    DBqueries.clearData();
                    Toast.makeText(getContext(), "Updated !", Toast.LENGTH_SHORT).show();
                }
            }
        });


        firebaseFirestore.collection(selectedCity).document("USERS").collection(firebaseAuth.getUid()).document(firebaseAuth.getUid())
                .set(userdata)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            CollectionReference userDataReference = firebaseFirestore.collection(selectedCity).document("USERS").collection(firebaseAuth.getUid()).document("USER_DATA").collection("USER_DATA");

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
//                                                Intent mainIntent = new Intent(getContext(), MainActivity.class);
//                                                startActivity(mainIntent);
//                                                getActivity().finish();
                                                loadingDialog.dismiss();
                                            }

                                        } else {
                                            loadingDialog.dismiss();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            registerbtn.setEnabled(true);
                                            registerbtn.setTextColor(Color.rgb(255, 255, 255));
                                            String error = task.getException().getMessage();
                                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }


                        } else {

                            String error = task.getException().getMessage();
                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });



    }
}