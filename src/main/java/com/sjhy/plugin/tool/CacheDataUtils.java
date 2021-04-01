package com.sjhy.plugin.tool;

import com.intellij.database.psi.DbTable;
import com.intellij.psi.PsiClass;
import lombok.Data;

import java.util.List;

/**
 * 缓存数据工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
@Data
public class CacheDataUtils {
    private volatile static CacheDataUtils cacheDataUtils;

    /**
     * 单例模式
     */
    public static CacheDataUtils getInstance() {
        if (cacheDataUtils == null) {
            synchronized (CacheDataUtils.class) {
                if (cacheDataUtils == null) {
                    cacheDataUtils = new CacheDataUtils();
                }
            }
        }
        return cacheDataUtils;
    }

    private CacheDataUtils() {
    }

    /**
     * 当前选中的表
     */
    private DbTable selectDbTable;
    /**
     * 所有选中的表
     */
    private List<DbTable> dbTableList;

    /**
     * 选中的类
     */
    private PsiClass selectPsiClass;

    /**
     * 所有选中的表
     */
    private List<PsiClass> psiClassList;
}
