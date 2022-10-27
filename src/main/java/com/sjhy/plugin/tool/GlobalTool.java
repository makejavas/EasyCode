package com.sjhy.plugin.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.ReflectionUtil;
import com.sjhy.plugin.entity.DebugField;
import com.sjhy.plugin.entity.DebugMethod;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 全局工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/08/14 18:11
 */
@SuppressWarnings("unused")
public class GlobalTool extends NameUtils {
    private static volatile GlobalTool globalTool;

    /**
     * 私有构造方法
     */
    private GlobalTool() {
    }

    /**
     * 单例模式
     */
    public static GlobalTool getInstance() {
        if (globalTool == null) {
            synchronized (GlobalTool.class) {
                if (globalTool == null) {
                    globalTool = new GlobalTool();
                }
            }
        }
        return globalTool;
    }

    /**
     * 创建集合
     *
     * @param items 初始元素
     * @return 集合对象
     */
    public Set<?> newHashSet(Object... items) {
        return items == null ? new HashSet<>() : new HashSet<>(Arrays.asList(items));
    }

    /**
     * 创建列表
     *
     * @param items 初始元素
     * @return 列表对象
     */
    public List<?> newArrayList(Object... items) {
        return items == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(items));
    }

    /**
     * 创建有序Map
     *
     * @return map对象
     */
    public Map<?, ?> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * 创建无序Map
     *
     * @return map对象
     */
    public Map<?, ?> newHashMap() {
        return new HashMap<>(16);
    }

    /**
     * 获取字段，私有属性一样强制访问
     *
     * @param obj       对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public Object getField(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        Class<?> cls = obj.getClass();
        return ReflectionUtil.getField(cls, obj, Object.class, fieldName);
    }

    /**
     * 无返回执行，用于消除返回值
     *
     * @param obj 接收执行返回值
     */
    public void call(Object... obj) {

    }

    /**
     * 获取某个类的所有字段
     *
     * @param cls 类
     * @return 所有字段
     */
    private List<Field> getAllFieldByClass(Class<?> cls) {
        List<Field> result = new ArrayList<>();
        do {
            result.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        } while (!cls.equals(Object.class));
        return result;
    }

    /**
     * 调式对象
     *
     * @param obj 对象
     * @return 调式JSON结果
     */
    public String debug(Object obj) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (obj == null) {
            result.put("title", "调试对象为null");
            return JSON.toJsonByFormat(result);
        }
        // 获取类
        Class<?> cls = obj.getClass();
        result.put("title", String.format("调试：%s", cls.getName()));
        // 方法列表
        List<DebugMethod> debugMethodList = new ArrayList<>();
        // 排除方法
        List<String> filterMethodName = Arrays.asList("hashCode", "toString", "equals", "getClass", "clone", "notify", "notifyAll", "wait", "finalize");
        for (Method method : cls.getMethods()) {
            if (filterMethodName.contains(method.getName())) {
                continue;
            }
            DebugMethod debugMethod = new DebugMethod();
            String methodName = method.getName();
            debugMethod.setName(methodName);
            debugMethod.setDesc(method.toGenericString());
            // 针对get，is开头的无参方法进行调用并取值。
            if ((methodName.startsWith("get") || methodName.startsWith("is"))) {
                if (method.getParameterCount() == 0) {
                    try {
                        Object val = method.invoke(obj);
                        if (val != null) {
                            debugMethod.setValue(val.toString());
                        }
                    } catch (IllegalAccessException e) {
                        // 忽略不允许访问异常
                    } catch (Throwable e) {
                        // 其他任何异常都捕获
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        PrintStream printStream = new PrintStream(out);
                        e.printStackTrace(printStream);
                        // 字节流的close方法本身就是空方法，没必要执行close操作
                        debugMethod.setValue("调用发生异常：" + out);
                    }
                }
            }
            // 添加至列表
            debugMethodList.add(debugMethod);
        }
        result.put("methodList", debugMethodList);
        // 添加一条分割先
        result.put("----", "-----------------我是一条华丽的分割线-----------------");
        // 字段列表
        List<Field> fieldList = getAllFieldByClass(cls);
        List<DebugField> debugFieldList = new ArrayList<>();
        fieldList.forEach(field -> {
            DebugField debugField = new DebugField();
            debugField.setName(field.getName());
            debugField.setType(field.getType());
            try {
                // 设置允许方法
                field.setAccessible(true);
                Object val = field.get(obj);
                if (val == null) {
                    debugField.setValue(null);
                } else {
                    debugField.setValue(val.toString());
                }
            } catch (IllegalAccessException e) {
                ExceptionUtil.rethrow(e);
            }
            debugFieldList.add(debugField);
        });
        result.put("fieldList", debugFieldList);
        return JSON.toJsonByFormat(result).replace("\r\n", "\n");
    }

    private static final long MAX = 100000000000000000L;

    /**
     * 生成长度为18位的序列号，保持代码美观
     *
     * @return 序列化
     */
    public String serial() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        // 正负号生成
        if (random.nextFloat() > 0.5F) {
            builder.append("-");
        }
        // 首位不能为0
        builder.append(random.nextInt(9) + 1);
        // 生成剩余位数
        do {
            builder.append(random.nextInt(10));
        } while (builder.length() < 18);
        // 加上结束符号
        builder.append("L");
        return builder.toString();
    }

    /**
     * 将json转map
     *
     * @param json json字符串
     * @return map对象
     */
    public Map<?,?> parseJson(String json) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return JSON.parse(json, Map.class);
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * 将对象转json字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    public String toJson(Object obj) {
        return toJson(obj, false);
    }

    /**
     * 将对象转json字符串
     *
     * @param obj 对象
     * @param format 是否格式化json
     * @return json字符串
     */
    public String toJson(Object obj, Boolean format) {
        if (obj == null) {
            return null;
        }
        if (format == null) {
            format = false;
        }
        // 是否格式化输出json
        if (format) {
            return JSON.toJsonByFormat(obj);
        } else {
            return JSON.toJson(obj);
        }
    }

    /**
     * 中文及中文符号正则表达式
     */
    public static final String CHINESE_REGEX = "[\u4e00-\u9fa5–—‘’“”…、。〈〉《》「」『』【】〔〕！（），．：；？]";

    /**
     * 字符串转unicode编码（默认只转换CHINESE_REGEX匹配到的字符）
     * @param str 字符串
     * @return 转码后的字符串
     */
    public String toUnicode(String str) {
        return toUnicode(str, false);
    }

    /**
     * 字符串转unicode编码
     * @param str 字符串
     * @param transAll true转换所有字符，false只转换CHINESE_REGEX匹配到的字符
     * @return 转码后的字符串
     */
    public String toUnicode(String str, Boolean transAll) {
        if (null == str) {
            return null;
        }
        if (str.length() <= 0) {
            return null;
        }
        if (null == transAll) {
            transAll = false;
        }

        StringBuilder sb = new StringBuilder();
        if (transAll) {
            for (char c : str.toCharArray()) {
                sb.append(String.format("\\u%04x", (int) c));
            }
        } else {
            for (char c : str.toCharArray()) {
                // 中文范围
                if (String.valueOf(c).matches(CHINESE_REGEX)) {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    /**
     * 远程调用服务
     *
     * @param name  服务名称
     * @param param 请求参数
     * @return 结果
     */
    public Object service(String name, Object... param) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        // 组装参数
        Map<String, Object> map = Collections.emptyMap();
        if (param != null && param.length > 0) {
            map = new LinkedHashMap<>(param.length);
            for (int i = 0; i < param.length; i++) {
                map.put("param" + i, param[0]);
            }
        }
        // 发起请求
        String result = HttpUtils.postJson(String.format("/service?name=%s", name), map);
        if (result == null) {
            return null;
        }
        try {
            // 处理结果
            JsonNode jsonNode = JSON.readTree(result);
            String type = jsonNode.get("type").asText();
            JsonNode data = jsonNode.get("data");
            Class<?> cls = Class.forName(type);
            // 字符串类型
            if (String.class.equals(cls)) {
                return data.asText();
            }
            // 其他类型
            return JSON.parse(data.toString(), cls);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
