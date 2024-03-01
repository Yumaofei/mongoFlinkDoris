package com.yumaofei.source.batch;

/**
 * @program: mongoFlinkDoris
 * @description: 从指定api获取数据
 * @author: Mr.YMF
 * @create: 2023-11-27 16:43
 **/

import com.yumaofei.util.HttpUtil;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.IOException;

public class ApiSource implements SourceFunction<String> {
    private String URL;

    public ApiSource(String URL){
        this.URL = URL;
    }

    private volatile boolean isRunning = true;



    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        while (isRunning) {
            // 模拟从指定接口获取数据的逻辑
            String data = fetchDataFromApi(URL);

            // 发送数据到 Flink 流
            ctx.collect(data);

            System.out.println(ctx);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }

    private String fetchDataFromApi(String URL) throws IOException {
        String response;
        //首先获取token
        try {
            response = HttpUtil.doGet(URL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 返回响应数据
        return response;
    }
}


