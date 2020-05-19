package com.example.himalaya;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.himalaya.views.RoundRectImageView;

import net.lucode.hackware.magicindicator.MagicIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TestActivity extends AppCompatActivity {

    @BindView(R.id.main_indicator)
    MagicIndicator mainIndicator;
    @BindView(R.id.search_btn)
    ImageView searchBtn;
    @BindView(R.id.linear)
    LinearLayout linear;
    @BindView(R.id.content_pager)
    ViewPager contentPager;
    @BindView(R.id.main_track_cover)
    RoundRectImageView mainTrackCover;
    @BindView(R.id.main_head_title)
    TextView mainHeadTitle;
    @BindView(R.id.main_sub_title)
    TextView mainSubTitle;
    @BindView(R.id.main_play_control)
    ImageView mainPlayControl;
    @BindView(R.id.main_play_control_item)
    LinearLayout mainPlayControlItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}