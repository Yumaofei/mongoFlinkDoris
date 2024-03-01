package com.yumaofei.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: mongoFlinkDoris
 * @description:
 * @author: Mr.YMF
 * @create: 2023-11-28 11:32
 **/

public class HttpUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * 以get方式调用第三方接口
     *
     * @param url
     * @return
     */
    public static String doGet(String url, Header[] headers, JSONObject params) {
        //创建HttpClient对象

        List<NameValuePair> paramList =new ArrayList<>();
        params.forEach((k, v) -> {
            paramList.add(new BasicNameValuePair(k, v.toString()));
        });
        String args = URLEncodedUtils.format(paramList, "UTF-8");

        HttpGet httpGet = new HttpGet(String.format("%s?%s", url, args));
        for (Header header: headers) {
            httpGet.addHeader(header);
        }
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
        try (CloseableHttpResponse response = HttpPool.getInstance().getClient().execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //返回json格式
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            log.error("get 请求异常", e);
        }
        return null;
    }

    public static String doGet(String url, JSONObject params) {
        //创建HttpClient对象

        List<NameValuePair> paramList =new ArrayList<>();
        params.forEach((k, v) -> {
            paramList.add(new BasicNameValuePair(k, v.toString()));
        });
        String args = URLEncodedUtils.format(paramList, "UTF-8");

        HttpGet httpGet = new HttpGet(String.format("%s?%s", url, args));
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
        try (CloseableHttpResponse response = HttpPool.getInstance().getClient().execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //返回json格式
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            log.error("get 请求异常", e);
        }
        return null;
    }

    public static String doGet(String url) {
        //创建HttpClient对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
        try (CloseableHttpResponse response = HttpPool.getInstance().getClient().execute(httpGet)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //返回json格式
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            log.error("get 请求异常", e);
        }
        return null;
    }

    /**
     * 以post方式调用第三方接口
     *
     * @param url
     * @param headers
     * @param entity
     * @return
     */
    public static String doPost(String url, Header[] headers, HttpEntity entity) throws Exception {
        String result = null;
        HttpPost httpPost = null;

        try {
            httpPost = new HttpPost(url);
            httpPost.setHeaders(headers);
            httpPost.addHeader("Connection", "close");
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            //设置请求参数
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = HttpPool.getInstance().getClient().execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    //返回json格式
                    result = EntityUtils.toString(response.getEntity(), StandardCharsets.US_ASCII);
                } else {
                    log.error(String.format("request error, Code: %s, msg: %s",
                            response.getStatusLine().getStatusCode(),
                            EntityUtils.toString(response.getEntity())));
                }
            }
        } catch (Exception e) {
            log.error("request error", e);

        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }

        return result;
    }

    public static String doPost(String url, JSONObject params) throws Exception {
        StringEntity entity = new StringEntity(params.toString(), "UTF-8");
        entity.setContentType("application/json");

        return doPost(url, null, entity);
    }

    public static String doPostByFrom(String url, Header[] headers, JSONObject params) throws Exception {
        List<NameValuePair> paramList=new ArrayList<>();
        params.forEach((k, v) -> {
            paramList.add(new BasicNameValuePair(k, v.toString()));
        });
        AbstractHttpEntity entity = new UrlEncodedFormEntity(paramList,"UTF-8");
        entity.setContentType("application/x-www-form-urlencoded");

        return doPost(url, headers, entity);
    }

    public static String doPostByMultipart(String url, Header[] headers, JSONObject params, File file) throws Exception {
        StringEntity entity = new StringEntity(params.toString(), "UTF-8");
        try (InputStream is = new FileInputStream(file)) {

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.setCharset(StandardCharsets.UTF_8);
            // 参数
            params.forEach((k, v) -> {
                entityBuilder.addTextBody(k, v.toString());
            });
            // 文件
            entityBuilder.addBinaryBody(
                    "file",
                    is,
                    ContentType.MULTIPART_FORM_DATA,
                    file.getName()
            );
            return doPost(url, headers, entity);

        } catch (Exception e) {
            log.error("请求失败", e);
        }
        return null;
    }

    public static <T> T doPost(String url, JSONObject params, Class<T> className) throws Exception {
        String response = doPost(url, params);
        if (StringUtils.isBlank(response)) return null;

        try {
            return JSONObject.parseObject(response, className);
        } catch (Exception e) {
            log.error(String.format("%s response 解析失败: %s", url, response), e);
            return null;
        }
    }

    public static String doPut(String url, Map<String, String> headers, HttpEntity entity) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

            HttpPut httpPut = new HttpPut(url);
            // 设置header
            headers.forEach(httpPut::setHeader);
            // 设置请求参数
            httpPut.setEntity(entity);

            HttpResponse response = httpClient.execute(httpPut);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //返回json格式
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException e) {
            log.error("put 请求异常", e);
        }
        return null;
    }

    public static void main(String[] args) throws Exception{
//        String QUERY_STOCK_ORDER_URL = "http://192.168.0.54:18001/order/queryOrder?api_key=MzUzYjMwMmM0NDU3NGY1NjUwNDU2ODdlNTM0ZTdkNmE6Mjg2OTI0";
//        JSONObject object = new JSONObject();
//        object.put("page_size", 2);
//        object.put("order_id", 230520173);

        String URL = "http://192.168.0.86:8081/api/dwapi/sale/adsSaleListingSkuPlatformSiteStatistic/list";
        JSONObject object = new JSONObject();
        object.put("platform","Shopee");
        object.put("sku","4955240");

        //首先获取token
        String response = doPost(URL, object);
        JSONObject responseObj = doPost(URL, object, JSONObject.class);
        //如果返回的结果是list形式的，需要使用JSONObject.parseArray转换
        //List<Result> list = JSONObject.parseArray(response, Result.class);
        System.out.println(response);
        System.out.println(responseObj);
    }
}
