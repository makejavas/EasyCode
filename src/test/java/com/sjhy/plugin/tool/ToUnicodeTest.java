package com.sjhy.plugin.tool;

import org.junit.Assert;
import org.junit.Test;

/**
 * ToUnicodeTest 类
 *
 * @author czb 2019/11/11 9:43
 * @version 1.0
 **/
public class ToUnicodeTest {

    @Test
    public void toUnicode() {
        GlobalTool tool = GlobalTool.getInstance();
        String out;
        System.out.println(out = tool.toUnicode("金融机构，Finance organization{0},！"));
        Assert.assertEquals("\\u91d1\\u878d\\u673a\\u6784\\uff0cFinance organization{0},\\uff01", out);
    }

}
