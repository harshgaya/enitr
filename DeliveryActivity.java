package com.harsh.enitr;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.harsh.enitr.Adapter.CartAdapter;
import com.harsh.enitr.Model.CartItemModel;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;


public class DeliveryActivity extends AppCompatActivity implements PaymentResultListener {

    public static List<CartItemModel> cartItemModelList;
    private RecyclerView deliveryRecyclerView;
    public static CartAdapter cartAdapter;
    private Button changeORaddNewAddressBtn;
    public static final int SELECT_ADDRESS = 0;
    private TextView totalAmount;
    private TextView fullname,textView41;
    private String name, mobileNo;
    private TextView fullAddress;
    private TextView pincode;
    private Button continueBtn;
    public static Dialog loadingDialog;
    private Dialog paymentMethodDialog;
    private TextView codTitle;
    private View divider;
    private ImageButton paytm, cod;
    private String paymentMethod = "PAYTM";
    private ConstraintLayout orderConfirmationLayout;
    private TextView orderId;
    private ImageButton continueShoppingBtn;
    private Boolean successResponse = false;
    public static boolean fromCart;
    private String order_id;
    public static boolean codOrderConfirmed = false;
    private FirebaseFirestore firebaseFirestore;

    final int UPI_PAYMENT = 0;

    public static boolean getQtyIDs = true;
    private static final String TAG = MainActivity.class.getSimpleName();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        Checkout.preload(getApplicationContext());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Delivery");

        deliveryRecyclerView = findViewById(R.id.delivery_recyclerview);
        changeORaddNewAddressBtn = findViewById(R.id.change_or_add_address_btn);
        totalAmount = findViewById(R.id.total_cart_amount);
        fullname = findViewById(R.id.fullname);
        fullAddress = findViewById(R.id.address);
        pincode = findViewById(R.id.pincode);
        continueBtn = findViewById(R.id.cart_continue_btn);
        orderConfirmationLayout = findViewById(R.id.order_confirmation_layout);
        continueShoppingBtn = findViewById(R.id.continue_shopping_btn);
        orderId = findViewById(R.id.order_id);
        textView41 = findViewById(R.id.textView41);


