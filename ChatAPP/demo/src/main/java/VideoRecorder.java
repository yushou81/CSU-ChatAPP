import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.*;

public class VideoRecorder {
    public static void main(String[] args) throws Exception {
        // 设置视频录制参数
        int width = 640;
        int height = 480;
        String outputFile = "output.mp4";

        // 打开摄像头
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0); // 0代表默认摄像头
        grabber.setImageWidth(width);
        grabber.setImageHeight(height);
        grabber.start();

        // 初始化录制器
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 使用H.264编码
        recorder.setFormat("mp4");
        recorder.setFrameRate(30);
        recorder.start();

        // 创建窗口来显示摄像头画面
        CanvasFrame canvas = new CanvasFrame("Video Recorder", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setVisible(true);

        // 开始录制
        while (canvas.isVisible()) {
            Frame frame = grabber.grab();
            if (frame != null) {
                recorder.record(frame); // 录制当前帧
                canvas.showImage(frame); // 显示当前帧
            }
        }

        // 关闭资源
        recorder.stop();
        grabber.stop();
        canvas.dispose();
    }
}

