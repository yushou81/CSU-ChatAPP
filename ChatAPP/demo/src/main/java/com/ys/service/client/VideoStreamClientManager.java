package com.ys.service.client;

public class VideoStreamClientManager {
    // 创建一个静态的VideoStreamClient实例
    private static VideoStreamClient videoStreamClient = null;

    // 私有化构造方法，防止外部实例化
    private VideoStreamClientManager() {
    }

    // 提供一个全局访问点来获取VideoStreamClient实例
    public static VideoStreamClient getClient() {
        if (videoStreamClient == null) {
            //     /.mnbnxcxxxxxxxcvxcxxxxxxxxxxxxxxx如果Client还没有初始化，进行初始化
            videoStreamClient = new VideoStreamClient();
        }
        return videoStreamClient;
    }

    // 提供一个用于设置VideoStreamClient实例的静态方法（可选）
    public static void setClient(VideoStreamClient newClient) {
        videoStreamClient = newClient;
    }
}