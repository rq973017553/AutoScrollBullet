package com.rq.autoscrollbullet.utils;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.disk.NoOpDiskTrimmableRegistry;
import com.facebook.common.internal.Supplier;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;
import com.facebook.common.memory.NoOpMemoryTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import java.io.File;

/**
 * fresco框架
 * https://blog.csdn.net/biezhihua/article/details/49893323
 * https://blog.csdn.net/chwnpp2/article/details/51063492
 */
public class ImageLoader {

    // 可分配内存分母
    private static final int MAX_MEMORY_CACHE_SIZE_RATIO = 12;

    //分配的可用内存
    private static final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();

    //使用的缓存数量
    private static final int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / MAX_MEMORY_CACHE_SIZE_RATIO;

    //默认图所放路径的文件夹名
    private static final String IMAGE_PIPELINE_CACHE_DIR = "image_cache";

    //默认图极低磁盘空间缓存的最大值
    private static final int MAX_DISK_CACHE_VERY_LOW_SIZE = 20 * ByteConstants.MB;

    //默认图低磁盘空间缓存的最大值
    private static final int MAX_DISK_CACHE_LOW_SIZE = 60 * ByteConstants.MB;

    //默认图磁盘缓存的最大值
    private static final int MAX_DISK_CACHE_SIZE = 100 * ByteConstants.MB;

    //小图所放路径的文件夹名
    private static final String IMAGE_PIPELINE_SMALL_CACHE_DIR = "image_cache_small";

    //小图极低磁盘空间缓存的最大值（特性：可将大量的小图放到额外放在另一个磁盘空间防止大图占用磁盘空间而删除了大量的小图）
    private static final int MAX_SMALL_DISK_VERY_LOW_CACHE_SIZE = 20 * ByteConstants.MB;

    //小图低磁盘空间缓存的最大值（特性：可将大量的小图放到额外放在另一个磁盘空间防止大图占用磁盘空间而删除了大量的小图）
    private static final int MAX_SMALL_DISK_LOW_CACHE_SIZE = 60 * ByteConstants.MB;

