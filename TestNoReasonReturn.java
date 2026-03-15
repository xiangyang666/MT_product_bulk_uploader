import java.util.*;

public class TestNoReasonReturn {

    /**
     * 当前的实现（有问题的版本）
     */
    public static String parseNoReasonReturnTagIdOld(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "1300030895";
        }

        String trimmedValue = value.trim();

        if (trimmedValue.matches("^13\\d{8}$")) {
            return trimmedValue;
        }

        // 1300030895 - 不支持7天无理由退货
        if (trimmedValue.equals("不支持7天无理由退货") ||
            trimmedValue.equals("0") ||
            trimmedValue.equalsIgnoreCase("false") ||
            trimmedValue.equals("否")) {
            return "1300030895";
        }

        // 1300030902 - 7天无理由退货（一次性包装破损不支持）
        if (trimmedValue.equals("7天无理由退货（一次性包装破损不支持）") ||
            trimmedValue.equals("7天无理由退货(一次性包装破损不支持)") ||
            trimmedValue.contains("一次性包装破损")) {
            return "1300030902";
        }

        // 1300030901 - 7天无理由退货
        if (trimmedValue.equals("7天无理由退货") ||
            trimmedValue.equals("七天无理由退货") ||
            trimmedValue.equals("1") ||
            trimmedValue.equalsIgnoreCase("true") ||
            trimmedValue.equals("是")) {
            return "1300030901";
        }

        return "1300030895";
    }

    /**
     * 修复后的版本
     */
    public static String parseNoReasonReturnTagIdNew(String value) {
        if (value == null || value.trim().isEmpty()) {
            System.out.println("输入为空，返回默认值: 1300030895");
            return "1300030895";
        }

        String trimmedValue = value.trim();
        System.out.println("输入值: [" + trimmedValue + "], 长度: " + trimmedValue.length());

        // 打印每个字符的Unicode值
        System.out.print("字符详情: ");
        for (int i = 0; i < trimmedValue.length(); i++) {
            char c = trimmedValue.charAt(i);
            System.out.print(String.format("[%c U+%04X] ", c, (int)c));
        }
        System.out.println();

        // 如果已经是标签ID格式（13开头的10位数字），直接返回
        if (trimmedValue.matches("^13\\d{8}$")) {
            System.out.println("匹配到标签ID格式: " + trimmedValue);
            return trimmedValue;
        }

        // 优先匹配更具体的标签（必须先匹配，否则会被通用标签覆盖）

        // 1300030902 - 7天无理由退货（一次性包装破损不支持）
        if (trimmedValue.contains("一次性包装破损") && trimmedValue.contains("不支持")) {
            System.out.println("匹配到: 7天无理由退货（一次性包装破损不支持）-> 1300030902");
            return "1300030902";
        }

        // 1300030903 - 7天无理由退货（激活后不支持）
        if (trimmedValue.contains("激活后") && trimmedValue.contains("不支持")) {
            System.out.println("匹配到: 7天无理由退货（激活后不支持）-> 1300030903");
            return "1300030903";
        }

        // 1300030904 - 7天无理由退货（使用后不支持）
        if (trimmedValue.contains("使用后") && trimmedValue.contains("不支持")) {
            System.out.println("匹配到: 7天无理由退货（使用后不支持）-> 1300030904");
            return "1300030904";
        }

        // 1300030905 - 7天无理由退货（安装后不支持）
        if (trimmedValue.contains("安装后") && trimmedValue.contains("不支持")) {
            System.out.println("匹配到: 7天无理由退货（安装后不支持）-> 1300030905");
            return "1300030905";
        }

        // 1300030906 - 7天无理由退货（定制类不支持）
        if (trimmedValue.contains("定制类") && trimmedValue.contains("不支持")) {
            System.out.println("匹配到: 7天无理由退货（定制类不支持）-> 1300030906");
            return "1300030906";
        }

        // 1300030901 - 7天无理由退货（通用）
        if (trimmedValue.contains("7天无理由退货") || trimmedValue.contains("七天无理由退货")) {
            System.out.println("匹配到: 7天无理由退货 -> 1300030901");
            return "1300030901";
        }

        // 1300030895 - 不支持7天无理由退货
        if (trimmedValue.equals("不支持7天无理由退货") ||
            trimmedValue.equals("0") ||
            trimmedValue.equalsIgnoreCase("false") ||
            trimmedValue.equals("否")) {
            System.out.println("匹配到: 不支持7天无理由退货 -> 1300030895");
            return "1300030895";
        }

        // 布尔值转换为通用标签
        if (trimmedValue.equals("1") ||
            trimmedValue.equalsIgnoreCase("true") ||
            trimmedValue.equals("是")) {
            System.out.println("匹配到: 布尔值true -> 1300030901");
            return "1300030901";
        }

        System.out.println("未匹配到任何标签，返回默认值: 1300030895");
        return "1300030895";
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("测试用例: 7天无理由退货（一次性包装破损不支持）");
        System.out.println("========================================\n");

        String testValue = "7天无理由退货（一次性包装破损不支持）";

        System.out.println("【旧版本】");
        String oldResult = parseNoReasonReturnTagIdOld(testValue);
        System.out.println("结果: " + oldResult);
        System.out.println("预期: 1300030902");
        System.out.println("测试: " + (oldResult.equals("1300030902") ? "✓ 通过" : "✗ 失败"));
        System.out.println();

        System.out.println("【新版本】");
        String newResult = parseNoReasonReturnTagIdNew(testValue);
        System.out.println("结果: " + newResult);
        System.out.println("预期: 1300030902");
        System.out.println("测试: " + (newResult.equals("1300030902") ? "✓ 通过" : "✗ 失败"));
    }
}
