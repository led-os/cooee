package com.cooeeui.downloader.core;

import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.cooeeui.downloader.core.DLCons.Base.DEFAULT_TIMEOUT;

public class DLThread implements Runnable {

    private static final String TAG = DLThread.class.getSimpleName();

    private DLThreadInfo dlThreadInfo;
    private DLInfo dlInfo;
    private IDLThreadListener listener;

    public DLThread(DLThreadInfo dlThreadInfo, DLInfo dlInfo, IDLThreadListener listener) {
        this.dlThreadInfo = dlThreadInfo;
        this.listener = listener;
        this.dlInfo = dlInfo;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        HttpURLConnection conn = null;
        RandomAccessFile raf = null;
        InputStream is = null;
        try {
            conn = (HttpURLConnection) new URL(dlInfo.realUrl).openConnection();
            conn.setConnectTimeout(DEFAULT_TIMEOUT);
            conn.setReadTimeout(DEFAULT_TIMEOUT);

            addRequestHeaders(conn);

            raf = new RandomAccessFile(dlInfo.file, "rwd");
            raf.seek(dlThreadInfo.start);

            is = conn.getInputStream();

            byte[] b = new byte[4096];
            int len;
            while (!dlThreadInfo.isStop && (len = is.read(b)) != -1) {
                dlThreadInfo.start += len;
                raf.write(b, 0, len);
                listener.onProgress(len);
            }
            if (dlThreadInfo.isStop) {
                if (DLCons.DEBUG) {
                    Log.d(TAG, "Thread " + dlThreadInfo.id + " will be stopped.");
                }
                listener.onStop(dlThreadInfo);
            } else {
                if (DLCons.DEBUG) {
                    Log.d(TAG, "Thread " + dlThreadInfo.id + " will be finished.");
                }
                listener.onFinish(dlThreadInfo);
            }
        } catch (IOException e) {
            listener.onStop(dlThreadInfo);
            e.printStackTrace();
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
                if (null != raf) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    private void addRequestHeaders(HttpURLConnection conn) {
        for (DLHeader header : dlInfo.requestHeaders) {
            conn.addRequestProperty(header.key, header.value);
        }
        conn.setRequestProperty("Range", "bytes=" + dlThreadInfo.start + "-" + dlThreadInfo.end);
    }
}
