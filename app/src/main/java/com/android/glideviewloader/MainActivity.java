package com.android.glideviewloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.imageloader.config.ScaleMode;
import com.android.imageloader.loader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static List<String> getImageUrl() {
        List<String> list = new ArrayList<>();
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556783439129&di=a4273466763cf63bcd9932d9f691186f&imgtype=0&src=http%3A%2F%2Ffile.youboy.com%2Fd%2F153%2F19%2F92%2F7%2F813577s.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556783439128&di=8e4053f79122b047229f4087870afd45&imgtype=0&src=http%3A%2F%2Fstatic-xiaoguotu.17house.com%2Fxgt%2Fs%2F21%2F1462885931154aa.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556783439127&di=261ffe971e5901405a8028f2cfdc5363&imgtype=0&src=http%3A%2F%2Fimg.warting.com%2Fallimg%2F2016%2F0310%2Ftdysqd2gp5v-2313.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556781467378&di=e644c7bc3bcefffd42c4a90f5b06d692&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201605%2F07%2F20160507140223_nYwc3.jpeg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556781467378&di=b79b56ea99be0dee0d57693ee3fb3f02&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201303%2F16%2F20130316114138_2CSdZ.jpeg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556781467378&di=9ba5b9ec3c4cd9e3c38c9204e5838428&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201801%2F21%2F20180121203444_FRzZy.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556781467377&di=2924dc884f07b3b1d28aee81b639df7b&imgtype=0&src=http%3A%2F%2Fb-ssl.duitang.com%2Fuploads%2Fitem%2F201806%2F07%2F20180607083016_VjzR3.jpeg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556781467376&di=904ffc4dd73d9b9f348ed1b0e924d990&imgtype=0&src=http%3A%2F%2Fimg.idol001.com%2Forigin%2F2018%2F02%2F09%2Fb6876a904b81707ddfa39dcf23069a091518158812.jpg");
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1556781467376&di=20526c6eb3f09fda242b6fd82fb17918&imgtype=0&src=http%3A%2F%2Fimage3.xyzs.com%2Fupload%2F9d%2Ffc%2F311%2F20150506%2F143087191032494_0.jpg");
        return list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout horizontalLayout = findViewById(R.id.horizontal_layout);
        //将需要滑动的布局动态添加到HorizontalScrollView包裹的布局中来实现滑动效果
        for (int i = 0; i < getImageUrl().size(); i++) {
            FrameLayout mHorizontalItem = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.view_horizontal_scrollview_item, null);
            ImageView ivHorizontalScrollview = mHorizontalItem.findViewById(R.id.iv_horizontal_scrollview);

            ImageLoader.with(this)
                    .url(getImageUrl().get(i))
                    .placeHolder(R.mipmap.login_bg)
                    .scale(ScaleMode.FIT_CENTER)
                    .rectRoundCorner(45)
                    .into(ivHorizontalScrollview);


            mHorizontalItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            horizontalLayout.addView(mHorizontalItem);
        }
    }


}
