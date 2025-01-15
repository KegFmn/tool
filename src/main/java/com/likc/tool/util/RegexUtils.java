package com.likc.tool.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    /**
     * 检查字符串是否符合给定的正则表达式。
     *
     * @param input      要检查的字符串
     * @param regex      正则表达式
     * @return 如果字符串符合正则表达式返回true，否则返回false
     */
    public static boolean matchesRegex(String input, String regex) {
        if (input == null || regex == null) {
            return false;
        }

        return input.matches(regex);
    }

    /**
     * 检查字符串是否包含连续的'-'。
     *
     * @param input 要检查的字符串
     * @return 如果字符串包含连续的'-'返回true，否则返回false
     */
    public static boolean doesContainConsecutiveDashes(String input) {
        // 正则表达式匹配连续的'-'，例如 '--'
        return matchesRegex(input, ".*-{2,}.*");
    }

    /**
     * 检查字符串是否只包含字母和数字。
     *
     * @param input 要检查的字符串
     * @return 如果字符串只包含字母和数字返回true，否则返回false
     */
    public static boolean isAlphanumeric(String input) {
        // 正则表达式匹配只包含字母和数字的字符串
        return matchesRegex(input, "^[a-zA-Z0-9]+$");
    }

    /**
     * 检查字符串是否是一个有效的电子邮件地址。
     *
     * @param input 要检查的字符串
     * @return 如果字符串是一个有效的电子邮件地址返回true，否则返回false
     */
    public static boolean isValidEmail(String input) {
        // 正则表达式匹配有效的电子邮件地址
        return matchesRegex(input, "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    /**
     * 检查字符串是否是一个有效的百分比。
     *
     * @param input 要检查的字符串
     * @return 如果字符串是一个有效的百分比返回true，否则返回false
     */
    public static boolean isPercentage(String input) {
        // 正则表达式匹配有效的电子邮件地址
        return matchesRegex(input, "^[0-9.%]*$");
    }


    public static String extractNumberBeforePercent(String percentStr) {
        String regex = "^(.*?)%";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(percentStr);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
