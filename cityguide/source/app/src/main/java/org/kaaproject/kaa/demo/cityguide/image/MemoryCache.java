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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.kaaproject.kaa.demo.cityguide.image.ImageLoader.ImageKey;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Used to store image {@link Bitmap} into the memory.
 */
public class MemoryCache {

    private static final String TAG = MemoryCache.class.getSimpleName();

    private static final int DEFAULT_LIMIT = 1000000;

    private Map<ImageKey, Bitmap> cache = Collections
            .synchronizedMap(new LinkedHashMap<ImageKey, Bitmap>(10, 1.5f, true));

    private long mLimit = DEFAULT_LIMIT;
    private long mSize = 0;

    public MemoryCache() {
        setLimit(Runtime.getRuntime().maxMemory() / 4);
    }

    public void setLimit(long newLimit) {
        mLimit = newLimit;
        Log.i(TAG, "MemoryCache will use up to " + mLimit / 1024. / 1024.
                + "MB");
    }

    public Bitmap get(ImageKey id) {
        return cache.containsKey(id) ? cache.get(id) : null;
    }

    public void put(ImageKey id, Bitmap bitmap) {
        try {
            if (cache.containsKey(id)) {
                mSize -= getSizeInBytes(cache.get(id));
            }
            cache.put(id, bitmap);
            mSize += getSizeInBytes(bitmap);
            checkSize();
        } catch (Throwable th) {
            Log.e(TAG, "Unable to put bitmap to memory cache!", th);
        }
    }

    private void checkSize() {
        Log.i(TAG, "cache size=" + mSize + " length=" + cache.size());
        if (mSize > mLimit) {
            Iterator<Entry<ImageKey, Bitmap>> iter = cache.entrySet()
                    .iterator();
            while (iter.hasNext()) {
                Entry<ImageKey, Bitmap> entry = iter.next();
                mSize -= getSizeInBytes(entry.getValue());
                iter.remove();
                if (mSize <= mLimit) {
                    break;
                }
            }
            Log.i(TAG, "Clean cache. New size " + cache.size());
        }
    }

    public void clear() {
        cache.clear();
        mSize = 0;
    }

    private long getSizeInBytes(Bitmap bitmap) {
        return bitmap != null ? bitmap.getRowBytes() * bitmap.getHeight() : 0;
    }
}
