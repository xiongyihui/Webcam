package cn.xiongyihui.webcam;

import java.io.ByteArrayOutputStream;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

public class JpegFactory implements Camera.PreviewCallback, JpegProvider {
    
    private int mWidth;
    private int mHeight;
    private int mQuality;
    private ByteArrayOutputStream mJpegOutputStream;
    private byte[] mJpegData;
    
    public JpegFactory(int width, int height, int quality) {
        mWidth = width;
        mHeight = height;
        mQuality = quality;
        mJpegData = null;
        mJpegOutputStream = new ByteArrayOutputStream();
    }
    
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }
   
    public int getWidth() {
        return mWidth;
    }
    
    public int getHeight() {
        return mHeight;
    }
    
    public void setQuality(int quality) {
        mQuality = quality;
    }
    
    public int getQuality() {
        return mQuality;
    }
    
    public void onPreviewFrame(byte[] data, Camera camera) {       
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
        mJpegOutputStream.reset();
        yuvImage.compressToJpeg(new Rect(0, 0, mWidth, mHeight), mQuality, mJpegOutputStream);
        mJpegData = mJpegOutputStream.toByteArray();
        
        synchronized (mJpegOutputStream) {
            mJpegOutputStream.notifyAll();
        }
        
    }
    
    public byte[] getNewJpeg() throws InterruptedException {
        synchronized (mJpegOutputStream) {
            mJpegOutputStream.wait();
        }
        
        return mJpegData;
    }
    
    public byte[] getJpeg() {
        return mJpegData;
    }

}