    public static void initialize(Application application){
        if (!Fresco.hasBeenInitialized()) {
            //设置内存紧张时的应对措施
            MemoryTrimmableRegistry memoryTrimmableRegistry = NoOpMemoryTrimmableRegistry.getInstance();
            memoryTrimmableRegistry.registerMemoryTrimmable(new MemoryTrimmable() {
                @Override
                public void trim(MemoryTrimType trimType) {
                    final double suggestedTrimRatio = trimType.getSuggestedTrimRatio();
                    if (MemoryTrimType.OnCloseToDalvikHeapLimit.getSuggestedTrimRatio() == suggestedTrimRatio
                            || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.getSuggestedTrimRatio() == suggestedTrimRatio
                            || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.getSuggestedTrimRatio() == suggestedTrimRatio) {
                        //清空内存缓存
                        ImagePipelineFactory.getInstance().getImagePipeline().clearMemoryCaches();
                    }
                }
            });

            //小图片的磁盘配置
            DiskCacheConfig diskSmallCacheConfig = DiskCacheConfig.newBuilder(application)
                    .setBaseDirectoryPath(DirectoryManager.getAPPCacheDirectory(application))//缓存图片基路径
                    .setBaseDirectoryName(IMAGE_PIPELINE_SMALL_CACHE_DIR)//文件夹名
                    .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//默认缓存的最大大小。
                    .setMaxCacheSizeOnLowDiskSpace(MAX_SMALL_DISK_LOW_CACHE_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
                    .setMaxCacheSizeOnVeryLowDiskSpace(MAX_SMALL_DISK_VERY_LOW_CACHE_SIZE)//缓存的最大大小,当设备极低磁盘空间
                    .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
                    .build();

            // fresco磁盘缓存
            DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(application)
                    .setBaseDirectoryPath(DirectoryManager.getAPPCacheDirectory(application))
                    .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)
                    .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//默认缓存的最大大小。
                    .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
                    .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERY_LOW_SIZE)//缓存的最大大小,当设备极低磁盘空间
                    .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
                    .build();

            // 缓存图片配置
            ImagePipelineConfig config = ImagePipelineConfig.newBuilder(application)
                    .setDownsampleEnabled(true)
                    .setMemoryTrimmableRegistry(memoryTrimmableRegistry)
                    .setBitmapsConfig(Bitmap.Config.ARGB_8888)
                    .setMainDiskCacheConfig(diskCacheConfig)
                    .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
                    .setBitmapMemoryCacheParamsSupplier(new Supplier<MemoryCacheParams>() {
                        @Override
                        public MemoryCacheParams get() {
                            return new MemoryCacheParams(
                                    MAX_MEMORY_CACHE_SIZE, // 内存缓存中总图片的最大大小,以字节为单位。
                                    Integer.MAX_VALUE, // 内存缓存中图片的最大数量。
                                    MAX_MEMORY_CACHE_SIZE, // 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                                    Integer.MAX_VALUE, // 内存缓存中准备清除的总图片的最大数量。
                                    Integer.MAX_VALUE); // 内存缓存中单个图片的最大大小。
                        }
                    }).build();
            Fresco.initialize(application, config);
        }
    }

    public static void loadFromUrl(final DraweeView view, final String url) {
        loadFromUrl(view, url, true);
    }

    public static void loadFromID(final DraweeView view, final int id){
        loadFromID(view, id, true);
    }

    public static void loadFromID(final DraweeView view, final int id, boolean anim){
        ResizeOptions resizeOptions = null;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (null != params) {
            int width = params.width;
            int height = params.height;
            if (width > 0 && height > 0) {
                resizeOptions = new ResizeOptions(width, height);
            }
        }
        ImageRequest request = ImageRequestBuilder.newBuilderWithResourceId(id)
                .setProgressiveRenderingEnabled(true)
                .setResizeOptions(resizeOptions)
                .setRotationOptions(RotationOptions.forceRotation(RotationOptions.NO_ROTATION))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(view.getController())
                .setRetainImageOnFailure(true)
                .build();
        view.setController(controller);
    }

    public static void loadFromUrl(final DraweeView view, final String url, BaseControllerListener listener) {
        if (!TextUtils.isEmpty(url)) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                    .setRotationOptions(RotationOptions.forceRotation(RotationOptions.NO_ROTATION))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setControllerListener(listener)
                    .setOldController(view.getController())
                    .setRetainImageOnFailure(true)
                    .build();
            view.setController(controller);
        }else {
            listener.onFailure(null, null);
        }
    }

    public static void loadFromUrl(DraweeView view, String url, boolean anim) {

        if (!TextUtils.isEmpty(url)) {
            ResizeOptions resizeOptions = null;
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (null != params) {
                int width = params.width;
                int height = params.height;
                if (width > 0 && height > 0) {
                    resizeOptions = new ResizeOptions(width, height);
                }
            }

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                    .setProgressiveRenderingEnabled(true)
                    .setResizeOptions(resizeOptions)
                    .setRotationOptions(RotationOptions.forceRotation(RotationOptions.NO_ROTATION))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(view.getController())
                    .setRetainImageOnFailure(true)
                    .setAutoPlayAnimations(anim)
                    .build();
            view.setController(controller);
        }
    }

    public static void loadFromUrl(DraweeView view, String url, boolean anim, int width, int height) {

        if (!TextUtils.isEmpty(url)) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                    .setProgressiveRenderingEnabled(true)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .setRotationOptions(RotationOptions.forceRotation(RotationOptions.NO_ROTATION))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(view.getController())
                    .setRetainImageOnFailure(true)
                    .setAutoPlayAnimations(anim)
                    .build();
            view.setController(controller);
        }
    }

    public static void loadFromUri(DraweeView view, Uri uri, int width, int height) {
        loadFromUri(view, uri, true, width, height);
    }

    public static void loadFromUri(DraweeView view, Uri uri, boolean anim, int width, int height) {

        if (uri != null) {
            ImageRequest request = ImageRequestBuilder
                    .newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(view.getController())
                    .setImageRequest(request)
                    .setAutoPlayAnimations(anim)
                    .build();
            view.setController(controller);
        }
    }

    public static boolean isImageDownloaded(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(url), null);
        return ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey) || ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey);
    }

    //return file or null
    public static File getCachedImageOnDisk(String url) {
        File localFile = null;
        if (!TextUtils.isEmpty(url)) {
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(url), null);
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }
        return localFile;
    }


    public static void loadImageUrlWithBlur(SimpleDraweeView view, String url) {
        if (url != null) {
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                            .setPostprocessor(blurPostprocessor).build())
                    .setOldController(view.getController())
                    .build();
            view.setController(controller);

        }
    }

    /**
     * 以高斯模糊显示。
     *
     * @param draweeView View。
     * @param url        url.
     * @param iterations 迭代次数，越大越魔化。
     * @param blurRadius 模糊图半径，必须大于0，越大越模糊。
     */
    public static void showUrlBlur(SimpleDraweeView draweeView, String url, int iterations, int blurRadius) {
        try {
            Uri uri = Uri.parse(url);
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setPostprocessor(new IterativeBoxBlurPostProcessor(iterations, blurRadius))
                    .build();
            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(request)
                    .build();
            draweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 以高斯模糊显示。
     *
     * @param draweeView View。
     * @param id        id.
     * @param iterations 迭代次数，越大越魔化。
     * @param blurRadius 模糊图半径，必须大于0，越大越模糊。
     */
    public static void showUrlBlur(SimpleDraweeView draweeView, int id, int iterations, int blurRadius) {
        try {
            ImageRequest request = ImageRequestBuilder.newBuilderWithResourceId(id)
                    .setPostprocessor(new IterativeBoxBlurPostProcessor(iterations, blurRadius))
                    .build();
            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(request)
                    .build();
            draweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static Postprocessor blurPostprocessor = new BasePostprocessor() {

        @Override
        public String getName() {
            return "blurPostprocessor";
        }

        @Override
        public void process(Bitmap bitmap) {

            int radius = 4;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int temp = 256 * divsum;
            int dv[] = new int[temp];
            for (i = 0; i < temp; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                            | (dv[gsum] << 8) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        }
    };
}
