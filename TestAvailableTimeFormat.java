public class TestAvailableTimeFormat {
    public static void main(String[] args) {
        String[] testCases = {
            "全天 09:00-22:00",
            "全天09:00-22:00",
            "09:00-22:00 全天",
            "全天  09:00-22:00",  // 多个空格
            "09:00-22:00"
        };
        
        for (String test : testCases) {
            String result = formatAvailableTime(test);
            System.out.println("输入: \"" + test + "\"");
            System.out.println("输出: \"" + result + "\"");
            System.out.println("---");
        }
    }
    
    public static String formatAvailableTime(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "全天";
        }
        
        time = time.trim();
        
        // 如果明确设置了"全天"，直接使用
        if ("全天".equals(time)) {
            return "全天";
        }
        
        // 验证严格的双位数字时间格式
        if (time.matches("^(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])-(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])$")) {
            return time;
        }
        
        // 对于其他格式，保留原始值
        return time;
    }
}
