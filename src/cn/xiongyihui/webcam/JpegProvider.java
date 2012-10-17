package cn.xiongyihui.webcam;

public interface JpegProvider {
    /**
     * Get a JPEG image as a byte array. if image is not available, it return null.
     * @return JPEG image as a byte array.
     */
    public byte[] getJpeg();
    
    /**
     * Wait for a new JPEG image and return it as a byte array
     * @return JPEG image as a byte array.
     * @throws InterruptedException
     */
    public byte[] getNewJpeg() throws InterruptedException;
}