        /////////// loading dialog
        loadingDialog = new Dialog(DeliveryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ///// loading dialog

        /////////// payment method dialog
        paymentMethodDialog = new Dialog(DeliveryActivity.this);
        paymentMethodDialog.setContentView(R.layout.payment_method);
        paymentMethodDialog.setCancelable(true);
        paymentMethodDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        paymentMethodDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paytm = (ImageButton) paymentMethodDialog.findViewById(R.id.paytm);
        cod = paymentMethodDialog.findViewById(R.id.cod_btn);

        codTitle = paymentMethodDialog.findViewById(R.id.cod_btn_title);
        divider = findViewById(R.id.payment_method_divider);
        ///// payment method dialog
        firebaseFirestore = FirebaseFirestore.getInstance();
        getQtyIDs = true;


        order_id = UUID.randomUUID().toString().substring(0, 28);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        deliveryRecyclerView.setLayoutManager(layoutManager);

        cartAdapter = new CartAdapter(cartItemModelList, totalAmount, false);
        deliveryRecyclerView.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();


        changeORaddNewAddressBtn.setVisibility(View.VISIBLE);
        changeORaddNewAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getQtyIDs = false;
                Intent myAddressesIntent = new Intent(DeliveryActivity.this, MyAddressesActivity.class);
                myAddressesIntent.putExtra("MODE", SELECT_ADDRESS);
                startActivity(myAddressesIntent);
            }
        });



        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean allProductsAvailable = true;
                for (CartItemModel cartItemModel : cartItemModelList) {
                    if (cartItemModel.isQtyError()) {
                        allProductsAvailable = false;
                        break;
                    }
                    if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                        if (!cartItemModel.isCOD()) {
                            cod.setEnabled(false);
                            cod.setAlpha(0.5f);
                            codTitle.setAlpha(0.5f);
                            //divider.setVisibility(View.GONE);
                            break;
                        } else {
                            cod.setEnabled(true);
                            cod.setAlpha(1f);
                            codTitle.setAlpha(1f);
                            codTitle.setVisibility(View.VISIBLE);
                        }
                    }
                }
                if (allProductsAvailable) {
                    paymentMethodDialog.show();
                }
            }
        });

        cod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentMethod = "COD";
                placeOrderDetails();
            }
        });


        paytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentMethod = "PAYTM";
                placeOrderDetails();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        ///// accesing quantity

        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    final String Selectedcity=task.getResult().getString("city");

                    if (getQtyIDs) {
                        loadingDialog.show();
                        for (int x = 0; x < cartItemModelList.size() - 1; x++) {

                            for (int y = 0; y < cartItemModelList.get(x).getProductQuantity(); y++) {
                                final String quantityDocumentName = UUID.randomUUID().toString().substring(0, 20);

                                final Map<String, Object> timeStamp = new HashMap<>();
                                timeStamp.put("time", FieldValue.serverTimestamp());
                                final int finalX = x;
                                final int finalY = y;

                                //firebaseFirestore.collection(Selectedcity).document("PRODUCTS").collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(quantityDocumentName).set(timeStamp)

                                        firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(quantityDocumentName).set(timeStamp)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete( Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    cartItemModelList.get(finalX).getQtyIDs().add(quantityDocumentName);

                                                    if (finalY + 1 == cartItemModelList.get(finalX).getProductQuantity()) {

                                                        //firebaseFirestore.collection(Selectedcity).document("PRODUCTS").collection("PRODUCTS").document(cartItemModelList.get(finalX).getProductID()).collection("QUANTITY").orderBy("time", Query.Direction.DESCENDING).limit(cartItemModelList.get(finalX).getStockQuantity()).get()

                                                                firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(finalX).getProductID()).collection("QUANTITY").orderBy("time", Query.Direction.DESCENDING).limit(cartItemModelList.get(finalX).getStockQuantity()).get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            List<String> serverQuantity = new ArrayList<>();
                                                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                                                serverQuantity.add(queryDocumentSnapshot.getId());
                                                                            }
                                                                            long availableQty = 0;
                                                                            boolean noLongerAvailable = true;
                                                                            for (String qtyId : cartItemModelList.get(finalX).getQtyIDs()) {
                                                                                cartItemModelList.get(finalX).setQtyError(false);
                                                                                if (!serverQuantity.contains(qtyId)) {
                                                                                    if (noLongerAvailable) {
                                                                                        cartItemModelList.get(finalX).setInStock(false);
                                                                                    } else {
                                                                                        cartItemModelList.get(finalX).setQtyError(true);
                                                                                        cartItemModelList.get(finalX).setMaxQuantity(availableQty);
                                                                                        Toast.makeText(DeliveryActivity.this, "Sorry ! all products may not be available in required quanity....", Toast.LENGTH_SHORT).show();
                                                                                    }

                                                                                } else {
                                                                                    availableQty++;
                                                                                    noLongerAvailable = false;
                                                                                }

                                                                            }
                                                                            cartAdapter.notifyDataSetChanged();


                                                                        } else {
                                                                            String error = task.getException().getMessage();
                                                                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        loadingDialog.dismiss();
                                                                    }
                                                                });

                                                    }
                                                } else {
                                                    loadingDialog.dismiss();
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                            }
                        }
                    } else {
                        getQtyIDs = true;
                    }
                    ///// accesing quantity

                    name = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getName();
                    mobileNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getMobileNo();

                    if (DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo().equals("")) {
                        fullname.setText(name + " - " + mobileNo);
                    } else {
                        fullname.setText(name + " - " + mobileNo + " or " + DBqueries.addressesModelList.get(DBqueries.selectedAddress).getAlternateMobileNo());
                    }
                    String flatNo = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getFlatNo();
                    String locality = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getLocality();
                    String landmark = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getLandmark();
                    String city = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getCity();
                    String state = DBqueries.addressesModelList.get(DBqueries.selectedAddress).getState();

                    if (landmark.equals("")) {
                        fullAddress.setText(flatNo + " " + locality + "  " + city + " " + state);
                    } else {
                        fullAddress.setText(flatNo + " " + locality + " " + landmark + " " + city + " " + state);
                    }
                    pincode.setText(DBqueries.addressesModelList.get(DBqueries.selectedAddress).getPincode());

                    if (codOrderConfirmed) {
                        showConfirmationLayout();
                    }

                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    final String Selectedcity=task.getResult().getString("city");

                    loadingDialog.dismiss();
                    if (getQtyIDs) {

                        for (int x = 0; x < cartItemModelList.size() - 1; x++) {
                            if (!successResponse) {
                                for (final String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                                    final int finalX = x;

                                    //firebaseFirestore.collection(Selectedcity).document("PRODUCTS").collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).delete()

                                            firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (qtyID.equals(cartItemModelList.get(finalX).getQtyIDs().get(cartItemModelList.get(finalX).getQtyIDs().size() - 1))) {
                                                        cartItemModelList.get(finalX).getQtyIDs().clear();
                                                    }
                                                }
                                            });

                                }
                            } else {
                                cartItemModelList.get(x).getQtyIDs().clear();
                            }
                        }
                    }

                }
            }
        });



    }

    @Override
    public void onBackPressed() {
        if (successResponse) {
            finish();
            return;
        }
        super.onBackPressed();
    }

    private void showConfirmationLayout() {

        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    final String Selectedcity=task.getResult().getString("city");

                    successResponse = true;
                    codOrderConfirmed = false;
                    getQtyIDs = false;

                    for (int x = 0; x < cartItemModelList.size() - 1; x++) {
                        for (String qtyID : cartItemModelList.get(x).getQtyIDs()) {

                            //firebaseFirestore.collection(Selectedcity).document("PRODUCTS").collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).update("user_ID", FirebaseAuth.getInstance().getUid());

                            firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).update("user_ID", FirebaseAuth.getInstance().getUid());
                        }

                    }
                    if (MainActivity.mainActivity != null) {
                        MainActivity.mainActivity.finish();
                        MainActivity.mainActivity = null;
                        MainActivity.showCart = false;
                    } else {
                        MainActivity.resetMainActivity = true;
                    }
                    if (ProductDetailsActivity.productDetailsActivity != null) {
                        ProductDetailsActivity.productDetailsActivity.finish();
                        ProductDetailsActivity.productDetailsActivity = null;
                    }


                    //sent confirmation SMS
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
                            body.put("numbers", mobileNo);
                            body.put("message", "19946");
                            body.put("variables", "{#FF#}");
                            body.put("variables_values", order_id);



                            return body;

                        }
                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
                    requestQueue.add(stringRequest);
                    //sent confirmation SMS


                    // sent confirmation mail


                    final String userID = FirebaseAuth.getInstance().getUid();


                    for (final CartItemModel cartItemModel : cartItemModelList) {
                        if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                            final String producttitle = cartItemModel.getProductTitle();
                            final String productQuantity = String.valueOf(cartItemModel.getProductQuantity());
                            final String productPrice = cartItemModel.getProductPrice();
                            final String cuttedPrice = cartItemModel.getCuttedPrice();
                            final String DiscountedPrice = cartItemModel.getDiscountedPrice();
                            final String Address = fullAddress.getText().toString();
                            final String fullName = fullname.getText().toString();
                            final String MobileNumber = mobileNo;
                            final String pinCode = pincode.getText().toString();
                            final String DeliveryPrice = cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice();
                            final String orderId = order_id;
                            final String productId = cartItemModel.getProductID();
                            final String userId = userID;
                            final String PaymentMethod = paymentMethod;




                            //sent confirmation SMS
                            String SMS_APII = "https://www.fast2sms.com/dev/bulk";
                            StringRequest stringRequestt = new StringRequest(Request.Method.POST, SMS_APII, new Response.Listener<String>() {
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
                                    body.put("message", "22611");
                                    body.put("variables", "{#FF#}");
                                    body.put("variables_values", producttitle);



                                    return body;

                                }
                            };
                            stringRequestt.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            RequestQueue requestQueuee = Volley.newRequestQueue(DeliveryActivity.this);
                            requestQueuee.add(stringRequestt);
                            //sent confirmation SMS

                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "theenitr@gmail.com",
                                                "yfacaxjoaiutgvou");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Order Details: ", "Order Id -" + orderId + "\n"
                                                        + "Product Id -" + productId + "\n"
                                                        + "Product Title -" + producttitle + "\r\n"
                                                        + "User Id -" + userId + "\n"
                                                        + "Product Quantity -" + productQuantity + "\n"
                                                        + "Cutted Price -" + cuttedPrice + "\n"
                                                        + "Product Price -" + productPrice + "\n"
                                                        + "Discounted Price- " + DiscountedPrice + "\n"
                                                        + "Full Address- " + Address + "\n"
                                                        + "User Name -" + fullName + "\n"
                                                        + "Mobile No -" + MobileNumber + "\n"
                                                        + "Pincode -" + pinCode + "\n"
                                                        + "Delivery Price - " + DeliveryPrice + "\n"
                                                        + "Payment Method - " + PaymentMethod+"\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "theenitr@gmail.com",
                                                "harshgaya42@gmail.com");


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();


                        } else {
                            final String totalItems = String.valueOf(cartItemModel.getTotalItems());
                            final String TotalItemPrice = String.valueOf(cartItemModel.getTotalItemPrice());
                            final String DeliveryPrice = String.valueOf(cartItemModel.getDeliveryPrice());
                            final String TotalAmount = String.valueOf(cartItemModel.getTotalAmount());
                            final String SavedAmount = String.valueOf(cartItemModel.getSavedAmount());


                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "theenitr@gmail.com",
                                                "yfacaxjoaiutgvou");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Order Details: ",
                                                "Total Items-" + totalItems + "\r\n"
                                                        + "Total Item Price -" + TotalItemPrice + "\r\n"
                                                        + "Delivery Price -" + DeliveryPrice + "\r\n"
                                                        + "Total Amount -" + TotalAmount + "\r\n"
                                                        + "Saved Amount -" + SavedAmount + "\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "theenitr@gmail.com",
                                                "harshgaya42@gmail.com");


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();


                        }
                    }


                    for (final CartItemModel cartItemModel : cartItemModelList) {
                        if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                            final String producttitle = cartItemModel.getProductTitle();
                            final String productQuantity = String.valueOf(cartItemModel.getProductQuantity());
                            final String productPrice = cartItemModel.getProductPrice();
                            final String cuttedPrice = cartItemModel.getCuttedPrice();
                            final String DiscountedPrice = cartItemModel.getDiscountedPrice();
                            final String Address = fullAddress.getText().toString();
                            final String fullName = fullname.getText().toString();
                            final String MobileNumber = mobileNo;
                            final String pinCode = pincode.getText().toString();
                            final String DeliveryPrice = cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice();
                            final String orderId = order_id;
                            final String productId = cartItemModel.getProductID();
                            final String userId = userID;
                            final String PaymentMethod = paymentMethod;

                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "yourenitr@gmail.com",
                                                "acglwvstettmursu");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Order Details: ", "Order Id -" + orderId + "\n"
                                                        + "Product Id -" + productId + "\n"
                                                        + "Product Title -" + producttitle + "\r\n"
                                                        + "User Id -" + userId + "\n"
                                                        + "Product Quantity -" + productQuantity + "\n"
                                                        + "Cutted Price -" + cuttedPrice + "\n"
                                                        + "Product Price -" + productPrice + "\n"
                                                        + "Discounted Price- " + DiscountedPrice + "\n"
                                                        + "Full Address- " + Address + "\n"
                                                        + "User Name -" + fullName + "\n"
                                                        + "Mobile No -" + MobileNumber + "\n"
                                                        + "Pincode -" + pinCode + "\n"
                                                        + "Delivery Price - " + DeliveryPrice + "\n"
                                                        + "Payment Method - " + PaymentMethod+"\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "yourenitr@gmail.com",
                                                "harshgaya42@gmail.com");


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();


                        } else {
                            final String totalItems = String.valueOf(cartItemModel.getTotalItems());
                            final String TotalItemPrice = String.valueOf(cartItemModel.getTotalItemPrice());
                            final String DeliveryPrice = String.valueOf(cartItemModel.getDeliveryPrice());
                            final String TotalAmount = String.valueOf(cartItemModel.getTotalAmount());
                            final String SavedAmount = String.valueOf(cartItemModel.getSavedAmount());


                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "yourenitr@gmail.com",
                                                "acglwvstettmursu");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Order Details: ",
                                                "Total Items-" + totalItems + "\r\n"
                                                        + "Total Item Price -" + TotalItemPrice + "\r\n"
                                                        + "Delivery Price -" + DeliveryPrice + "\r\n"
                                                        + "Total Amount -" + TotalAmount + "\r\n"
                                                        + "Saved Amount -" + SavedAmount + "\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "yourenitr@gmail.com",
                                                "harshgaya42@gmail.com");


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();


                        }
                    }

                    ///////sent confirmation mail

                    for (final CartItemModel cartItemModel : cartItemModelList) {
                        if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                            final String producttitle = cartItemModel.getProductTitle();
                            final String productQuantity = String.valueOf(cartItemModel.getProductQuantity());
                            final String productPrice = cartItemModel.getProductPrice();
                            final String cuttedPrice = cartItemModel.getCuttedPrice();
                            final String DiscountedPrice = cartItemModel.getDiscountedPrice();
                            final String Address = fullAddress.getText().toString();
                            final String fullName = fullname.getText().toString();
                            final String MobileNumber = mobileNo;
                            final String pinCode = pincode.getText().toString();
                            final String DeliveryPrice = cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice();
                            final String orderId = order_id;
                            final String productId = cartItemModel.getProductID();
                            final String userId = userID;
                            final String PaymentMethod = paymentMethod;


                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "thecarthive@gmail.com",/// ejiwzmbuolypvozm
                                                ///// for theenitr@gmail.com password  yfacaxjoaiutgvou
                                                "rvittdvpwjaqwgwn");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Carthive", "Your order has been placed."+"\n"
                                                        +"Your order id is: " + orderId + "\n"
                                                        + "Your product is  -" + producttitle + "\r\n"
                                                        + "Thanks for shopping with Carthive !" + "\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "thecarthive@gmail.com",
                                                DBqueries.email);


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();


                        } else {
                            final String totalItems = String.valueOf(cartItemModel.getTotalItems());
                            final String TotalItemPrice = String.valueOf(cartItemModel.getTotalItemPrice());
                            final String DeliveryPrice = String.valueOf(cartItemModel.getDeliveryPrice());
                            final String TotalAmount = String.valueOf(cartItemModel.getTotalAmount());
                            final String SavedAmount = String.valueOf(cartItemModel.getSavedAmount());


                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "thecarthive@gmail.com",
                                                "rvittdvpwjaqwgwn");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Carthive",

                                                "Your Product Details :" +"\n"
                                                        +"Total Items-" + totalItems + "\r\n"
                                                        + "Total Item Price -" + TotalItemPrice + "\r\n"
                                                        + "Delivery Price -" + DeliveryPrice + "\r\n"
                                                        + "Total Amount -" + TotalAmount + "\r\n"
                                                        + "Saved Amount -" + SavedAmount + "\n"
                                                        + "Thanks for shopping with Carthive !" + "\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "thecarthive@gmail.com",
                                                DBqueries.email);


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();

                        }
                    }

                    ///sent confirmation mail
