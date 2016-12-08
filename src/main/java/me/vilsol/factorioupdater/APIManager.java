package me.vilsol.factorioupdater;

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
    
    private APIManager(){
        agent = HttpClientBuilder.create().disableRedirectHandling().setMaxConnPerRoute(100).build();
    }
    
    @Synchronized
    public void login(String username, String password) throws Exception {
        if(loggedIn){
            return;
        }
    
        HttpClient httpClient = HttpClientBuilder.create().disableRedirectHandling().setMaxConnPerRoute(100).build();
        String redirect = httpClient.execute(new HttpGet("https://mods.factorio.com/login")).getFirstHeader("Location").getValue().replace("http://", "https://");
        org.apache.http.HttpResponse loginPage = httpClient.execute(new HttpGet(redirect));
        
        if(loginPage.getHeaders("Location").length > 0){
            loginPage = httpClient.execute(new HttpGet(loginPage.getFirstHeader("Location").getValue()));
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
    
        String location = httpClient.execute(new HttpPost("https://auth.factorio.com/login/process?" + args)).getFirstHeader("Location").getValue();
        if(location.startsWith("http://auth") || location.startsWith("https://auth")){
            throw new InvalidCredentialsException();
        }
        
        httpClient.execute(new HttpGet(location));
    
        Matcher siteMatcher = hiddenSiteMatcher.matcher(Utils.readStream(httpClient.execute(new HttpGet("https://www.factorio.com/login")).getEntity().getContent()));
    
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
        HttpResponse siteLogin = httpClient.execute(httpPost);
        if(siteLogin.getStatusLine().getStatusCode() != 302){
            throw new InvalidCredentialsException();
        }
        
        loggedIn = true;
    }
    
    public static class InvalidCredentialsException extends Exception {}

}
