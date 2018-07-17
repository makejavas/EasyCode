package com.sjhy.plugin.tool;

import com.sjhy.plugin.comm.CommClone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloneUtils {
    //单例模式
    private static class Instance {
        private static final CloneUtils ME = new CloneUtils();
    }
    public static CloneUtils getInstance() {
        return Instance.ME;
    }
    private CloneUtils(){}

    /**
     * List集合克隆方法
     * @param src 源数据
     * @param <E> 继承克隆父类
     * @return 克隆结果
     */
    public <E extends CommClone<E>> List<E> cloneList(List<E> src) {
        if (src==null) {
            return null;
        }
        List<E> result = new ArrayList<>(src.size());
        src.forEach(e -> result.add(e.clone()));
        return result;
    }

    public <K,E extends CommClone<E>> Map<K, E> cloneMap(Map<K, E> src) {
        if (src==null) {
            return null;
        }
        Map<K, E> result = new HashMap<>();
        src.forEach((k, e) -> result.put(k, e.clone()));
        return result;
    }
}
