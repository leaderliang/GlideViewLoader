package com.android.glideviewloader;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.android.imageloader.loader.ImageLoader;

/**
 * TODO
 * 为了防止oom,加入如下代码，onTrimMemory、onLowMemory 清理内存
 *
 * @author dev.liang <a href="mailto:dev.liang@outlook.com">Contact me.</a>
 * @version 1.0
 * @since 2019/04/23 22:29
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        ImageLoader.init(getApplicationContext());
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1) {
            getResources();
        }
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 设置 app 字体不随系统字体设置改变
     */
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res != null) {
            Configuration config = res.getConfiguration();
            if (config != null && config.fontScale != 1.0f) {
                config.fontScale = 1.0f;
                /*设置默认*/
                /*网上很多地方写着使用config.setToDefaults();，实际上除了影响 app 内字体大小，还会影响很多地方的属性值。我们来看一下源码：*/
                /*config.setToDefaults();*/
                res.updateConfiguration(config, res.getDisplayMetrics());
               /*
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    createConfigurationContext(newConfig);
                } else {
                    res.updateConfiguration(newConfig, res.getDisplayMetrics());
                }
                */
            }
        }
        return res;
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        /*程序在内存清理的时候执行*/
        ImageLoader.trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
         /*低内存的时候执行*/
        ImageLoader.clearAllMemoryCaches();
    }


}
