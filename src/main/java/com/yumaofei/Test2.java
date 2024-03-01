package com.yumaofei;

import com.alibaba.fastjson2.JSONObject;
import com.yumaofei.util.HttpUtil;

/**
 * @program: mongoFlinkDoris
 * @description:
 * @author: Mr.YMF
 * @create: 2024-02-24 15:44
 **/

public class Test2 {
    public static void main(String[] args) throws Exception {
        // 定义接口地址
        String URL = "http://api.mairui.club/hslt/list/0e5e5198546de725f3";

        String responseData;

        //首先获取token
        try {
            responseData = HttpUtil.doGet(URL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(responseData);


    }
}
