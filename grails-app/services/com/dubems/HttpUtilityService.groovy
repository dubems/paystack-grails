package com.dubems

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

class HttpUtilityService {


    /**
     * Make A get request to the specified resource(url)
     * @param url
     * @param authString
     * @return
     */
    Map<String, String> getRequest(String url, String authString) {
        CloseableHttpClient client = HttpClientBuilder.create().build()
        HttpGet httpGet = new HttpGet(url)
        httpGet.addHeader("Authorization", authString)
        def responseMap = [:]

        client.execute(httpGet).withCloseable { result ->
            HttpEntity entity = result.getEntity() // get result
            String responseBody = EntityUtils.toString(entity) // extract response body
            JsonSlurper jsonSlurper = new JsonSlurper() // for parsing response
            responseMap = jsonSlurper.parseText(responseBody); // parse into json object
        }

        return responseMap as Map
    }


    /**
     * Make a post request the the specified resource(url)
     * @param url
     * @param data
     * @param authString
     * @return
     */
    Map<String, String> postRequest(String url, Map data, String authString) {
        //add a try catch here
        String postParams = new JsonBuilder(data).toPrettyString()
        CloseableHttpClient client = HttpClients.createDefault()
        StringEntity requestEntity = new StringEntity(postParams, ContentType.APPLICATION_JSON)
        HttpPost request = new HttpPost(url)
        request.setHeader("Authorization", authString)
        request.setEntity(requestEntity)

        def responseMap = [:]
        client.execute(request).withCloseable { response ->
            def entity = response.getEntity()
            String responseBody = EntityUtils.toString(entity)
            def jsonSlurper = new JsonSlurper() // for parsing response
            responseMap = jsonSlurper.parseText(responseBody); // parse into json object
        }

        return responseMap as Map
    }

}