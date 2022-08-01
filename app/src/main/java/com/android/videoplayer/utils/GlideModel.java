package com.android.videoplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;
import com.android.videoplayer.R;
import com.android.videoplayer.ui.widget.GlideCircleTransform;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

/**
 * Created by TinyHung@outlook.com
 * 2019/9/6
 * Glide图片加载
 */

public class GlideModel {

    private static final String TAG = "GlideModel";
    private volatile static GlideModel mInstance;

    public static synchronized GlideModel getInstance(){
        synchronized (GlideModel.class){
            if(null==mInstance){
                mInstance=new GlideModel();
            }
        }
        return mInstance;
    }

    public void loadImage(ImageView imageView, Object path){
        loadImage(imageView,path,false);
    }

    public void loadImage(ImageView imageView, Object path,boolean isBlur){
        loadImage(imageView,path, R.mipmap.ic_default_cover,isBlur);
    }

    public void loadImage(ImageView imageView, Object path, int error){
        loadImage(imageView,path,error,false);
    }

    public void loadImage(ImageView imageView, Object path ,int error,boolean isBlur){
        loadImage(null,imageView,path, error,isBlur);
    }

    public void loadImage(Context context, ImageView imageView, Object path ,int error,boolean isBlur){
        loadImage(context,imageView,path,R.mipmap.ic_default_cover,error,isBlur);
    }

