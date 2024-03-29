package com.weng.openapiinterface.client;

import com.google.gson.Gson;
import com.weng.openapiclientspringbootstarter.client.OpenApiClient;
import jakarta.annotation.Resource;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class HttpClientTest
{
    @Autowired
    private OpenApiClient openApiClient;


    @Test
    public void testGson() throws IOException
    {
        Map<String, String> paramMap=new HashMap<>();
        paramMap.put("ak","weng");
        paramMap.put("sk","weng");
        //构造json格式数据
        Gson gson=new Gson();
        String json = gson.toJson(paramMap);
        StringEntity entity = new StringEntity(json,"utf-8");
        System.out.println("entity====>"+entity);
        System.out.println("paramMap====>"+paramMap);
        System.out.println("json=====>"+json);
    }

    @Test
    public void testGet()
    {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("name","husiqi");
        String result = openApiClient.doGet("http://localhost:8081/api/girl/getGirlFriend", paramMap);
        System.out.printf("result=====>"+result);
    }
    @Test
    public void testPost() throws IOException
    {
        Map<String,String>map=new HashMap<>();
        map.put("name","husiqi");
        map.put("age","17");
        String result = openApiClient.doPost4Json("http://localhost:8081/api/girl/changeToGirl", map);
        System.out.println("result====>"+result);
    }

    @Test
    public void testMd5()
    {
        String s1 = DigestUtils.md5DigestAsHex("weng".getBytes());
        String s2 = DigestUtils.md5DigestAsHex("weng".getBytes());
        System.out.println("s1===>"+ s1);
        System.out.println("s2===>"+ s2);
    }



}