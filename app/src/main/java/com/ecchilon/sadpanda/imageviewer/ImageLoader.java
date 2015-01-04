package com.ecchilon.sadpanda.imageviewer;

import java.util.List;

import android.util.Log;
import android.util.SparseArray;
import com.ecchilon.sadpanda.api.DataLoader;
import com.ecchilon.sadpanda.overview.GalleryEntry;
import com.ecchilon.sadpanda.util.AsyncResultTask;

/**
 * Created by Alex on 21-9-2014.
 */
public class ImageLoader {

    public interface ImageListener {
        void onLoad(ImageEntry entry);
        void onError(Exception exception);
    }

    private final DataLoader mDataLoader;
    private final GalleryEntry mGalleryEntry;

    private SparseArray<ImageEntry> mEntryMap = new SparseArray<ImageEntry>();

    private Integer loadingPage = -1;

    public ImageLoader(DataLoader dataLoader, GalleryEntry entry) {
        this.mDataLoader = dataLoader;
        this.mGalleryEntry = entry;
    }

    public void getImage(int page, ImageListener listener) {
        new ImageLoadTask(listener, page).execute();
    }

    private class ImageLoadTask extends AsyncResultTask<Void, Void, ImageEntry> {

        private final ImageListener mListener;
        private final int page;

        public ImageLoadTask(ImageListener listener, int page) {
            super(false);
            this.mListener = listener;
            this.page = page;
        }

        @Override
        public ImageEntry call(Void... params) throws Exception{
            ImageEntry entry = mEntryMap.get(page);

            if(entry == null) {
                int gPage = page / DataLoader.PHOTO_PER_PAGE;

                boolean imLoading = false;
                if (loadingPage == -1) {
                    imLoading = true;
                    loadingPage = gPage;
                } else {
                    synchronized (loadingPage) {
                        loadingPage.wait();

                    }
                    //Entry has been loaded while we were sleeping
                    entry = mEntryMap.get(page);
                    if(entry != null) {
                        return mDataLoader.getPhotoInfo(mGalleryEntry, entry);
                    }
                }

                Log.d("ImageLoader", "Entry was null, loading new page: " + gPage);

                List<ImageEntry> entryList = mDataLoader.getPhotoList(mGalleryEntry,gPage);

                for(ImageEntry newEntry : entryList) {
                    mEntryMap.append(newEntry.getPage(), newEntry);
                }

                if (imLoading) {
                    synchronized (loadingPage) {
                        loadingPage.notifyAll();
                    }
                    loadingPage = -1;
                }


                entry = mEntryMap.get(page);
            }

            return mDataLoader.getPhotoInfo(mGalleryEntry, entry);
        }

        @Override
        protected void onSuccess(ImageEntry imageEntry) {
            mListener.onLoad(imageEntry);
        }

        @Override
        protected void onError(Exception e) {
            mListener.onError(e);
        }
    }
}