    /**
     * 加载 静态、GIF图片
     * @param context
     * @param imageView
     * @param path 任意类型的图片地址
     * @param error 当获取图片失败的占位图
     */
    public void loadImage(Context context, ImageView imageView, Object path, int placeholder, int error ,boolean isBlur){
        if(null!=imageView&&null!=path){
            RequestOptions requestOptions = RequestOptions.centerInsideTransform();
            requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .skipMemoryCache(true)
                    .placeholder(placeholder)
                    .error(error);
            try {
//            Logger.d(TAG,"loadImage-->isBlur"+isBlur);
                if(isBlur){
                    Glide.with(null!=context?context:imageView.getContext())
                            .load(path)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .skipMemoryCache(true)
                            .placeholder(placeholder)
                            .error(error)
//                            .transform(new BlurTransformation(1, 2))
                            .into(imageView);
                }else{
                    Glide.with(null!=context?context:imageView.getContext())
                            .load(path)
                            .apply(requestOptions)
                            .into(imageView);
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载 静态、GIF图片
     * @param context
     * @param viewTarget TAG
     * @param path 任意类型的图片地址
     * @param error 当获取图片失败的占位图
     */
    public void loadImage(Context context, ImageViewTarget viewTarget, Object path, int error){
        if(null!=viewTarget&&null!=path){
            try {
                if(path instanceof String){
                    String imagePath= (String) path;
                    if (imagePath.endsWith(".gif")||imagePath.endsWith(".GIF")) {
                        Glide.with(context)
                                .asGif()
                                .load(imagePath)
                                .placeholder(R.mipmap.ic_default_cover)
                                .error(error)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .into(viewTarget);
                        return;
                    }
                }
                Glide.with(context)
                        .load(path)
                        .placeholder(R.mipmap.ic_default_cover)
                        .error(error)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .dontAnimate()
                        .into(viewTarget);
            }catch (RuntimeException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void loadImage(ImageView imageView, Object path, int width, int height){
        loadImage(null,imageView,path,width,height,R.mipmap.ic_default_cover);
    }

    public void loadImage(ImageView imageView, Object path, int width, int height, int error){
        loadImage(null,imageView,path,width,height,error);
    }

    /**
     * 加载 静态、GIF图片
     * @param context
     * @param imageView
     * @param path 任意类型的图片地址
     * @param width 指定输出宽
     * @param height 指定输出高
     * @param error 当获取图片失败的占位图
     */
    public void loadImage(Context context, ImageView imageView, Object path, int width, int height, int error){
        if(null!=imageView&&null!=path){
            try {
                if(path instanceof String){
                    String imagePath= (String) path;
                    if (imagePath.endsWith(".gif")||imagePath.endsWith(".GIF")) {
                        Glide.with(null!=context?context:imageView.getContext())
                                .asGif()
                                .load(imagePath)
                                .override(width, height)
                                .placeholder(R.mipmap.ic_default_cover)
                                .error(error)
                                .diskCacheStrategy(DiskCacheStrategy.DATA)
                                .into(imageView);
                        return;
                    }
                }
                Glide.with(null!=context?context:imageView.getContext())
                        .load(path)
                        .placeholder(R.mipmap.ic_default_cover)
                        .error(error)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .dontAnimate()
                        .override(width, height)
                        .into(imageView);
            }catch (RuntimeException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public void loadCirImage(ImageView imageView, Object path){
        loadCirImage(null,imageView,path, R.mipmap.ic_default_circle);
    }

    public void loadCirImage(ImageView imageView, Object path, int error){
        loadCirImage(null,imageView,path,error);
    }

    public void loadCirImage(Context context, ImageView imageView, Object path){
        loadCirImage(context,imageView,path,R.mipmap.ic_default_circle);
    }

    /**
     * 加载图片以圆形输出
     * @param context
     * @param imageView
     * @param path 任意类型的图片地址
     * @param error 当获取图片失败的占位图
     */
    public void loadCirImage(Context context, ImageView imageView, Object path, int error){
        if(null!=imageView&&null!=imageView.getContext()){
            try {
                Glide.with(null!=context?context:imageView.getContext())
                        .load(path)
                        .placeholder(R.mipmap.ic_default_circle)
                        .error(error)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .dontAnimate()
                        .transform(new GlideCircleTransform())
                        .into(imageView);
            }catch (RuntimeException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void loadBitmapImage(ImageView imageView, Object path){
        loadBitmapImage(null,imageView,path,R.mipmap.ic_default_cover);
    }

    public void loadBitmapImage(ImageView imageView, Object path, int error){
        loadBitmapImage(null,imageView,path,error);
    }

    /**
     * 加载图片以Bitmap输出
     * @param context
     * @param view
     * @param path 文件绝对路径
     * @param error 加载失败占位图
     */
    public void loadBitmapImage(Context context, final ImageView view, Object path, int error) {
        try {
            if(path instanceof String){
                String urlPath= (String) path;
                if(urlPath.endsWith(".GIF")||urlPath.endsWith(".gif")){
                    Glide.with(null!=context?context:view.getContext())
                            .asGif()
                            .load(urlPath)
                            .fitCenter()
                            .placeholder(R.mipmap.ic_default_cover)
                            .error(error)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .listener(new RequestListener<GifDrawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    if(null!=resource){
                                        view.setImageDrawable(resource);
                                    }
                                    return false;
                                }

                            })
                            .into(view);
                }else{
                    Glide.with(null!=context?context:view.getContext())
                            .asBitmap()
                            .load(path)
                            .error(error)
                            .dontAnimate()
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(new BitmapImageViewTarget(view) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    super.setResource(resource);
                                }
                            });
                }
            }else{
                Glide.with(null!=context?context:view.getContext())
                        .asBitmap()
                        .load(path)
                        .placeholder(R.mipmap.ic_default_cover)
                        .error(error)
                        .dontAnimate()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(new BitmapImageViewTarget(view) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                super.setResource(resource);
                            }
                        });
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 仅仅只是获取Bitmap
     * @param context
     * @param imageUrl
     * @param target
     */
    public void loadBitmap(Context context, String imageUrl, final BitmapTarget target) {
        if(null==context){
            if(null!=target){
                target.onLoadFailed(null);
            }
            return;
        }
        if(null==target) return;
        try {
            Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if(null!=target){
                                target.onResourceReady(resource);
                            }
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            if(null!=target){
                                target.onLoadFailed(errorDrawable);
                            }
                        }
                    });
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public Bitmap transformRadiusBitmap(Bitmap bitmap, int radius) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, radius, radius, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;
        }catch (Throwable e){
            e.printStackTrace();
        }
        return bitmap;
    }

    public interface BitmapTarget{
        void onResourceReady(Bitmap resource);
        void onLoadFailed(Drawable errorDrawable);
    }

    /**
     * 合成音频文件封面并显示到空间上
     * @param context
     * @param musicCover 显示对象
     * @param filePath 封面地址
     * @param frontBgSize 唱片机背景大小(宽高)
     * @param frontCoverSize 唱片机封面大小(宽高)
     * @param jukeBoxBgCover 唱片机背景封面
     * @param defaultCover 默认音频
     */
    public void setMusicComposeFront(final Context context, final ImageView musicCover, final String filePath,
                                     final float frontBgSize, final float frontCoverSize, final int jukeBoxBgCover, final int defaultCover) {
        if(null==context||null==musicCover||null==filePath){
            return;
        }
        loadBitmap(context, filePath, new BitmapTarget() {
            @Override
            public void onResourceReady(Bitmap bitmap) {
                if(null!=musicCover){
                    if(null==bitmap){
                        bitmap = BitmapFactory.decodeResource(context.getResources(),defaultCover);
                        bitmap=drawRoundBitmap(bitmap);
                    }
                    if(null!=bitmap){
                        LayerDrawable discDrawable = composeJukeBoxDrawable(context,bitmap,
                                frontBgSize,frontCoverSize,jukeBoxBgCover);
                        if(null!=discDrawable){
                            musicCover.setImageDrawable(discDrawable);
                        }
                    }
                }
            }

            @Override
            public void onLoadFailed(Drawable errorDrawable) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),defaultCover);
                if(null!=bitmap){
                    bitmap=drawRoundBitmap(bitmap);
                    if(null!=bitmap){
                        LayerDrawable discDrawable = composeJukeBoxDrawable(context,bitmap,
                                frontBgSize,frontCoverSize,jukeBoxBgCover);
                        if(null!=discDrawable){
                            musicCover.setImageDrawable(discDrawable);
                        }
                    }
                }
            }
        });
    }

    /**
     * 合成封面并显示
     * @param context
     * @param musicCover 显示对象
     * @param bitmap 封面位图
     * @param frontBgSize 唱片机背景大小(宽高)
     * @param frontCoverSize 唱片机封面大小(宽高)
     * @param jukeBoxBgCover 唱片机背景封面
     * @param defaultCover 默认音频封面
     */
    public void setMusicComposeFront(final Context context, final ImageView musicCover,Bitmap bitmap
            ,final float frontBgSize, final float frontCoverSize, final int jukeBoxBgCover, final int defaultCover){
        if(null!=context&&null!=musicCover){
            if(null==bitmap){
                bitmap = BitmapFactory.decodeResource(context.getResources(), defaultCover);
            }
            LayerDrawable discDrawable = composeJukeBoxDrawable(context,bitmap,frontBgSize,frontCoverSize,jukeBoxBgCover);
            if(null!=discDrawable){
                musicCover.setImageDrawable(discDrawable);
            }
        }
    }

    /**
     * 合成唱片机封面，将音乐封面合成在地图上层
     * @param context
     * @param bitmap 封面位图对象
     * @param frontJukeBoxScale 封面底盘大小比例
     * @param frontCoverScale 封面大小比例
     * @param jukeBoxBgCover 唱片背景封面
     * @return LayerDrawable
     */
    public LayerDrawable composeJukeBoxDrawable(Context context, Bitmap bitmap, float frontJukeBoxScale,
                                                float frontCoverScale, int jukeBoxBgCover) {
        if(frontJukeBoxScale<=0||frontCoverScale<=0){
            return null;
        }
        int screenWidth = ScreenUtils.getInstance().getScreenWidth();
        //背景图片大小
        int jukeBoxCoverBgSize = (int) (screenWidth * frontJukeBoxScale);
        //封面大小
        int jukeBoxCoverFgSize = (int) (screenWidth * frontCoverScale);
        //生成一张去除锯齿的背景位图
        Bitmap bgBitmapDisc = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
                context.getResources(), jukeBoxBgCover), jukeBoxCoverBgSize, jukeBoxCoverBgSize, true);
        BitmapDrawable bgDiscDrawable = new BitmapDrawable(bgBitmapDisc);
        //适配大小
        Bitmap finalBitmap = scalePicSize(jukeBoxCoverFgSize,bitmap);
        RoundedBitmapDrawable roundMusicDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), finalBitmap);
        //抗锯齿
        bgDiscDrawable.setAntiAlias(true);
        roundMusicDrawable.setAntiAlias(true);
        Drawable[] drawables = new Drawable[2];
        drawables[0] = bgDiscDrawable;
        drawables[1] = roundMusicDrawable;
        LayerDrawable layerDrawable = new LayerDrawable(drawables);
        int musicPicMargin = (int) ((frontJukeBoxScale - frontCoverScale) * screenWidth / 2);
        //调整专辑图片的四周边距，让其显示在正中
        layerDrawable.setLayerInset(1, musicPicMargin, musicPicMargin, musicPicMargin, musicPicMargin);
        return layerDrawable;
    }

    /**
     * 缩放封面大小，适配唱盘
     * @param musicPicSize
     * @param bitmap
     * @return Bitmap
     */
    private Bitmap scalePicSize(int musicPicSize, Bitmap bitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imageWidth = bitmap.getWidth();
        int sample = imageWidth / musicPicSize;
        int dstSample = 1;
        if (sample > dstSample) {
            dstSample = sample;
        }
        options.inJustDecodeBounds = false;
        //设置图片采样率
        options.inSampleSize = dstSample;
        //设置图片解码格式
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return Bitmap.createScaledBitmap(bitmap, musicPicSize, musicPicSize, true);
    }

    /**
     * 矩形转换为圆形
     * @param bitmap
     * @return Bitmap
     */
    public Bitmap drawRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }
        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
        final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom);
        final RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }
}