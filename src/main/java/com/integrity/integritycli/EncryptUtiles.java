package com.integrity.integritycli;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Lhz
 * @since 2021/12/27
 **/
@Log4j2
public class EncryptUtiles {

    private final static String host = "https://gwapi.gwm.cn/";
    private final static String fileEncrypt = "/sec/cdg/file_encrypt";
    private final static String fileDecrypt = "/sec/cdg/file_decrypt";
    private final static String packageEncrypt = "/sec/cdg/package_encrypt";
    private final static String packageDecrypt = "/sec/cdg/package_decrypt";

    /**
     * 文件加密
     * @param file 待加密文件
     * @param env test:测试环境，rest:正式环境
     * @param appKey 调用方应用appKey
     * @param appSecret 调用方应用appSecret
     * @return 加密后文件内容
     */
    public static String fileEncrypt(File file, String env, String appKey, String appSecret){
        try {
            String url = host + env + fileEncrypt;
            Map<String, String> headerMap = HmacSignUtil.createSignHeader(appKey, appSecret, url, "post");
            HttpResponseObj result = uploadFile(url, file, headerMap);
            return result.toString();
        }  catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] fileEncryptByte(File file, String env, String appKey, String appSecret){
        try {
            String url = host + env + fileEncrypt;
            Map<String, String> headerMap = HmacSignUtil.createSignHeader(appKey, appSecret, url, "post");
            HttpResponseObj result = uploadFile(url, file, headerMap);
            return result.toByte();
        }  catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件解密
     * @param file 待解密文件
     * @param env test:测试环境，rest:正式环境
     * @param appKey 调用方应用appKey
     * @param appSecret 调用方应用appSecret
     * @return 加密后文件内容
     */
    public static String fileDecrypt(File file, String env, String appKey, String appSecret){
        try {
            String url = host + env + fileDecrypt;
            Map<String, String> headerMap = HmacSignUtil.createSignHeader(appKey, appSecret, url, "post");
            HttpResponseObj result = uploadFile(url, file, headerMap);
            return result.toString();
        }  catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] fileDecryptByte(File file, String env, String appKey, String appSecret){
        try {
            String url = host + env + fileDecrypt;
            Map<String, String> headerMap = HmacSignUtil.createSignHeader(appKey, appSecret, url, "post");
            HttpResponseObj result = uploadFile(url, file, headerMap);
            //解密需要调用clone方法
            byte[] bytes = result.toByte();
            return bytes;
        }  catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件解密
     * @param file 待加密压缩包
     * @param env test:测试环境，rest:正式环境
     * @param appKey 调用方应用appKey
     * @param appSecret 调用方应用appSecret
     * @return 加密后文件内容
     */
    public static String packageEncrypt(File file, String env, String appKey, String appSecret){
        try {
            String url = host + env + packageEncrypt;
            Map<String, String> headerMap = HmacSignUtil.createSignHeader(appKey, appSecret, url, "post");
            HttpResponseObj result = uploadFile(url, file, headerMap);
            return result.toString();
        }  catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件解密
     * @param file 待解密压缩包
     * @param env test:测试环境，rest:正式环境
     * @param appKey 调用方应用appKey
     * @param appSecret 调用方应用appSecret
     * @return 加密后文件内容
     */
    public static String packageDecrypt(File file, String env, String appKey, String appSecret){
        try {
            String url = host + env + packageDecrypt;
            Map<String, String> headerMap = HmacSignUtil.createSignHeader(appKey, appSecret, url, "post");
            HttpResponseObj result = uploadFile(url, file, headerMap);
            return result.toString();
        }  catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static HttpResponseObj uploadFile(String url, File file, Map<String, String> headerMap) throws IOException, IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse httpResponse = null;
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000000).build();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
//        multipartEntityBuilder.addBinaryBody("file",file);
        FileBody fileBody = new FileBody(file);
        multipartEntityBuilder.addPart("file", fileBody);
        multipartEntityBuilder.addTextBody("file_size", String.valueOf(fileBody.getContentLength()));
        //multipartEntityBuilder.setContentType(ContentType.MULTIPART_FORM_DATA);
        multipartEntityBuilder.setCharset(Charset.forName("UTF-8"));
        HttpEntity httpEntity = multipartEntityBuilder.build();
        httpPost.setEntity(httpEntity);
        if (headerMap != null) {
            Iterator var38 = headerMap.entrySet().iterator();
            while(var38.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)var38.next();
                httpPost.addHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        httpResponse = httpClient.execute(httpPost);
        HttpEntity responseEntity = httpResponse.getEntity();
        HttpResponseObj httpResponseObj = new HttpResponseObj();
        httpResponseObj.setHttpClient(httpClient);
        httpResponseObj.setHttpResponse(httpResponse);
        log.info("加解密结果--------------->"+httpResponse.getStatusLine().getStatusCode());
        httpResponseObj.setResponseEntity(responseEntity);
        return httpResponseObj;
    }

    public static class HttpResponseObj{
        private HttpEntity responseEntity;
        private CloseableHttpClient httpClient;
        private CloseableHttpResponse httpResponse;

        private void close() throws IOException {
            if(httpResponse!=null){
                httpResponse.close();
            }
            if(httpClient!=null){
                httpClient.close();
            }
        }

        public byte[] toByte(){
            if(httpResponse == null) return null;
            int statusCode= httpResponse.getStatusLine().getStatusCode();
            if(statusCode != 200){
                return null;
            }
            byte[] bytes = null;
            try {
                bytes = EntityUtils.toByteArray(responseEntity);
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bytes;
        }

        public String toString(){
            if(httpResponse == null) return null;
            int statusCode= httpResponse.getStatusLine().getStatusCode();
            if(statusCode != 200){
                return null;
            }
            String content = null;
            try {
                content = EntityUtils.toString(responseEntity);
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }

        public HttpEntity getResponseEntity() {
            return responseEntity;
        }

        public void setResponseEntity(HttpEntity responseEntity) {
            this.responseEntity = responseEntity;
        }

        public CloseableHttpClient getHttpClient() {
            return httpClient;
        }

        public void setHttpClient(CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public CloseableHttpResponse getHttpResponse() {
            return httpResponse;
        }

        public void setHttpResponse(CloseableHttpResponse httpResponse) {
            this.httpResponse = httpResponse;
        }
    }

}
