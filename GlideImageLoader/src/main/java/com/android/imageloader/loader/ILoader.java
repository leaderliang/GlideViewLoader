package com.android.imageloader.loader;

import android.content.Context;
import android.view.View;

import com.android.imageloader.config.SingleConfig;
import com.android.imageloader.utils.DownLoadImageService;
import com.bumptech.glide.MemoryCategory;


/**
 * Created by doudou on 2017/4/10.
 */

public interface ILoader {

    void init(Context context, int cacheSizeInM, MemoryCategory memoryCategory, boolean isInternalCD);

    void request(SingleConfig config);

    void pause();

    void resume();

    void clearDiskCache();

    void clearMomoryCache(View view);

    void clearMomory();

    boolean isCached(String url);

    void trimMemory(int level);

    void clearAllMemoryCaches();

    void saveImageIntoGallery(DownLoadImageService downLoadImageService);
}
