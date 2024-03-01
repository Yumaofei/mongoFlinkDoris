package com.yumaofei.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * @program: mongoFlinkDoris
 * @description:
 * @author: Mr.YMF
 * @create: 2023-11-28 11:32
 **/

public class HttpPool {

    private final PoolingHttpClientConnectionManager connPoolMng;
    private final RequestConfig requestConfig;

    private volatile static HttpPool httpClientInstance;
    private static final int maxTotal = 20;

    private HttpPool(){
        //初始化http连接池
        connPoolMng = new PoolingHttpClientConnectionManager();
        connPoolMng.setMaxTotal(maxTotal);
        connPoolMng.setDefaultMaxPerRoute(maxTotal);
        //初始化请求超时控制参数
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(90_000)
                .setConnectionRequestTimeout(90_000)
                .setSocketTimeout(90_000)
                .build();
    }

    /**
     * 单例模式
     * 使用双检锁机制，线程安全且在多线程情况下能保持高性能
     * @return
     */
    public static HttpPool getInstance(){
        if(httpClientInstance == null){
            synchronized (HttpPool.class) {
                if(httpClientInstance == null){
                    httpClientInstance = new HttpPool();
                }
            }
        }
        return httpClientInstance;
    }

    /**
     * 获取client客户端
     * @return
     */
    public CloseableHttpClient getClient() {
        return HttpClients.custom()
                .setConnectionManager(connPoolMng)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public static void close(){
        if (httpClientInstance == null) return;
        httpClientInstance.connPoolMng.shutdown();
        httpClientInstance.connPoolMng.close();
    }
}
