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

    private final DataLoader mDataLoader;
    private final GalleryEntry mGalleryEntry;

    private SparseArray<ImageEntry> mEntryMap = new SparseArray<ImageEntry>();

    private Integer loadingPage = -1;

    public ImageLoader(DataLoader dataLoader, GalleryEntry entry) {
        this.mDataLoader = dataLoader;
        this.mGalleryEntry = entry;
    }

    public void getImage(int page, AsyncResultTask.Callback<ImageEntry> listener) {
        ImageLoadTask task = new ImageLoadTask(page);
        task.setListener(listener);
        task.execute();
    }

    private class ImageLoadTask extends AsyncResultTask<Void, Void, ImageEntry> {

        private final int page;

        public ImageLoadTask(int page) {
            super(false);
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
    }
}
