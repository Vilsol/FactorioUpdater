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
package me.vilsol.factorioupdater.util;

import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.messageresolver.AbstractMessageResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

public class WebUtils {
    
    private static final TemplateEngine templateEngine;
    private static final LessCompiler lessCompiler;
    
    static {
        FileTemplateResolver templateResolver = new FileTemplateResolver();
    
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix(WebUtils.class.getResource("/").getPath() + "/web/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);
        templateResolver.setCacheable(true);
    
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.setMessageResolver(new AbstractMessageResolver() {
            @Override
            public String resolveMessage(ITemplateContext context, Class<?> c, String variable, Object[] o){
                String[] search = variable.split("\\.");
                
                int pos = 1;
                Object object = context.getVariable(search[0]);
                while(pos < search.length && object != null){
                    if(object instanceof Map){
                        object = ((Map) object).get(search[pos]);
                    }else{
                        break;
                    }
                    
                    pos++;
                }
                
                if(object == null || pos < search.length){
                    return "??" + variable + "??";
                }
                
                return object.toString();
            }
    
            @Override
            public String createAbsentMessageRepresentation(ITemplateContext context, Class<?> c, String variable, Object[] o){
                return "??" + variable + "??";
            }
        });
        
        lessCompiler = new LessCompiler();
    }
    
    public static String getCSS(String file){
        URL resource = WebUtils.class.getResource("/web/styles/" + file);
        
        try{
            return lessCompiler.compile(new File(resource.toURI()));
        }catch(IOException | LessException | URISyntaxException e){
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String getTemplate(String file, CustomContext context){
        return templateEngine.process(file, context);
    }
    
    public static class CustomContext extends AbstractContext {
    
        public CustomContext(){
            super(Locale.ROOT);
        }
        
    }
    
}
