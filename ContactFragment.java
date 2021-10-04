package com.harsh.enitr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.net.URLEncoder;


public class ContactFragment extends Fragment {

    private TextView Query;
    Button FbBtn,InstaBtn,twitterBtn,
    harshCallBtn,rameshCallBtn,chandanCallBtn,LocationBtn,MailBtn,whatsappBtn;




    public ContactFragment() {
        // Required empty public constructor
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_contact, container, false);

        FbBtn=view.findViewById(R.id.FbBtn);
        InstaBtn=view.findViewById(R.id.InstaBtn);
        twitterBtn=view.findViewById(R.id.twitterBtn);
        harshCallBtn=view.findViewById(R.id.harshCallBtn);
        rameshCallBtn=view.findViewById(R.id.RameshCallBtn);
        LocationBtn=view.findViewById(R.id.LocationBtn);
        MailBtn=view.findViewById(R.id.MailBtn);
        whatsappBtn=view.findViewById(R.id.whatsappBtn);

        FbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/carthive.carthive.9"));
                startActivity(browserIntent);
            }
        });
        InstaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/thecarthive/"));
                startActivity(browserIntent);
            }
        });
        twitterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/carthive"));
                startActivity(browserIntent);
            }
        });
        harshCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:+91 9304136129"));
                startActivity(callIntent);
            }
        });
        rameshCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:+91 7050117050"));
                startActivity(callIntent);
            }
        });

        LocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/nit+rourkela/@22.2425197,84.9089783,14.94z"));
                startActivity(browserIntent);
            }
        });
        MailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:thecarthive@gmail.com"));
                startActivity(emailIntent);
            }
        });
        whatsappBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager packageManager = getContext().getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);

                String phone="+91 9304136129";
                String message="Hi ! ...";

                try {
                    String url = "https://api.whatsapp.com/send?phone="+ phone +"&text=" + URLEncoder.encode(message, "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        getContext().startActivity(i);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        return  view;

    }
}