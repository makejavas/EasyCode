package com.sjhy.plugin.tool;

import com.intellij.util.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Created by cgb
 */
public class FindRelevancyBeanFileVisitor extends SimpleFileVisitor<Path> {
    /**
     * 文件路径
     */
    Map<String, String> rbNameAndPathMap = new HashMap<>();
    /**
     * 文件截断的packet 兼容旧模板
     * 保存的是:com.sjhy.plugin
     */
    Map<String, String> rbNameAndCutOffPackageMap = new HashMap<>();
    /**
     * 文件全packet
     * 保存的是:package com.sjhy.plugin.tool;
     */
    Map<String, String> rbNameAndPackageMap = new HashMap<>();
    /**
     * 文件名称
     */
    Map<String, String> rbNameAndFileNameMap = new HashMap<>();
    String referenceBeanName;

    NameUtils nameUtils = NameUtils.getInstance();

    public FindRelevancyBeanFileVisitor(String referenceBeanName) {
        // 转换为类名, 兼容下划线和首字母小写
        if (referenceBeanName.contains("_")) {
            this.referenceBeanName = nameUtils.getClassName(referenceBeanName);
        } else {
            this.referenceBeanName = referenceBeanName;
        }

        this.referenceBeanName = nameUtils.firstUpperCase(this.referenceBeanName);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String fileSourceName = file.getFileName().toString();

        // TODO 后续可以配置忽略的文件和文件夹
        if (fileSourceName.contains(".class")) {
            return FileVisitResult.SKIP_SUBTREE;
        }

        if (fileSourceName.contains(referenceBeanName)) {
            // 去掉类名中的bean名称. 获取模板名称并小写 DefaultBeanDto.java => DefaultDto.java => defaultDto.java
            String templateName = nameUtils.firstLowerCase(fileSourceName.replace(referenceBeanName, ""));

            rbNameAndPathMap.put(templateName, file.getParent().toString());
            rbNameAndPackageMap.put(templateName, readPackageLine(file));
            rbNameAndCutOffPackageMap.put(templateName, cutOffPackage(rbNameAndPackageMap.get(templateName)));
            rbNameAndFileNameMap.put(templateName, fileSourceName);
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    /**
     * 读取文件的第一行
     *
     * @param file 文件
     * @return 第一行
     */
    private String readPackageLine(Path file) {
        try (Stream<String> lines = Files.lines(file)) {
            Optional<String> first = lines
                    .filter(StringUtils::isNotBlank)
                    .findFirst();

            return first.orElse(StringUtils.EMPTY);

        } catch (IOException e) {
            e.printStackTrace();
            ExceptionUtil.rethrow(e);
        }

        return StringUtils.EMPTY;
    }

    /**
     * 截断package
     * 兼容以前的模板
     *
     * @param packageStr 完整package(package com.sjhy.plugin.tool;)
     * @return 截断的package(com.sjhy.plugin)
     */
    private String cutOffPackage(String packageStr) {
        // impl的要多截取一次
        if (packageStr.endsWith(".impl;")) {
            packageStr = packageStr.replace(".impl;", "").trim();
        }

        int lastIndex = packageStr.lastIndexOf(".");
        if (lastIndex != -1) {
            packageStr = packageStr.substring(0, lastIndex);
        }
        return packageStr.replace("package", "").trim();
    }

    public boolean isExist(String name) {
        return rbNameAndPathMap.containsKey(name);
    }


    public String getPath(String name) {
        return rbNameAndPathMap.get(name);
    }

    public String getCutOffPackage(String name) {
        return rbNameAndCutOffPackageMap.get(name);
    }

    public String getPackage(String name) {
        return rbNameAndPackageMap.get(name);
    }

    public String getFileName(String name) {
        return rbNameAndFileNameMap.get(name);
    }

    public String getReferenceBeanName() {
        return referenceBeanName;
    }
}
