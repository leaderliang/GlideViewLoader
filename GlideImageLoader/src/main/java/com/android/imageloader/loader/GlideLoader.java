package com.android.imageloader.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.imageloader.config.AnimationMode;
import com.android.imageloader.config.GlobalConfig;
import com.android.imageloader.config.PriorityMode;
import com.android.imageloader.config.ScaleMode;
import com.android.imageloader.config.ShapeMode;
import com.android.imageloader.config.SingleConfig;
import com.android.imageloader.transformation.GlideRoundTransform;
import com.android.imageloader.utils.DownLoadImageService;
import com.android.imageloader.utils.ImageUtil;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.ViewAnimationFactory;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import jp.wasabeef.glide.transformations.gpu.BrightnessFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ContrastFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.PixelationFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SwirlFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ToonFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;


/**
 * Created by doudou on 2017/4/10.
 * 参考:
 * https://mrfu.me/2016/02/28/Glide_Sries_Roundup/
 */

public class GlideLoader implements ILoader {

    /**
     * @param context        上下文
     * @param cacheSizeInM   Glide默认磁盘缓存最大容量250MB
     * @param memoryCategory 调整内存缓存的大小 LOW(0.5f) ／ NORMAL(1f) ／ HIGH(1.5f);
     * @param isInternalCD   true 磁盘缓存到应用的内部目录 / false 磁盘缓存到外部存
     */
    @Override
    public void init(Context context, int cacheSizeInM, MemoryCategory memoryCategory, boolean isInternalCD) {
        Glide.get(context).setMemoryCategory(memoryCategory); //如果在应用当中想要调整内存缓存的大小，开发者可以通过如下方式：
        GlideBuilder builder = new GlideBuilder();
        if (isInternalCD) {
            builder.setDiskCache(new InternalCacheDiskCacheFactory(context, cacheSizeInM * 1024 * 1024));
        } else {
            builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context, cacheSizeInM * 1024 * 1024));
        }

    }

    @Override
    public void request(final SingleConfig config) {

        //得到初始的 RequestOptions
        RequestOptions requestOptions = getRequestOptions(config);

        //得到一个正确类型的 RequestBuilder(bitmap or 其他加载)
        RequestBuilder requestBuilder = getRequestBuilder(config);

        if (requestBuilder == null) {
            return;
        }
        //应用 RequestOptions
        requestBuilder.apply(requestOptions);


        //设置缩略图
        if (config.getThumbnail() != 0) { //设置缩略比例
            requestBuilder.thumbnail(config.getThumbnail());
        }

        //设置图片加载动画
        setAnimator(config, requestBuilder);

        if (config.isAsBitmap()) {//如果是获取bitmap,则回调
            SimpleTarget target = new SimpleTarget<Bitmap>(config.getWidth(), config.getHeight()) {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    if (config.getBitmapListener() != null) {
                        config.getBitmapListener().onSuccess(resource);
                    }
                }

            };
            requestBuilder.into(target);
        } else {//如果是加载图片，（无论是否为Gif）
            if (config.getTarget() instanceof ImageView) {
                requestBuilder.into((ImageView) config.getTarget());
            }
        }

    }

    private RequestOptions getRequestOptions(SingleConfig config) {
        RequestOptions options = new RequestOptions();

        if (config.getDiskCacheStrategy() != null) {
            options = options.diskCacheStrategy(config.getDiskCacheStrategy());
        }
        if (ImageUtil.shouldSetPlaceHolder(config)) {
            options = options.placeholder(config.getPlaceHolderResId());
        }

        int scaleMode = config.getScaleMode();
        switch (scaleMode) {
            case ScaleMode.CENTER_CROP:
                options.centerCrop();
                break;
            case ScaleMode.FIT_CENTER:
                options.fitCenter();
                break;
            default:
                options.fitCenter();
                break;
        }

        //设置图片加载的分辨 sp
        if (config.getoWidth() != 0 && config.getoHeight() != 0) {
            options.override(config.getoWidth(), config.getoHeight());
        }

        //是否跳过磁盘存储
        if (config.getDiskCacheStrategy() != null) {
            options.diskCacheStrategy(config.getDiskCacheStrategy());
        }
        //设置图片加载优先级
        setPriority(config, options);

        if (config.getErrorResId() > 0) {
            options.error(config.getErrorResId());
        }

        //设置 RequestOptions 关于 多重变换
        setShapeModeAndBlur(config, options);

        return options;
    }


    /**
     * 设置加载优先级
     *
     * @param config
     * @param options
     */
    private void setPriority(SingleConfig config, RequestOptions options) {
        switch (config.getPriority()) {
            case PriorityMode.PRIORITY_LOW:
                options.priority(Priority.LOW);
                break;
            case PriorityMode.PRIORITY_NORMAL:
                options.priority(Priority.NORMAL);
                break;
            case PriorityMode.PRIORITY_HIGH:
                options.priority(Priority.HIGH);
                break;
            case PriorityMode.PRIORITY_IMMEDIATE:
                options.priority(Priority.IMMEDIATE);
                break;
            default:
                options.priority(Priority.IMMEDIATE);
                break;
        }
    }

    /**
     * 设置加载进入动画
     *
     * @param config
     * @param request
     */
    private void setAnimator(SingleConfig config, RequestBuilder request) {
        if (config.getAnimationType() == AnimationMode.ANIMATIONID) {
            GenericTransitionOptions genericTransitionOptions = GenericTransitionOptions.with(config.getAnimationId());
            request.transition(genericTransitionOptions);
        } else if (config.getAnimationType() == AnimationMode.ANIMATOR) {
            GenericTransitionOptions genericTransitionOptions = GenericTransitionOptions.with(config.getAnimator());
            request.transition(genericTransitionOptions);
        } else if (config.getAnimationType() == AnimationMode.ANIMATION) {
            GenericTransitionOptions genericTransitionOptions = GenericTransitionOptions.with(new ViewAnimationFactory(config.getAnimation()));
            request.transition(genericTransitionOptions);
        } else {//设置默认的交叉淡入动画
            request.transition(DrawableTransitionOptions.withCrossFade());
        }
    }

    @Nullable
    private RequestBuilder getRequestBuilder(SingleConfig config) {

        RequestManager requestManager = Glide.with(config.getContext());
        RequestBuilder request = null;
        if (config.isAsBitmap()) {
            request = requestManager.asBitmap();

        } else if (config.isGif()) {
            request = requestManager.asGif();
        } else {
            request = requestManager.asDrawable();
        }
        if (!TextUtils.isEmpty(config.getUrl())) {
            request.load(ImageUtil.appendUrl(config.getUrl()));
            Log.e("TAG", "getUrl : " + config.getUrl());
        } else if (!TextUtils.isEmpty(config.getFilePath())) {
            request.load(ImageUtil.appendUrl(config.getFilePath()));
            Log.e("TAG", "getFilePath : " + config.getFilePath());
        } else if (!TextUtils.isEmpty(config.getContentProvider())) {
            request.load(Uri.parse(config.getContentProvider()));
            Log.e("TAG", "getContentProvider : " + config.getContentProvider());
        } else if (config.getResId() > 0) {
            request.load(config.getResId());
            Log.e("TAG", "getResId : " + config.getResId());
        } else if (config.getFile() != null) {
            request.load(config.getFile());
            Log.e("TAG", "getFile : " + config.getFile());
        } else if (!TextUtils.isEmpty(config.getAssertspath())) {
            request.load(config.getAssertspath());
            Log.e("TAG", "getAssertspath : " + config.getAssertspath());
        } else if (!TextUtils.isEmpty(config.getRawPath())) {
            request.load(config.getRawPath());
            Log.e("TAG", "getRawPath : " + config.getRawPath());
        }
        return request;
    }

    /**
     * 设置图片滤镜和形状
     *
     * @param config
     * @param options
     */
    private void setShapeModeAndBlur(SingleConfig config, RequestOptions options) {

        int count = 0;

        Transformation[] transformation = new Transformation[statisticsCount(config)];

        if (config.isNeedBlur()) {
            transformation[count] = new BlurTransformation(config.getBlurRadius());
            count++;
        }

        if (config.isNeedBrightness()) {
            transformation[count] = new BrightnessFilterTransformation(config.getBrightnessLeve()); //亮度
            count++;
        }

        if (config.isNeedGrayscale()) {
            transformation[count] = new GrayscaleTransformation(); //黑白效果
            count++;
        }

        if (config.isNeedFilteColor()) {
            transformation[count] = new ColorFilterTransformation(config.getFilteColor());
            count++;
        }

        if (config.isNeedSwirl()) {
            transformation[count] = new SwirlFilterTransformation(0.5f, 1.0f, new PointF(0.5f, 0.5f)); //漩涡
            count++;
        }

        if (config.isNeedToon()) {
            transformation[count] = new ToonFilterTransformation(); //油画
            count++;
        }

        if (config.isNeedSepia()) {
            transformation[count] = new SepiaFilterTransformation(); //墨画
            count++;
        }

        if (config.isNeedContrast()) {
            transformation[count] = new ContrastFilterTransformation(config.getContrastLevel()); //锐化
            count++;
        }

        if (config.isNeedInvert()) {
            transformation[count] = new InvertFilterTransformation(); //胶片
            count++;
        }

        if (config.isNeedPixelation()) {
            transformation[count] = new PixelationFilterTransformation(config.getPixelationLevel()); //马赛克
            count++;
        }

        if (config.isNeedSketch()) {
            transformation[count] = new SketchFilterTransformation(); //素描
            count++;
        }

        if (config.isNeedVignette()) {
            transformation[count] = new VignetteFilterTransformation(new PointF(0.5f, 0.5f),
                    new float[]{0.0f, 0.0f, 0.0f}, 0f, 0.75f);//晕映
            count++;
        }

        switch (config.getShapeMode()) {
            case ShapeMode.RECT:

                break;
            case ShapeMode.RECT_ROUND://设置圆角
                /*下面这个不好使*/
//              transformation[count] = new RoundedCornersTransformation(config.getRectRoundRadius(), 0, RoundedCornersTransformation.CornerType.ALL);
                transformation[count] = new GlideRoundTransform(config.getRectRoundRadius());
                count++;
                break;
            case ShapeMode.OVAL://@deprecated Use {@link RequestOptions#circleCrop()}.
//                transformation[count] = new CropCircleTransformation();
//                count++;
                options = options.circleCrop();

                break;

            case ShapeMode.SQUARE:
                transformation[count] = new CropSquareTransformation();
                count++;
                break;
            default:
                break;
        }

        if (transformation.length != 0) {
            options.transforms(transformation);
        }
    }

    private int statisticsCount(SingleConfig config) {
        int count = 0;

        if (config.getShapeMode() == ShapeMode.RECT_ROUND || config.getShapeMode() == ShapeMode.SQUARE) {
            count++;
        }

        if (config.isNeedBlur()) {
            count++;
        }

        if (config.isNeedFilteColor()) {
            count++;
        }

        if (config.isNeedBrightness()) {
            count++;
        }

        if (config.isNeedGrayscale()) {
            count++;
        }

        if (config.isNeedSwirl()) {
            count++;
        }

        if (config.isNeedToon()) {
            count++;
        }

        if (config.isNeedSepia()) {
            count++;
        }

        if (config.isNeedContrast()) {
            count++;
        }

        if (config.isNeedInvert()) {
            count++;
        }

        if (config.isNeedPixelation()) {
            count++;
        }

        if (config.isNeedSketch()) {
            count++;
        }

        if (config.isNeedVignette()) {
            count++;
        }

        return count;
    }


    @Override
    public void pause() {
        Glide.with(GlobalConfig.context).pauseRequestsRecursive();
    }

    @Override
    public void resume() {
        Glide.with(GlobalConfig.context).resumeRequestsRecursive();
    }

    @Override
    public void clearDiskCache() {
        Glide.get(GlobalConfig.context).clearDiskCache();
    }

    @Override
    public void clearMomoryCache(View view) {
        Glide.with(view).clear(view);
    }

    @Override
    public void clearMomory() {
        Glide.get(GlobalConfig.context).clearMemory();
    }

    @Override
    public boolean isCached(String url) {
        return false;
    }

    @Override
    public void trimMemory(int level) {
        //已经过期了，内部会自动调用
//        Glide.with(GlobalConfig.context).onTrimMemory(level);
    }

    @Override
    public void clearAllMemoryCaches() {
        //已经过期了，内部会自动调用
//        Glide.with(GlobalConfig.context).onLowMemory();
    }

    @Override
    public void saveImageIntoGallery(DownLoadImageService downLoadImageService) {
        new Thread(downLoadImageService).start();
    }

}
