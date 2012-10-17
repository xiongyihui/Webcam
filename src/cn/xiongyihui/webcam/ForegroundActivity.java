package cn.xiongyihui.webcam;

import java.io.IOException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class ForegroundActivity extends Activity implements SurfaceHolder.Callback {
    public final String TAG = "Webcam";
    
    private Camera mCamera = null;
    private MjpegServer mMjpegServer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.v(TAG, "onCreate");
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.semi_transparent));
        
        setContentView(R.layout.foreground);
        
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.foregroundSurfaceView);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        Log.v(TAG, "onResume()");
        
        
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        Log.v(TAG, "onPuase()");
        
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        
        if (mMjpegServer != null) {
            mMjpegServer.close();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "surfaceCreated()");
        
        int cameraId;
        int previewWidth;
        int previewHeight;
        int rangeMin;
        int rangeMax;
        int quality;
        int port;
        
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String cameraIdString = preferences.getString("settings_camera", null);       
        String previewSizeString = preferences.getString("settings_size", null);       
        String rangeString = preferences.getString("settings_range", null);
        String qualityString = preferences.getString("settings_quality", "50");
        String portString = preferences.getString("settings_port", "8080");
        
        // if failed, it means settings is broken.
        assert(cameraIdString != null && previewSizeString != null && rangeString != null);
        
        int xIndex = previewSizeString.indexOf("x");
        int tildeIndex = rangeString.indexOf("~");
        
        // if failed, it means settings is broken.
        assert(xIndex > 0 && tildeIndex > 0);
        
        try {
            cameraId = Integer.parseInt(cameraIdString);
            
            previewWidth = Integer.parseInt(previewSizeString.substring(0, xIndex - 1));
            previewHeight = Integer.parseInt(previewSizeString.substring(xIndex + 2));
            
            rangeMin = Integer.parseInt(rangeString.substring(0, tildeIndex - 1));
            rangeMax = Integer.parseInt(rangeString.substring(tildeIndex + 2));
            
            quality = Integer.parseInt(qualityString);
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Settings is broken");
            Toast.makeText(this, "Settings is broken", Toast.LENGTH_SHORT).show();
            
            finish();
            return;
        }
        
        mCamera = Camera.open(cameraId);
        if (mCamera == null) {
            Log.v(TAG, "Can't open camera" + cameraId);
            
            Toast.makeText(this, getString(R.string.can_not_open_camera),
                    Toast.LENGTH_SHORT).show();
            finish();
            
            return;
        }
        
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            Log.v(TAG, "SurfaceHolder is not available");
            
            Toast.makeText(this, "SurfaceHolder is not available",
                    Toast.LENGTH_SHORT).show();
            finish();
            
            return;
        }
        
        Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(previewWidth, previewHeight);
        parameters.setPreviewFpsRange(rangeMin, rangeMax);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
        
        JpegFactory jpegFactory = new JpegFactory(previewWidth, 
                previewHeight, quality);
        mCamera.setPreviewCallback(jpegFactory);
        
        mMjpegServer = new MjpegServer(jpegFactory);
        try {
            mMjpegServer.start(port);
        } catch (IOException e) {
            String message = "Port: " + port + " is not available";
            Log.v(TAG, message);
            
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }
    
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "surfaceDestroyed()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.foreground, menu);
        return true;
    }
}
