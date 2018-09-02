package com.sjhy.plugin.tool;

import com.sjhy.plugin.config.Settings;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.StringWriter;
import java.util.Map;

/**
 * Velocity工具类，主要用于代码生成
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class VelocityUtils {
    private volatile static VelocityUtils velocityUtils;

    /**
     * 单例模式
     */
    public static VelocityUtils getInstance() {
        if (velocityUtils == null) {
            synchronized (VelocityUtils.class) {
                if (velocityUtils == null) {
                    velocityUtils = new VelocityUtils();
                }
            }
        }
        return velocityUtils;
    }

    /**
     * 私有构造方法
     */
    private VelocityUtils() {
        velocityEngine = new VelocityEngine();
        // 修复部分用户的velocity日志记录无权访问velocity.log文件问题
        velocityEngine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute" );
        velocityEngine.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
    }

    /**
     * Velocity引擎
     */
    private final VelocityEngine velocityEngine;

    /**
     * 渲染模板
     *
     * @param template 模板字符串
     * @param map      参数集合
     * @return 渲染结果
     */
    public String generate(String template, Map<String, Object> map) {
        VelocityContext velocityContext = new VelocityContext();
        Settings settings = Settings.getInstance();
        if (map != null) {
            map.forEach(velocityContext::put);
        }
        StringWriter stringWriter = new StringWriter();
        // 设置编码
        velocityEngine.setProperty(VelocityEngine.INPUT_ENCODING, settings.getEncode());
        velocityEngine.setProperty(VelocityEngine.OUTPUT_ENCODING, settings.getEncode());
        // 生成代码
        velocityEngine.evaluate(velocityContext, stringWriter, "Velocity Code Generate", template);
        // 返回结果
        return stringWriter.toString();
    }
}
