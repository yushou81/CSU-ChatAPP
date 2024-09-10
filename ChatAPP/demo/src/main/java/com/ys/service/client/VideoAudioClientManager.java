package com.ys.service.client;


public class VideoAudioClientManager {
    // 创建一个静态的VideoStreamClient实例
    private static VideoAudioClient videoAudioClient = null;

    // 私有化构造方法，防止外部实例化
    private VideoAudioClientManager() {
    }

    // 提供一个全局访问点来获取VideoStreamClient实例
    public static VideoAudioClient getClient() throws Exception {
        if (videoAudioClient == null) {
            //     /.mnbnxcxxxxxxxcvxcxxxxxxxxxxxxxxx如果Client还没有初始化，进行初始化
//            videoAudioClient = new VideoAudioClient(serverIp,videoPort,audioPort);
        }
        return videoAudioClient;
    }

    // 提供一个用于设置VideoStreamClient实例的静态方法（可选）
    public static void setClient(VideoAudioClient newClient) {
        videoAudioClient = newClient;
    }
}