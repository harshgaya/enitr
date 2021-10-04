package com.harsh.enitr.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.harsh.enitr.ProductDetailsActivity;
import com.harsh.enitr.R;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.harsh.enitr.Model.HorizontalProductScrollModel;

import java.util.List;

public class GridProductLayoutAdapter extends BaseAdapter {
    List<HorizontalProductScrollModel> horizontalProductScrollModelList;

    public GridProductLayoutAdapter(List<HorizontalProductScrollModel> horizontalProductScrollModelList) {
        this.horizontalProductScrollModelList = horizontalProductScrollModelList;
    }

    @Override
    public int getCount() {
        return horizontalProductScrollModelList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;
        if (convertView==null){
            view= LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_scroll_item_layout,null);
//            view.setElevation(0);
//            view.setBackgroundColor(Color.parseColor("#ffffff"));

//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent productDetailsIntent=new Intent(parent.getContext(), ProductDetailsActivity.class);
//                    productDetailsIntent.putExtra("PRODUCT_ID",horizontalProductScrollModelList.get(position).getProductID());
//                    parent.getContext().startActivity(productDetailsIntent);
//                }
//            });


        }else{
            view=convertView;
        }



        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent productDetailsIntent=new Intent(parent.getContext(), ProductDetailsActivity.class);
                productDetailsIntent.putExtra("PRODUCT_ID",horizontalProductScrollModelList.get(position).getProductID());
                parent.getContext().startActivity(productDetailsIntent);
            }
        });
        view.setElevation(0);
        view.setBackgroundColor(Color.parseColor("#ffffff"));
        view.setPadding(10, 10, 10, 10);

        ImageView productImage=view.findViewById(R.id.h_s_product_image);
        TextView productTitle=view.findViewById(R.id.h_s_product_title);
        TextView productDescription=view.findViewById(R.id.h_s_product_description);
        TextView productPrice=view.findViewById(R.id.h_s_product_price);

        Glide.with(parent.getContext()).load(horizontalProductScrollModelList.get(position).getProductImage()).apply(new RequestOptions().placeholder(R.drawable.placeholdericon)).into(productImage);
        productTitle.setText(horizontalProductScrollModelList.get(position).getProductTitle());
        productDescription.setText(horizontalProductScrollModelList.get(position).getProductDescription());
        productPrice.setText("Rs."+horizontalProductScrollModelList.get(position).getProductPrice()+"/-");
        return view;
    }
}
