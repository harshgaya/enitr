package com.harsh.enitr.Adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.harsh.enitr.Model.HomePageModel;
import com.harsh.enitr.Model.HorizontalProductScrollModel;
import com.harsh.enitr.Model.SliderModel;
import com.harsh.enitr.Model.WishlistModel;
import com.harsh.enitr.ProductDetailsActivity;
import com.harsh.enitr.R;
import com.harsh.enitr.RequestBookActivity;
import com.harsh.enitr.ViewAllActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomePageAdapter extends RecyclerView.Adapter {

    private List<HomePageModel> homePageModelList;
    private RecyclerView.RecycledViewPool recycledViewPool;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private int lastPosition = -1;

    public HomePageAdapter(List<HomePageModel> homePageModelList) {
        this.homePageModelList = homePageModelList;
        recycledViewPool = new RecyclerView.RecycledViewPool();
    }

    @Override
    public int getItemViewType(int position) {
        switch (homePageModelList.get(position).getType()) {
            case 0:
                return HomePageModel.BANNER_SLIDER;
            case 1:
                return HomePageModel.STRIP_AD_BANNER;

            case 2:
                return HomePageModel.HORIZONTAL_PRODUCT_VIEW;

            case 3:
                return HomePageModel.GRID_PRODUCT_VIEW;

            case 4:
                return HomePageModel.REQUEST_VIEW;


            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        switch (viewType) {
            case HomePageModel.BANNER_SLIDER:
                View bannerSliderView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.sliding_ad_layout, viewGroup, false);
                return new BannerSliderViewholder(bannerSliderView);
            case HomePageModel.STRIP_AD_BANNER:
                View stripAdView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.strip_ad_layout, viewGroup, false);
                return new StripAdBannerViewHolder(stripAdView);

            case HomePageModel.HORIZONTAL_PRODUCT_VIEW:
                View horizontalProductView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.horizontal_scroll_layout, viewGroup, false);
                return new HorizontalProductViewholder(horizontalProductView);

            case HomePageModel.GRID_PRODUCT_VIEW:
                View gridProductView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_product_layout, viewGroup, false);
                return new GridProductViewHolder(gridProductView);

            case HomePageModel.REQUEST_VIEW:
                View RequestView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.request_layout, viewGroup, false);
                return new RequestBookViewHolder(RequestView);

            default:
                return null;
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        switch (homePageModelList.get(position).getType()) {
            case HomePageModel.BANNER_SLIDER:

                List<SliderModel> sliderModelList = homePageModelList.get(position).getSliderModelList();
                ((BannerSliderViewholder) viewHolder).setBannerSliderViewPager(sliderModelList);
                break;

            case HomePageModel.STRIP_AD_BANNER:
                String resource = homePageModelList.get(position).getResource();
                String color = homePageModelList.get(position).getBackgroundColor();
                ((StripAdBannerViewHolder) viewHolder).setStripAd(resource, color);
                break;
            case HomePageModel.HORIZONTAL_PRODUCT_VIEW:
                String layoutColor = homePageModelList.get(position).getBackgroundColor();
                String HorizontalLayoutTitle = homePageModelList.get(position).getTitle();
                List<WishlistModel> viewAllProductList = homePageModelList.get(position).getViewAllProductList();
                List<HorizontalProductScrollModel> horizontalProductScrollModelList = homePageModelList.get(position).getHorizontalProductScrollModelList();
                ((HorizontalProductViewholder) viewHolder).setHorizontalProductLayout(horizontalProductScrollModelList, HorizontalLayoutTitle, layoutColor, viewAllProductList);
                break;
            case HomePageModel.GRID_PRODUCT_VIEW:
                String gridLayoutColor = homePageModelList.get(position).getBackgroundColor();
                String gridLayoutTitle = homePageModelList.get(position).getTitle();
                List<HorizontalProductScrollModel> gridProductScrollModelList = homePageModelList.get(position).getHorizontalProductScrollModelList();
                ((GridProductViewHolder) viewHolder).setGridProductLayout(gridProductScrollModelList, gridLayoutTitle, gridLayoutColor);

                break;

            case HomePageModel.REQUEST_VIEW:
                ((RequestBookViewHolder) viewHolder).setRequestView();

            default:
                return;
        }
        if (lastPosition < position) {
            Animation animation = AnimationUtils.loadAnimation(viewHolder.itemView.getContext(), R.anim.fade_in);
            viewHolder.itemView.setAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return homePageModelList.size();
    }


    public class BannerSliderViewholder extends RecyclerView.ViewHolder {
        private ViewPager bannerSliderViewPager;
        private int currentPage;
        private Timer timer;
        final private long DELAY_TIME = 3000;
        final private long PERIOD_TIME = 3000;

        private List<SliderModel> arrangedList;

        public BannerSliderViewholder(@NonNull View itemView) {
            super(itemView);
            bannerSliderViewPager = itemView.findViewById(R.id.banner_slider_view_pager);


        }

        private void setBannerSliderViewPager(final List<SliderModel> sliderModelList) {
            currentPage = 2;
            if (timer != null) {
                timer.cancel();
            }

            arrangedList = new ArrayList<>();

            for (int x = 0; x < sliderModelList.size(); x++) {
                arrangedList.add(x, sliderModelList.get(x));
            }
            arrangedList.add(0, sliderModelList.get(sliderModelList.size() - 2));
            arrangedList.add(1, sliderModelList.get(sliderModelList.size() - 1));
            arrangedList.add(sliderModelList.get(0));
            arrangedList.add(sliderModelList.get(1));


            SliderAdapter sliderAdapter = new SliderAdapter(arrangedList);
            bannerSliderViewPager.setAdapter(sliderAdapter);
            bannerSliderViewPager.setClipToPadding(false);
            bannerSliderViewPager.setPageMargin(20);
            bannerSliderViewPager.setCurrentItem(currentPage);


            ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    currentPage = i;
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    if (i == ViewPager.SCROLL_STATE_IDLE) {
                        pageLooper(arrangedList);
                    }
                }
            };
            bannerSliderViewPager.addOnPageChangeListener(onPageChangeListener);

            startBannerSlideShow(arrangedList);
            bannerSliderViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    pageLooper(arrangedList);
                    stopbannerSlideShow();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        startBannerSlideShow(arrangedList);
                    }
                    return false;
                }
            });

        }

        private void pageLooper(List<SliderModel> sliderModelList) {
            if (currentPage == sliderModelList.size() - 2) {
                currentPage = 2;
                bannerSliderViewPager.setCurrentItem(currentPage, false);
            }
            if (currentPage == 1) {
                currentPage = sliderModelList.size() - 3;
                bannerSliderViewPager.setCurrentItem(currentPage, false);
            }
        }

        private void startBannerSlideShow(final List<SliderModel> sliderModelList) {
            final Handler handler = new Handler();
            final Runnable update = new Runnable() {
                @Override
                public void run() {
                    if (currentPage >= sliderModelList.size()) {
                        currentPage = 1;
                    }
                    bannerSliderViewPager.setCurrentItem(currentPage++, true);
                }
            };
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(update);
                }
            }, DELAY_TIME, PERIOD_TIME);

        }

        private void stopbannerSlideShow() {
            timer.cancel();
        }
    }

    public class StripAdBannerViewHolder extends RecyclerView.ViewHolder {

        private ImageView stripAdImage;
        private ConstraintLayout stripAdContainer;

        public StripAdBannerViewHolder(@NonNull View itemView) {
            super(itemView);
            stripAdImage = itemView.findViewById(R.id.strip_ad_image);
            stripAdContainer = itemView.findViewById(R.id.strip_ad_container);
        }

        private void setStripAd(String resource, String color) {
            Glide.with(itemView.getContext()).load(resource).apply(new RequestOptions().placeholder(R.drawable.bannerplaceholder)).into(stripAdImage);

            stripAdImage.setBackgroundColor(Color.parseColor(color));
        }
    }

    private ConstraintLayout container;
    private TextView horizontalLayoutTitle;
    private Button horizontalLayoutViewAllBtn;
    private RecyclerView horiontalRecyclerView;

    public class HorizontalProductViewholder extends RecyclerView.ViewHolder {
        public HorizontalProductViewholder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            horizontalLayoutTitle = itemView.findViewById(R.id.horizontal_scroll_layout_title);
            horizontalLayoutViewAllBtn = itemView.findViewById(R.id.horizontal_scroll_view_all_btn);
            horiontalRecyclerView = itemView.findViewById(R.id.horizontal_scroll_layout_recyclerview);
            horiontalRecyclerView.setRecycledViewPool(recycledViewPool);
        }

        private void setHorizontalProductLayout(final List<HorizontalProductScrollModel> horizontalProductScrollModelList, final String title, final String color, final List<WishlistModel> viewAllProductList) {
            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        final String Selectedcity = task.getResult().getString("city");

                        container.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
                        horizontalLayoutTitle.setText(title);

                        for (final HorizontalProductScrollModel model : horizontalProductScrollModelList) {

                            if (!model.getProductID().isEmpty() && model.getProductTitle().isEmpty()) {

                                firebaseFirestore.collection("PRODUCTS")
                                        .document(model.getProductID())
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {

                                            model.setProductTitle(task.getResult().getString("product_title"));
                                            model.setProductImage(task.getResult().getString("product_image_1"));
                                            model.setProductPrice(task.getResult().getString("product_price"));

                                            WishlistModel wishlistModel = viewAllProductList.get(horizontalProductScrollModelList.indexOf(model));

                                            wishlistModel.setTotalRatings(task.getResult().getLong("total_ratings"));
                                            wishlistModel.setRating(task.getResult().getString("average_rating"));
                                            wishlistModel.setProductTitle(task.getResult().getString("product_title"));
                                            wishlistModel.setProductPrice(task.getResult().getString("product_price"));
                                            wishlistModel.setProductImage(task.getResult().getString("product_image_1"));
                                            wishlistModel.setFreeCoupens(task.getResult().getLong("free_coupens"));
                                            wishlistModel.setCuttedPrice(task.getResult().getString("cutted_price"));
                                            wishlistModel.setCOD(task.getResult().getBoolean("COD"));
                                            wishlistModel.setInStock(task.getResult().getLong("stock_quantity") > 0);

                                            if (horizontalProductScrollModelList.indexOf(model) == horizontalProductScrollModelList.size() - 1) {

                                                if (horiontalRecyclerView.getAdapter() != null) {
                                                    horiontalRecyclerView.getAdapter().notifyDataSetChanged();
                                                }
                                            }


                                        } else {
                                            ////do nothing
                                        }
                                    }
                                });
                            }
                        }


                        if (horizontalProductScrollModelList.size() > 8) {
                            horizontalLayoutViewAllBtn.setVisibility(View.VISIBLE);
                            horizontalLayoutViewAllBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ViewAllActivity.wishlistModelList = viewAllProductList;
                                    Intent viewAllIntent = new Intent(itemView.getContext(), ViewAllActivity.class);
                                    viewAllIntent.putExtra("layout_code", 0);
                                    viewAllIntent.putExtra("title", title);
                                    itemView.getContext().startActivity(viewAllIntent);
                                }
                            });
                        } else {
                            horizontalLayoutViewAllBtn.setVisibility(View.INVISIBLE);
                        }


                        HorizontalProductScrollAdapter horizontalProductScrollAdapter = new HorizontalProductScrollAdapter(horizontalProductScrollModelList);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext());
                        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                        horiontalRecyclerView.setLayoutManager(linearLayoutManager);
                        horiontalRecyclerView.setAdapter(horizontalProductScrollAdapter);
                        horizontalProductScrollAdapter.notifyDataSetChanged();
                    }
                }
            });


        }
    }


    public class GridProductViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout container;
        private TextView gridLayoutTitle;
        private Button gridLayoutViewAllBtn;
        private GridLayout gridProductLayout;


        public GridProductViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            gridLayoutTitle = itemView.findViewById(R.id.grid_product_layout_title);
            gridLayoutViewAllBtn = itemView.findViewById(R.id.grid_product_layout_viewall_btn);
            gridProductLayout = itemView.findViewById(R.id.grid_layout);


        }

        private void setGridProductLayout(final List<HorizontalProductScrollModel> horizontalProductScrollModelList, final String title, final String color) {


            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        //horizontalProductScrollModelList.clear();
                        final String Selectedcity = task.getResult().getString("city");

                        container.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
                        gridLayoutTitle.setText(title);


                        for (final HorizontalProductScrollModel model : horizontalProductScrollModelList) {

                            if (!model.getProductID().isEmpty() && model.getProductTitle().isEmpty()) {

                                firebaseFirestore.collection("PRODUCTS")
                                        .document(model.getProductID())

                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {

                                            model.setProductTitle(task.getResult().getString("product_title"));
                                            model.setProductImage(task.getResult().getString("product_image_1"));
                                            model.setProductPrice(task.getResult().getString("product_price"));

                                            setGridData(title, horizontalProductScrollModelList);


                                            if (horizontalProductScrollModelList.indexOf(model) == horizontalProductScrollModelList.size() - 1) {
                                                setGridData(title, horizontalProductScrollModelList);


                                            }


                                        } else {
                                            ////do nothing
                                        }
                                    }
                                });
                            }
                        }
                        setGridData(title, horizontalProductScrollModelList);

                        if (!title.equals("")) {
                            gridLayoutViewAllBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ViewAllActivity.horizontalProductScrollModelList = horizontalProductScrollModelList;
                                    Intent viewAllIntent = new Intent(itemView.getContext(), ViewAllActivity.class);
                                    viewAllIntent.putExtra("layout_code", 1);
                                    viewAllIntent.putExtra("title", title);
                                    itemView.getContext().startActivity(viewAllIntent);
                                }
                            });
                        }



                    }
                }
            });



        }

        private void setGridData(String title, final List<HorizontalProductScrollModel> horizontalProductScrollModelList) {

            for (int x = 0; x < 4; x++) {
                ImageView productImage = gridProductLayout.getChildAt(x).findViewById(R.id.h_s_product_image);
                TextView productTitle = gridProductLayout.getChildAt(x).findViewById(R.id.h_s_product_title);
                TextView productDescription = gridProductLayout.getChildAt(x).findViewById(R.id.h_s_product_description);
                TextView productPrice = gridProductLayout.getChildAt(x).findViewById(R.id.h_s_product_price);


                Glide.with(itemView.getContext()).load(horizontalProductScrollModelList.get(x).getProductImage()).apply(new RequestOptions().placeholder(R.drawable.bannerplaceholder)).into(productImage);
                productTitle.setText(horizontalProductScrollModelList.get(x).getProductTitle());
                productDescription.setText(horizontalProductScrollModelList.get(x).getProductDescription());
                productPrice.setText("Rs." + horizontalProductScrollModelList.get(x).getProductPrice() + "/-");


                gridProductLayout.getChildAt(x).setBackgroundColor(Color.parseColor("#ffffff"));

                if (!title.equals("")) {
                    final int finalX = x;
                    gridProductLayout.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent productDetailsIntent = new Intent(itemView.getContext(), ProductDetailsActivity.class);
                            productDetailsIntent.putExtra("PRODUCT_ID", horizontalProductScrollModelList.get(finalX).getProductID());
                            itemView.getContext().startActivity(productDetailsIntent);
                        }
                    });
                }

            }

//

        }
    }

    public class RequestBookViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout requestLayoutContainer;
        private LinearLayout requestLinearLayout;
        private TextView dontfindyourbooktxt, dontworrytxt;
        private Button requestBtn;

        public RequestBookViewHolder(@NonNull View itemView) {
            super(itemView);

            requestLayoutContainer = itemView.findViewById(R.id.requestLayoutContainer);
            requestLinearLayout = itemView.findViewById(R.id.requestLinearLayout);
            dontfindyourbooktxt = itemView.findViewById(R.id.dontfindyourbooktxt);
            dontworrytxt = itemView.findViewById(R.id.dontworrytxt);
            requestBtn = itemView.findViewById(R.id.requestBtn);


        }

        private void setRequestView() {

            requestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(itemView.getContext(), RequestBookActivity.class);
                    itemView.getContext().startActivity(intent);


                }
            });

        }
    }


}
