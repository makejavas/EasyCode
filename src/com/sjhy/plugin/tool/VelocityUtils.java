package com.sjhy.plugin.tool;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class VelocityUtils {
    //单例模式
    private static class Instance {
        private static final VelocityUtils ME = new VelocityUtils();
    }
    public static VelocityUtils getInstance() {
        return Instance.ME;
    }
    private VelocityUtils(){
        velocityEngine = new VelocityEngine();
    }

    private final VelocityEngine velocityEngine;

    public String generate(String template, Map<String,Object> map) {
        VelocityContext velocityContext = new VelocityContext();
        if (map==null) {
            map = new HashMap<>();
        }
        map.forEach(velocityContext::put);
        StringWriter stringWriter = new StringWriter();
        velocityEngine.evaluate(velocityContext, stringWriter, "", template);
        return stringWriter.toString();
    }
}
