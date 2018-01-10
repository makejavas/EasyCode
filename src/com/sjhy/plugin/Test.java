package com.sjhy.plugin;

import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        System.out.println("varchar(20)".matches("varchar(\\(\\d+\\))?"));
    }
}