//                    for (final CartItemModel cartItemModel : cartItemModelList) {
//                        if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
//                            final String producttitle = cartItemModel.getProductTitle();
//                            final String productQuantity = String.valueOf(cartItemModel.getProductQuantity());
//                            final String productPrice = cartItemModel.getProductPrice();
//                            final String cuttedPrice = cartItemModel.getCuttedPrice();
//                            final String DiscountedPrice = cartItemModel.getDiscountedPrice();
//                            final String Address = fullAddress.getText().toString();
//                            final String fullName = fullname.getText().toString();
//                            final String MobileNumber = mobileNo;
//                            final String pinCode = pincode.getText().toString();
//                            final String DeliveryPrice = cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice();
//                            final String orderId = order_id;
//                            final String productId = cartItemModel.getProductID();
//                            final String userId = userID;
//                            final String PaymentMethod = paymentMethod;
//
//                            new Thread(new Runnable() {
//                                public void run() {
//                                    try {
//                                        GMailSender sender = new GMailSender(
//                                                "yourenitr@gmail.com",
//                                                "acglwvstettmursu");
//                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
//                                        sender.sendMail("Order Details: ", "Order Id -" + orderId + "\n"
//                                                        + "Product Id -" + productId + "\n"
//                                                        + "Product Title -" + producttitle + "\r\n"
//                                                        + "User Id -" + userId + "\n"
//                                                        + "Product Quantity -" + productQuantity + "\n"
//                                                        + "Cutted Price -" + cuttedPrice + "\n"
//                                                        + "Product Price -" + productPrice + "\n"
//                                                        + "Discounted Price- " + DiscountedPrice + "\n"
//                                                        + "Full Address- " + Address + "\n"
//                                                        + "User Name -" + fullName + "\n"
//                                                        + "Mobile No -" + MobileNumber + "\n"
//                                                        + "Pincode -" + pinCode + "\n"
//                                                        + "Delivery Price - " + DeliveryPrice + "\n"
//                                                        + "Payment Method - " + PaymentMethod+"\n"
//                                                        + "This is a system generated mail don't reply to these messages."+ "\n",
//
//
//                                                "yourenitr@gmail.com",
//                                                "harshkumarworld@gmail.com");
//
//
//                                    } catch (Exception e) {
//                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
//                                    }
//                                }
//
//                            }).start();
//
//
//                        } else {
//                            final String totalItems = String.valueOf(cartItemModel.getTotalItems());
//                            final String TotalItemPrice = String.valueOf(cartItemModel.getTotalItemPrice());
//                            final String DeliveryPrice = String.valueOf(cartItemModel.getDeliveryPrice());
//                            final String TotalAmount = String.valueOf(cartItemModel.getTotalAmount());
//                            final String SavedAmount = String.valueOf(cartItemModel.getSavedAmount());
//
//
//                            new Thread(new Runnable() {
//                                public void run() {
//                                    try {
//                                        GMailSender sender = new GMailSender(
//                                                "yourenitr@gmail.com",
//                                                "acglwvstettmursu");
//                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
//                                        sender.sendMail("Order Details: ",
//                                                "Total Items-" + totalItems + "\r\n"
//                                                        + "Total Item Price -" + TotalItemPrice + "\r\n"
//                                                        + "Delivery Price -" + DeliveryPrice + "\r\n"
//                                                        + "Total Amount -" + TotalAmount + "\r\n"
//                                                        + "Saved Amount -" + SavedAmount + "\n"
//                                                        + "This is a system generated mail don't reply to these messages."+ "\n",
//
//
//                                                "yourenitr@gmail.com",
//                                                "harshkumarworld@gmail.com");
//
//
//                                    } catch (Exception e) {
//                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
//                                    }
//                                }
//
//                            }).start();
//
//
//                        }
//                    }
//                    ///sent confirmation mail to sweta kashyap



                    /////sent confirmation mail to user

                    ////sent to rakesh
                    for (final CartItemModel cartItemModel : cartItemModelList) {
                        if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                            final String producttitle = cartItemModel.getProductTitle();
                            final String productQuantity = String.valueOf(cartItemModel.getProductQuantity());
                            final String productPrice = cartItemModel.getProductPrice();
                            final String cuttedPrice = cartItemModel.getCuttedPrice();
                            final String DiscountedPrice = cartItemModel.getDiscountedPrice();
                            final String Address = fullAddress.getText().toString();
                            final String fullName = fullname.getText().toString();
                            final String MobileNumber = mobileNo;
                            final String pinCode = pincode.getText().toString();
                            final String DeliveryPrice = cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice();
                            final String orderId = order_id;
                            final String productId = cartItemModel.getProductID();
                            final String userId = userID;
                            final String PaymentMethod = paymentMethod;


                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "yourenitr@gmail.com",/// ejiwzmbuolypvozm
                                                ///// for theenitr@gmail.com password  yfacaxjoaiutgvou
                                                "acglwvstettmursu");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Carthive", "Your order has been placed."+"\n"
                                                        +"Your order id is: " + orderId + "\n"
                                                        + "Your product is  -" + producttitle + "\r\n"
                                                        + "Thanks for shopping with Carthive !" + "\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "yourenitr@gmail.com",
                                                "rakeshraushan258@gmail.com");


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();


                        } else {
                            final String totalItems = String.valueOf(cartItemModel.getTotalItems());
                            final String TotalItemPrice = String.valueOf(cartItemModel.getTotalItemPrice());
                            final String DeliveryPrice = String.valueOf(cartItemModel.getDeliveryPrice());
                            final String TotalAmount = String.valueOf(cartItemModel.getTotalAmount());
                            final String SavedAmount = String.valueOf(cartItemModel.getSavedAmount());


                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "yourenitr@gmail.com",
                                                "acglwvstettmursu");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Carthive",

                                                "Your Product Details :" +"\n"
                                                        +"Total Items-" + totalItems + "\r\n"
                                                        + "Total Item Price -" + TotalItemPrice + "\r\n"
                                                        + "Delivery Price -" + DeliveryPrice + "\r\n"
                                                        + "Total Amount -" + TotalAmount + "\r\n"
                                                        + "Saved Amount -" + SavedAmount + "\n"
                                                        + "Thanks for shopping with Carthive !" + "\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "yourenitr@gmail.com",
                                                "rakeshraushan258@gmail.com");


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();

                        }
                    }
                    ////sent to rakesh


                    ///sent to dinesh
                    for (final CartItemModel cartItemModel : cartItemModelList) {
                        if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                            final String producttitle = cartItemModel.getProductTitle();
                            final String productQuantity = String.valueOf(cartItemModel.getProductQuantity());
                            final String productPrice = cartItemModel.getProductPrice();
                            final String cuttedPrice = cartItemModel.getCuttedPrice();
                            final String DiscountedPrice = cartItemModel.getDiscountedPrice();
                            final String Address = fullAddress.getText().toString();
                            final String fullName = fullname.getText().toString();
                            final String MobileNumber = mobileNo;
                            final String pinCode = pincode.getText().toString();
                            final String DeliveryPrice = cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice();
                            final String orderId = order_id;
                            final String productId = cartItemModel.getProductID();
                            final String userId = userID;
                            final String PaymentMethod = paymentMethod;


                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "yourenitr@gmail.com",/// ejiwzmbuolypvozm
                                                ///// for theenitr@gmail.com password  yfacaxjoaiutgvou
                                                "acglwvstettmursu");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Carthive", "Your order has been placed."+"\n"
                                                        +"Your order id is: " + orderId + "\n"
                                                        + "Your product is  -" + producttitle + "\r\n"
                                                        + "Thanks for shopping with Carthive !" + "\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "yourenitr@gmail.com",
                                                "kumardinesh0316@gmail.com");


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();


                        } else {
                            final String totalItems = String.valueOf(cartItemModel.getTotalItems());
                            final String TotalItemPrice = String.valueOf(cartItemModel.getTotalItemPrice());
                            final String DeliveryPrice = String.valueOf(cartItemModel.getDeliveryPrice());
                            final String TotalAmount = String.valueOf(cartItemModel.getTotalAmount());
                            final String SavedAmount = String.valueOf(cartItemModel.getSavedAmount());


                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        GMailSender sender = new GMailSender(
                                                "yourenitr@gmail.com",
                                                "acglwvstettmursu");
                                        //sender.addAttachment(Environment.getExternalStorageDirectory().getPath()+"/image.jpg");
                                        sender.sendMail("Carthive",

                                                "Your Product Details :" +"\n"
                                                        +"Total Items-" + totalItems + "\r\n"
                                                        + "Total Item Price -" + TotalItemPrice + "\r\n"
                                                        + "Delivery Price -" + DeliveryPrice + "\r\n"
                                                        + "Total Amount -" + TotalAmount + "\r\n"
                                                        + "Saved Amount -" + SavedAmount + "\n"
                                                        + "Thanks for shopping with Carthive !" + "\n"
                                                        + "This is a system generated mail don't reply to these messages."+ "\n",


                                                "yourenitr@gmail.com",
                                                "kumardinesh0316@gmail.com");


                                    } catch (Exception e) {
                                        Toast.makeText(DeliveryActivity.this, "Error", Toast.LENGTH_LONG).show();
                                    }
                                }

                            }).start();

                        }
                    }
                    ///sent to dinesh








                    ///////////// sent confirmation mail


