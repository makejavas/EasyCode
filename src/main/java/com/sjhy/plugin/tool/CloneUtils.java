package com.sjhy.plugin.tool;

import com.intellij.util.ExceptionUtil;

import java.io.*;

/**
 * 克隆工具类，实现原理通过JSON序列化反序列化实现
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public final class CloneUtils {
    /**
     * 禁用构造方法
     */
    private CloneUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 实体对象克隆方法
     *
     * @param entity 实体对象
     * @return 克隆后的实体对象
     */
    @SuppressWarnings("unchecked")
    public static <E> E clone(E entity) {
        if (entity == null) {
            return null;
        }
        // 定义一个缓冲输出流对象
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectInputStream input = null;
        try(ObjectOutputStream out = new ObjectOutputStream(buffer)) {
            // 将对象输出到缓冲区
            out.writeObject(entity);
            // 重新从缓冲区读取对象
            input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            return (E) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            ExceptionUtil.rethrow(e);
        } finally {
            // 关闭流
            try {
                buffer.close();
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
