package com.yumaofei;

import com.alibaba.fastjson2.JSONObject;
import com.yumaofei.util.HttpUtil;

/**
 * @program: mongoFlinkDoris
 * @description:
 * @author: Mr.YMF
 * @create: 2023-12-19 12:01
 **/

public class Test {
    public static void main(String[] args) {
        String URL = "http://192.168.0.86:8081/api/dwapi/sale/adsSaleListingSkuPlatformSiteStatistic/list";
        JSONObject object = new JSONObject();
        object.put("platform","Shopee");
        object.put("sku","4955240");

        String responseData;
        JSONObject responseObj;
        //首先获取token
        try {
            responseData = HttpUtil.doPost(URL, object);
            responseObj = HttpUtil.doPost(URL, object, JSONObject.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(responseData);
    }
}
