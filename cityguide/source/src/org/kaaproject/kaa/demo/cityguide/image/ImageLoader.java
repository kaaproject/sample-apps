/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.demo.cityguide.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kaaproject.kaa.demo.cityguide.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Used to asynchronously decode {@link Bitmap} from a remote url address and show 
 * the resulting {@link Bitmap} via {@link LoadingImageView}.
 * Handles {@link Bitmap} caching in {@link MemoryCache} and {@link FileCache}.
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    private final static int CONNECTION_TIMEOUT = 30000;
    private final static int READ_TIMEOUT = 30000;

    private MemoryCache mMemoryCache = new MemoryCache();
    private FileCache mFileCache;

    private Map<LoadingImageView, ImageKey> imageViews = Collections
            .synchronizedMap(new WeakHashMap<LoadingImageView, ImageKey>());

    private ExecutorService mExecutorService;

    public ImageLoader(Context context) {
        mFileCache = new FileCache(context);
        mExecutorService = Executors.newFixedThreadPool(5);
    }

    public void loadImage(String url, LoadingImageView imageView, ImageType type) {
        ImageKey key = new ImageKey(type, url);
        imageViews.put(imageView, key);
        Bitmap bitmap = mMemoryCache.get(key);
        if (bitmap != null) {
            imageView.showBitmap(bitmap);
        } else {
            queuePhoto(key, imageView);
            imageView.setLoading();
        }
    }

    private void queuePhoto(ImageKey key, LoadingImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(key, imageView);
        mExecutorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(ImageKey key) {
        File file = mFileCache.getFile(key);
        Bitmap b = decodeFile(file, key.type.targetSize);
        if (b != null) {
            return b;
        }
        try {
            Bitmap bitmap;
            URL imageUrl = new URL(key.url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(file);
            Utils.copyStream(is, os);
            os.close();
            bitmap = decodeFile(file, key.type.targetSize);
            return bitmap;
        } catch (Throwable ex) {
            Log.e(TAG, "Unable to load bitmap!", ex);
            if (ex instanceof OutOfMemoryError) {
                mMemoryCache.clear();
            }
            return null;
        }
    }

    private Bitmap decodeFile(File file, int targetSize) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, o);
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < targetSize || height_tmp / 2 < targetSize) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to decode bitmap! File was not found", e);
        }
        return null;
    }

    private class PhotoToLoad {
        public ImageKey key;
        public LoadingImageView imageView;

        public PhotoToLoad(ImageKey key, LoadingImageView i) {
            this.key = key;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad)) {
                return;
            }
            Bitmap bmp = getBitmap(photoToLoad.key);
            mMemoryCache.put(photoToLoad.key, bmp);
            if (imageViewReused(photoToLoad)) {
                return;
            }
            BitmapDisplayer bmpDisplayer = new BitmapDisplayer(bmp, photoToLoad);
            Activity activity = (Activity) photoToLoad.imageView.getContext();
            activity.runOnUiThread(bmpDisplayer);
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        ImageKey key = imageViews.get(photoToLoad.imageView);
        return key == null || !key.equals(photoToLoad.key);
    }

    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad)) {
                return;
            }
            if (bitmap != null) {
                photoToLoad.imageView.showBitmap(bitmap);
            } else {
                photoToLoad.imageView.showFailedBitmap();
            }
        }
    }

    public void clearCache() {
        mMemoryCache.clear();
        mFileCache.clear();
    }

    public static enum ImageType {

        THUMBNAIL(128), SCREENAIL(512);

        int targetSize;

        ImageType(int targetSize) {
            this.targetSize = targetSize;
        }

    }

    public static class ImageKey {
        ImageType type;
        String url;

        public ImageKey(ImageType type, String url) {
            this.type = type;
            this.url = url;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            result = prime * result + ((url == null) ? 0 : url.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ImageKey other = (ImageKey) obj;
            if (type != other.type)
                return false;
            if (url == null) {
                if (other.url != null)
                    return false;
            } else if (!url.equals(other.url))
                return false;
            return true;
        }

    }

}