//        if (fromCart) {
//            loadingDialog.show();
//            Map<String, Object> updateCartList = new HashMap<>();
//            long cartListSize = 0;
//            final List<Integer> indexList = new ArrayList<>();
//            for (int x = 0; x < DBqueries.cartList.size(); x++) {
//                if (!cartItemModelList.get(x).isInStock()) {
//                    updateCartList.put("product_ID_" + cartListSize, cartItemModelList.get(x).getProductID());
//                    cartListSize++;
//                } else {
//                    indexList.add(x);
//                }
//            }
//            updateCartList.put("list_size", cartListSize);
//            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
//                    .set(updateCartList).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if (task.isSuccessful()) {
//                        for (int x = 0; x < indexList.size(); x++) {
//                            DBqueries.cartList.remove(indexList.get(x).intValue());
//                            DBqueries.cartItemModelList.remove(indexList.get(x).intValue());
//                            DBqueries.cartItemModelList.remove(DBqueries.cartItemModelList.size() - 1);
//                        }
//                    } else {
//                        String error = task.getException().getMessage();
//                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
//                    }
//                    loadingDialog.dismiss();
//                }
//            });
//        }

                    continueBtn.setEnabled(false);
                    changeORaddNewAddressBtn.setEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    orderId.setText("Order ID " + order_id);
                    orderConfirmationLayout.setVisibility(View.VISIBLE);
                    continueShoppingBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
                    textView41.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });

                }
            }
        });


    }

    private void placeOrderDetails() {

        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    final String Selectedcity=task.getResult().getString("city");

                    final String userID = FirebaseAuth.getInstance().getUid();
                    loadingDialog.show();
                    for (final CartItemModel cartItemModel : cartItemModelList) {
                        final String producttitle = cartItemModel.getProductTitle();

                        if (cartItemModel.getType() == CartItemModel.CART_ITEM) {

                            Map<String, Object> orderDetails = new HashMap<>();
                            orderDetails.put("ORDER ID", order_id);
                            orderDetails.put("Product Id", cartItemModel.getProductID());
                            orderDetails.put("Product Image", cartItemModel.getProductImage());
                            orderDetails.put("Product Title", producttitle);
                            orderDetails.put("User Id", userID);
                            orderDetails.put("Product Quantity", cartItemModel.getProductQuantity());
                            if (cartItemModel.getCuttedPrice() != null) {
                                orderDetails.put("Cutted Price", cartItemModel.getCuttedPrice());
                            } else {
                                orderDetails.put("Cutted Price", "");

                            }
                            orderDetails.put("Product Price", cartItemModel.getProductPrice());
                            if (cartItemModel.getSelectedCoupenId() != null) {
                                orderDetails.put("Coupen Id", cartItemModel.getSelectedCoupenId());
                            } else {
                                orderDetails.put("Coupen Id", "");

                            }
                            if (cartItemModel.getDiscountedPrice() != null) {
                                orderDetails.put("Discounted Price", cartItemModel.getDiscountedPrice());
                            } else {
                                orderDetails.put("Discounted Price", "");

                            }
                            orderDetails.put("Ordered date", FieldValue.serverTimestamp());
                            orderDetails.put("Packed date", FieldValue.serverTimestamp());
                            orderDetails.put("Shipped date", FieldValue.serverTimestamp());
                            orderDetails.put("Delivered date", FieldValue.serverTimestamp());
                            orderDetails.put("Cancelled date", FieldValue.serverTimestamp());
                            orderDetails.put("Order Status", "Ordered");
                            orderDetails.put("Payment Method", paymentMethod);
                            orderDetails.put("Address", fullAddress.getText());
                            orderDetails.put("FullName", fullname.getText());
                            orderDetails.put("Pincode", pincode.getText());
                            orderDetails.put("Free Coupens", cartItemModel.getFreeCoupens());
                            orderDetails.put("Delivery Price", cartItemModelList.get(cartItemModelList.size() - 1).getDeliveryPrice());
                            orderDetails.put("Cancellation requested", false);

                            //firebaseFirestore.collection(Selectedcity).document("ORDERS").collection("ORDERS").document(order_id).collection("OrderItems").document(cartItemModel.getProductID())

                                    firebaseFirestore.collection("ORDERS").document(order_id).collection("OrderItems").document(cartItemModel.getProductID())
                                    .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Map<String, Object> orderDetails = new HashMap<>();
                            orderDetails.put("Total Items", cartItemModel.getTotalItems());
                            orderDetails.put("Total Items Price", cartItemModel.getTotalItemPrice());
                            orderDetails.put("Delivery Price", cartItemModel.getDeliveryPrice());
                            orderDetails.put("Total Amount", cartItemModel.getTotalAmount());
                            orderDetails.put("Saved Amount", cartItemModel.getSavedAmount());
                            orderDetails.put("Payment Status", "not paid");
                            orderDetails.put("Order Status", "Cancelled");

                            //firebaseFirestore.collection(Selectedcity).document("ORDERS").collection("ORDERS").document(order_id)

                                    firebaseFirestore.collection("ORDERS").document(order_id)
                                    .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (paymentMethod.equals("PAYTM")) {
                                            razorPay();
                                        } else{
                                            cod();
                                        }
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }


                    }

                }
            }
        });



    }

    private void paytm() {
        getQtyIDs = false;
        paymentMethodDialog.dismiss();
        loadingDialog.show();
        if (ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeliveryActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

        final String M_id = "scXhZV10272393881170";
        final String customer_id = FirebaseAuth.getInstance().getUid();
        String url = "https://kkenitr.000webhostapp.com/paytm/generateChecksum.php";
        final String callBackUrl = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=<order_id>";




        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("CHECKSUMHASH")) {
                        String CHECKSUMHASH = jsonObject.getString("CHECKSUMHASH");

                        PaytmPGService paytmPGService = PaytmPGService.getProductionService();
                        HashMap<String, String> paramMap = new HashMap<String, String>();
                        paramMap.put("MID", M_id);
                        paramMap.put("ORDER_ID", order_id);
                        paramMap.put("CUST_ID", customer_id);
                        paramMap.put("CHANNEL_ID", "WAP");
                        paramMap.put("TXN_AMOUNT", totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2));
                        paramMap.put("WEBSITE", "DEFAULT");
                        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                        paramMap.put("CALLBACK_URL", callBackUrl);
                        paramMap.put("CHECKSUMHASH", CHECKSUMHASH);

                        PaytmOrder order = new PaytmOrder(paramMap);
                        paytmPGService.initialize(order, null);

                        paytmPGService.startPaymentTransaction(DeliveryActivity.this, true, true, new PaytmPaymentTransactionCallback() {
                            @Override
                            public void onTransactionResponse(Bundle inResponse) {
                                Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();

                                if (inResponse.getString("STATUS").equals("TXN_SUCCESS")) {
                                    Map<String, Object> updateStatus = new HashMap<>();
                                    updateStatus.put("Payment Status", "Paid");
                                    updateStatus.put("Order Status", "Ordered");
                                    firebaseFirestore.collection("ORDERS").document(order_id).update(updateStatus)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Map<String, Object> userOrder = new HashMap<>();
                                                        userOrder.put("order_id", order_id);
                                                        userOrder.put("time", FieldValue.serverTimestamp());
                                                        firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_ORDERS").document(order_id).set(userOrder)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            showConfirmationLayout();
                                                                        } else {
                                                                            Toast.makeText(DeliveryActivity.this, "Failed to update user's OrderList  ", Toast.LENGTH_LONG).show();

                                                                        }
                                                                    }
                                                                });

                                                    } else {
                                                        Toast.makeText(DeliveryActivity.this, "Order Cancelled", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                }
                            }





                            @Override
                            public void networkNotAvailable() {
                                Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void clientAuthenticationFailed(String inErrorMessage) {
                                Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void someUIErrorOccurred(String inErrorMessage) {
                                Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                                Toast.makeText(getApplicationContext(), "Unable to load webpage " + inFailingUrl.toString(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onBackPressedCancelTransaction() {
                                Toast.makeText(getApplicationContext(), "Transaction cancelled", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                                Toast.makeText(getApplicationContext(), "Transaction cancelled" + inResponse.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.dismiss();
                Toast.makeText(DeliveryActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> paramMap = new HashMap<String, String>();
                paramMap.put("MID", M_id);
                paramMap.put("ORDER_ID", order_id);
                paramMap.put("CUST_ID", customer_id);
                paramMap.put("CHANNEL_ID", "WAP");
                paramMap.put("TXN_AMOUNT", totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2));
                paramMap.put("WEBSITE", "DEFAULT");
                paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                paramMap.put("CALLBACK_URL", callBackUrl);
                return paramMap;
            }
        };
        requestQueue.add(stringRequest);

    }

    private void cod() {
        getQtyIDs = false;
        paymentMethodDialog.dismiss();
        Intent otpIntent = new Intent(DeliveryActivity.this, OTPverificationActivity.class);
        otpIntent.putExtra("mobileNo", mobileNo.substring(0, 10));
        otpIntent.putExtra("OrderID", order_id);
        startActivity(otpIntent);
    }

    private void razorPay(){
        paymentMethodDialog.dismiss();

        final Checkout checkout = new Checkout();
        final Activity activity = this;
        //checkout.setImage(R.drawable.abc);
        int i=Integer.parseInt(totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2));
        int totalAmount=i*100;



        try {
            JSONObject options = new JSONObject();
            options.put("name", "Carthive");
            options.put("description", "Order Id: "+order_id);
            options.put("currency", "INR");
            options.put("amount",totalAmount);

            String email=DBqueries.email;
            JSONObject preFill=new JSONObject();
            preFill.put("email",email);
            preFill.put("contact",mobileNo);
            options.put("prefill",preFill);

            checkout.open(activity, options);
        } catch(Exception e) {
            Toast.makeText(activity, "Error in payment", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }



    }

    @Override
    public void onPaymentSuccess(String s) {

        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    final String Selectedcity=task.getResult().getString("city");

                    Map<String, Object> updateStatus = new HashMap<>();
                    updateStatus.put("Payment Status", "Paid");
                    updateStatus.put("Order Status", "Ordered");

                    //firebaseFirestore.collection(Selectedcity).document("ORDERS").collection("ORDERS").document(order_id).update(updateStatus)



                            firebaseFirestore.collection("ORDERS").document(order_id).update(updateStatus)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> userOrder = new HashMap<>();
                                        userOrder.put("order_id", order_id);
                                        userOrder.put("time", FieldValue.serverTimestamp());

                                        //firebaseFirestore.collection(Selectedcity).document("USERS").collection(FirebaseAuth.getInstance().getUid()).document("USER_ORDERS").collection("USER_ORDERS").document(order_id).set(userOrder)

                                                firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_ORDERS").document(order_id).set(userOrder)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            showConfirmationLayout();
                                                        } else {
                                                            Toast.makeText(DeliveryActivity.this, "Failed to update user's OrderList  ", Toast.LENGTH_LONG).show();

                                                        }
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(DeliveryActivity.this, "Order Cancelled", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                    try {
                        Toast.makeText(DeliveryActivity.this, "Payment Successful", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Log.e(TAG,"Exception in onPaymentSuccess");
                    }

                }
            }
        });


    }

    @Override
    public void onPaymentError(int i, String s) {
        try {
            Toast.makeText(this, "Payment Failed :" +i+" "+s, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e(TAG,"Exception in onPaymentError",e);
        }
    }



}
