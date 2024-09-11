package com.ys.service.client;

public class ClientManager {
    // 创建一个静态的Client实例
    private static Client client = null;

    // 私有化构造方法，防止外部实例化
    private ClientManager() {
    }

    // 提供一个全局访问点来获取Client实例
    public static Client getClient() throws Exception {
        if (client == null) {
            //     /.mnbnxcxxxxxxxcvxcxxxxxxxxxxxxxxx如果Client还没有初始化，进行初始化
            client = new Client();
        }
        return client;
    }

    // 提供一个用于设置Client实例的静态方法（可选）
    public static void setClient(Client newClient) {
        client = newClient;
    }
}

