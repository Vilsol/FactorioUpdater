/*
 * FactorioUpdater - The best factorio mod manager
 * Copyright 2016 The FactorioUpdater Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.vilsol.factorioupdater.managers;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import me.vilsol.factorioupdater.util.Utils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class APIManager {
    
    private static final Pattern hiddenModMatcher = Pattern.compile("<input.*?type=\"hidden\".*?name=\"(.+?)\".*?value=\"(.+?)\">");
    private static final Pattern hiddenSiteMatcher = Pattern.compile("<input.*?name=\"(.+?)\".*?type=\"hidden\".*?value=\"(.+?)\">");
    
    @Getter
    private static APIManager instance = new APIManager();
    
    @Getter
    private final HttpClient agent;
    
    @Getter
    @Setter
    private boolean loggedIn;
    
    private String username;
    private String password;
    
    private APIManager(){
        agent = HttpClientBuilder.create().disableRedirectHandling().setMaxConnPerRoute(100).build();
    }
    
    @Synchronized
    public void login(String username, String password) throws InvalidCredentialsException, IOException {
        if(this.username == null){
            this.username = username;
            this.password = password;
        }
        
        if(loggedIn){
            return;
        }
    
        String redirect = agent.execute(new HttpGet("https://mods.factorio.com/login")).getFirstHeader("Location").getValue().replace("http://", "https://");
        org.apache.http.HttpResponse loginPage = agent.execute(new HttpGet(redirect));
        
        if(loginPage.getHeaders("Location").length > 0){
            loginPage = agent.execute(new HttpGet(loginPage.getFirstHeader("Location").getValue()));
        }
    
        Matcher modMatcher = hiddenModMatcher.matcher(Utils.readStream(loginPage.getEntity().getContent()));
        
        String args = "";
        while(modMatcher.find()){
            if(!args.equals("")){
                args += "&";
            }
            
            args += modMatcher.group(1) + "=" + URLEncoder.encode(modMatcher.group(2), "UTF-8");
        }
        
        args += "&username=" + URLEncoder.encode(username, "UTF-8");
        args += "&password=" + URLEncoder.encode(password, "UTF-8");
    
        String location = agent.execute(new HttpPost("https://auth.factorio.com/login/process?" + args)).getFirstHeader("Location").getValue();
        if(location.startsWith("http://auth") || location.startsWith("https://auth")){
            throw new InvalidCredentialsException();
        }
        
        agent.execute(new HttpGet(location));
    
        Matcher siteMatcher = hiddenSiteMatcher.matcher(Utils.readStream(agent.execute(new HttpGet("https://www.factorio.com/login")).getEntity().getContent()));
    
        List<NameValuePair> params = new ArrayList<>();
        args = "";
        while(siteMatcher.find()){
            if(!args.equals("")){
                args += "&";
            }
        
            args += siteMatcher.group(1) + "=" + URLEncoder.encode(siteMatcher.group(2), "UTF-8");
            params.add(new BasicNameValuePair(siteMatcher.group(1), siteMatcher.group(2)));
        }
    
        args += "&username_or_email=" + URLEncoder.encode(username, "UTF-8");
        args += "&password=" + URLEncoder.encode(password, "UTF-8");
        args += "&action=Login";
    
        params.add(new BasicNameValuePair("username_or_email", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("action", "Login"));
    
        HttpPost httpPost = new HttpPost("https://www.factorio.com/login?" + args);
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        HttpResponse siteLogin = agent.execute(httpPost);
        if(siteLogin.getStatusLine().getStatusCode() != 302){
            throw new InvalidCredentialsException();
        }
        
        loggedIn = true;
    }
    
    public void reLogin(){
        try{
            loggedIn = false;
            login(username, password);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static class InvalidCredentialsException extends Exception {}

}
