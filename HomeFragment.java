package com.harsh.enitr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.harsh.enitr.Adapter.CategoryAdapter;
import com.harsh.enitr.Adapter.HomePageAdapter;
import com.harsh.enitr.Model.CategoryModel;
import com.harsh.enitr.Model.HomePageModel;
import com.harsh.enitr.Model.HorizontalProductScrollModel;
import com.harsh.enitr.Model.SliderModel;
import com.harsh.enitr.Model.WishlistModel;

import java.util.ArrayList;
import java.util.List;

import static com.harsh.enitr.DBqueries.categoryModelList;
import static com.harsh.enitr.DBqueries.lists;
import static com.harsh.enitr.DBqueries.loadCategories;
import static com.harsh.enitr.DBqueries.loadFragmentData;
import static com.harsh.enitr.DBqueries.loadedCategoriesNames;


public class HomeFragment extends Fragment {

    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    public static SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView categoryRecyclerView;
    private List<CategoryModel> categoryModelFakeList=new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private  RecyclerView homePageRecyclerView;
    private List<HomePageModel> homePageModelFakeList=new ArrayList<>();
    private HomePageAdapter adapter;
    private ImageView noInternetConnection;
    private Button retryBtn;



    public HomeFragment() {
        // Required empty public constructor
    }



    @SuppressLint("WrongConstant")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);


        swipeRefreshLayout=view.findViewById(R.id.refresh_layout);
        noInternetConnection=view.findViewById(R.id.no_internet_connection);
        categoryRecyclerView = view.findViewById(R.id.category_recyclerview);
        homePageRecyclerView = view.findViewById(R.id.home_page_recyclerview);
        retryBtn=view.findViewById(R.id.retry_btn);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);


        swipeRefreshLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.colorPrimary),getContext().getResources().getColor(R.color.colorPrimary),getContext().getResources().getColor(R.color.colorPrimary));




        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        categoryRecyclerView.setLayoutManager(layoutManager);


        LinearLayoutManager testingLayoutManager = new LinearLayoutManager(getContext());
        testingLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        homePageRecyclerView.setLayoutManager(testingLayoutManager);

        /////////////  categories fake list

        categoryModelFakeList.add(new CategoryModel("null",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        ////// categories fake list

        /////homepage fake list

        List<SliderModel> sliderModelFakeList=new ArrayList<>();
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));


        List<HorizontalProductScrollModel>horizontalProductScrollModelFakeList=new ArrayList<>();
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));



        homePageModelFakeList.add(new HomePageModel(0,sliderModelFakeList));
        homePageModelFakeList.add(new HomePageModel(1,"","#dfdfdf"));
        homePageModelFakeList.add(new HomePageModel(2,"","#dfdfdf",horizontalProductScrollModelFakeList,new ArrayList<WishlistModel>()));
        homePageModelFakeList.add(new HomePageModel(3,"","#dfdfdf",horizontalProductScrollModelFakeList));


        /////homepage fake list

        categoryAdapter = new CategoryAdapter(categoryModelFakeList);


        adapter = new HomePageAdapter(homePageModelFakeList);



        connectivityManager=(ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo=connectivityManager.getActiveNetworkInfo();


        if (networkInfo !=null && networkInfo.isConnected()==true){
            MainActivity.drawer.setDrawerLockMode(0);
            noInternetConnection.setVisibility(View.GONE);
            retryBtn.setVisibility(View.GONE);
            categoryRecyclerView.setVisibility(View.VISIBLE);
            homePageRecyclerView.setVisibility(View.VISIBLE);

            if(categoryModelList.size()==0){
                loadCategories(categoryRecyclerView,getContext());

            }else {
                categoryAdapter=new CategoryAdapter(categoryModelList);
                categoryAdapter.notifyDataSetChanged();

            }
            categoryRecyclerView.setAdapter(categoryAdapter);

            if(lists.size()==0){
                loadedCategoriesNames.add("HOME");
                lists.add(new ArrayList<HomePageModel>());

                loadFragmentData(homePageRecyclerView,getContext(),0,"Home");


            }else {
                adapter = new HomePageAdapter(lists.get(0));
                adapter.notifyDataSetChanged();
            }
            homePageRecyclerView.setAdapter(adapter);

        }else{
            MainActivity.drawer.setDrawerLockMode(1);
            categoryRecyclerView.setVisibility(View.GONE);
            homePageRecyclerView.setVisibility(View.GONE);
            Glide.with(this).load(R.drawable.nointernet).into(noInternetConnection);
            noInternetConnection.setVisibility(View.VISIBLE);
            retryBtn.setVisibility(View.VISIBLE);
        }

////// refresh layout

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setRefreshing(true);
                reloadPage();

            }
        });
        /////refresh layout


        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadPage();
            }
        });



        return view;
    }

    @SuppressLint("WrongConstant")
    private void reloadPage(){
        networkInfo=connectivityManager.getActiveNetworkInfo();
//    categoryModelList.clear();
//    lists.clear();
//    loadedCategoriesNames.clear();
        DBqueries.clearData();
        if (networkInfo !=null && networkInfo.isConnected()==true) {
            MainActivity.drawer.setDrawerLockMode(0);
            noInternetConnection.setVisibility(View.GONE);
            retryBtn.setVisibility(View.GONE);

            categoryRecyclerView.setVisibility(View.VISIBLE);
            homePageRecyclerView.setVisibility(View.VISIBLE);

            categoryAdapter=new CategoryAdapter(categoryModelFakeList);
            adapter=new HomePageAdapter(homePageModelFakeList);

            categoryRecyclerView.setAdapter(categoryAdapter);
            homePageRecyclerView.setAdapter(adapter);


            loadCategories(categoryRecyclerView,getContext());


            loadedCategoriesNames.add("HOME");
            lists.add(new ArrayList<HomePageModel>());

            loadFragmentData(homePageRecyclerView,getContext(),0,"Home");




        }else{
            MainActivity.drawer.setDrawerLockMode(1);
            Toast.makeText(getContext(),"No internet Connection!",Toast.LENGTH_SHORT).show();
            categoryRecyclerView.setVisibility(View.GONE);
            homePageRecyclerView.setVisibility(View.GONE);
            Glide.with(getContext()).load(R.drawable.nointernet).into(noInternetConnection);
            noInternetConnection.setVisibility(View.VISIBLE);
            retryBtn.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}