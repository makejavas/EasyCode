package com.sjhy.plugin.tool;

import org.junit.Assert;
import org.junit.Test;

/**
 * GlobalTool相关测试
 **/
public class GlobalToolTest {

    @Test
    public void hump2Underline() {
        GlobalTool tool = GlobalTool.getInstance();
        String out;
        //正常
        System.out.println(out = tool.hump2Underline("asianInfrastructureInvestmentBank"));
        Assert.assertEquals("asian_infrastructure_investment_bank", out);
        //首字母大写
        System.out.println(out = tool.hump2Underline("AsianInfrastructureInvestmentBank"));
        Assert.assertEquals("asian_infrastructure_investment_bank", out);
        //已经是下划线字符串
        System.out.println(out = tool.hump2Underline("asian_infrastructure_investment_bank"));
        Assert.assertEquals("asian_infrastructure_investment_bank", out);
        //包含重复的大写字符
        System.out.println(out = tool.hump2Underline("AAsianIInfrastructureInvestmentBank"));
        Assert.assertEquals("aasian_iinfrastructure_investment_bank", out);
        //空字符串
        System.out.println(out = tool.hump2Underline(""));
        Assert.assertEquals("", out);
        //null
        System.out.println(out = tool.hump2Underline(null));
        Assert.assertEquals(null, out);
        //全大写字符串
        System.out.println(out = tool.hump2Underline("AASIANINFRASTRUCTUREINVESTMENTBANK"));
        Assert.assertEquals("aasianinfrastructureinvestmentbank", out);
    }

}
