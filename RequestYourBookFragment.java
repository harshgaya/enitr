package com.harsh.enitr;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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

import java.util.HashMap;
import java.util.Map;


public class RequestYourBookFragment extends Fragment {

    private Spinner spinner;
    private EditText nameOfBook;
    private EditText authorofBook,Address,Addresss;
    private Button submit;
    private String[] conditionofBook;
    private String selectedConditionofBook;


    public RequestYourBookFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_your_book, container, false);
        spinner = view.findViewById(R.id.spinner);
        nameOfBook = view.findViewById(R.id.nameofBook);
        authorofBook = view.findViewById(R.id.authorofBook);
        submit = view.findViewById(R.id.submit);
        Address = view.findViewById(R.id.Address);
        Addresss = view.findViewById(R.id.Addresss);

        conditionofBook = getResources().getStringArray(R.array.conditionofBook);
        ArrayAdapter spinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, conditionofBook);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedConditionofBook = conditionofBook[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameOfBook.addTextChangedListener(new TextWatcher() {
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
                sendSMS();
                sendEmail();
                Toast.makeText(getContext(), "Your request for the book "+nameOfBook.getText().toString()+" has been submitted successfully!", Toast.LENGTH_SHORT).show();
                nameOfBook.setText(null);
                authorofBook.setText(null);



            }
        });
    }

    private void checkInputs() {
        if (!TextUtils.isEmpty(nameOfBook.getText())) {
            submit.setEnabled(true);
            submit.setTextColor(Color.rgb( 255, 255, 255));

        } else {
            submit.setEnabled(false);
            submit.setTextColor(Color.argb(50, 255, 255, 255));
        }
    }
    private void sendEmail(){
        final String nameofBook = nameOfBook.getText().toString();
        final String authorOfBook = authorofBook.getText().toString();
        final String conditionOfBook = selectedConditionofBook;
        new Thread(new Runnable() {
            public void run() {
                try {
                    GMailSender sender = new GMailSender(
                            "thenitr@gmail.com",
                            "yfacaxjoaiutgvou");
                    //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                    sender.sendMail("Request of the Book", "Request of the Book -" + "\n"
                                    + "Name of The Book :- " + nameofBook + "\n"
                                    + "Author of Book :- " + authorOfBook + "\n"
                                    + "Condition of Book :- " + selectedConditionofBook + "\n"

                                    + "This is a system generated mail don't reply to these messages." + "\n",


                            "thenitr@gmail.com",
                            "harshgaya42@gmail.com");


                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
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
                                    + "Name of The Book :- " + nameofBook + "\n"
                                    + "Author of Book :- " + authorOfBook + "\n"
                                    + "Condition of Book :- " + selectedConditionofBook + "\n"

                                    + "This is a system generated mail don't reply to these messages." + "\n",


                            "yourenitr@gmail.com",
                            "harshgaya42@gmail.com");


                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
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
                                    + "Name of The Book :- " + nameofBook + "\n"
                                    + "Author of Book :- " + authorOfBook + "\n"
                                    + "Condition of Book :- " + selectedConditionofBook + "\n"

                                    + "This is a system generated mail don't reply to these messages." + "\n",


                            "yourenitr@gmail.com",
                            "harshkumarworld@gmail.com");


                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
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
                                    + "Name of The Book :- " + nameofBook + "\n"
                                    + "Author of Book :- " + authorOfBook + "\n"
                                    + "Condition of Book :- " + selectedConditionofBook + "\n"

                                    + "Your request for the book "+nameofBook+" has been submitted ."+" We will call you if the book is available. " + "\n"

                                    + "Thanks for shopping with eNITR !" + "\n"
                                    + "This is a system generated mail don't reply to these messages." + "\n",


                            "yourenitr@gmail.com",
                            DBqueries.email);


                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                }
            }

        }).start();
    }

    private void sendSMS(){
        //sent confirmation SMS
        final String nameofBook = nameOfBook.getText().toString();
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
                body.put("numbers","9304136129");
                body.put("message", "22612");
                body.put("variables", "{#FF#}");
                body.put("variables_values", nameofBook);



                return body;

            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
        //sent confirmation SMS


    }
}