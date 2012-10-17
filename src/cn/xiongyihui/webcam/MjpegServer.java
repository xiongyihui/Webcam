package cn.xiongyihui.webcam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.StringTokenizer;

import android.util.Log;

public class MjpegServer extends SimpleServer {
    public final String TAG = "Webcam";
    
    private final static String BOUNDARY_STRING = "boundarystring";
    private JpegProvider mJpegProvider;
    
    public MjpegServer(JpegProvider jpegProvider) {
        mJpegProvider = jpegProvider;
    }

    @Override
    protected void handleConnection(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream(), "ASCII");
        BufferedReader in = new BufferedReader(reader);
        String request = in.readLine();
        if (request == null) {
            return;
        }
        
        StringTokenizer tokens = new StringTokenizer(request);
        String method = tokens.nextToken();
        
        // only response HTTP GET
        if (!method.equals("GET")) {
            return;
        }
        
        OutputStream out = socket.getOutputStream();
        
        String fileName = tokens.nextToken();
        if (fileName.equals("/?action=stream")) {
            sendStream(out);
        } else if (fileName.equals("/?action=snapshot")) {
            sendSnapshot(out);
        } else {
            sendDefault(out);
        }
        

        out.close();
    }
    
    private void sendStream(OutputStream out) throws IOException {
        Log.v(TAG, "Send stream");
        
        String header = "HTTP/1.0 200 OK\r\n" +
                "Connection: close\r\n" +
                "Server: Android Webcam\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                "Pragma: no-cache\r\n" + 
                "Content-Type: multipart/x-mixed-replace;boundary=" + BOUNDARY_STRING + "\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.flush();

        out.write(("--" + BOUNDARY_STRING + "\r\n").getBytes());

        while (true) {
            byte[] data = null;

            try {
                data = mJpegProvider.getNewJpeg();
            } catch (InterruptedException e) {
                Log.v(TAG, "Fail to get new JPEG image");

                break;
            }
            
            String subHeader = "Content-Type: image/jpeg\r\n" +
                               "Content-Length: " + data.length + "\r\n" +
                               "\r\n";
            out.write(subHeader.getBytes());
            out.write(data);
            out.write(("\r\n--" + BOUNDARY_STRING + "\r\n").getBytes());
            out.flush();
        }
    }
    
    private void sendSnapshot(OutputStream out) throws IOException {
        Log.v(TAG, "Send snapshot");
        
        byte[] data = mJpegProvider.getJpeg();
        if (data != null) {
            String header = "HTTP/1.0 200 OK\r\n" +
                            "Content-Type: image/jpeg\r\n" +
                            "Content-Length: " + data.length + "\r\n" +
                            "\r\n";
            out.write(header.getBytes());
            out.write(data);
        } else {
            String content = "Image is not available.";
            String header = "HTTP/1.0 200 OK\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Lenght: " + content.length() + "\r\n" +
                            "\r\n";
            out.write(header.getBytes());
            out.write(content.getBytes());
        }
        
        out.flush();
    }

    private void sendDefault(OutputStream out) throws IOException {
        String content = "<html>" +
                "<head><title>Webcam</title></head>" +
                "<body><img src='/?action=stream' alt='Camera is not available.' /></body>" +
                "</html>";
        String header = "HTTP/1.0 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Content-Lenght: " + content.length() + "\r\n" +
                "\r\n";
        out.write(header.getBytes());
        out.write(content.getBytes());
        out.flush();
    }
}
