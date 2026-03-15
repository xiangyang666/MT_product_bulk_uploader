import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class VerifyPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        // 数据库中的哈希
        String storedHash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";
        
        // 测试不同的密码
        String[] testPasswords = {"123456", "admin", "admin123", "password", "Admin123"};
        
        System.out.println("测试密码验证：");
        System.out.println("存储的哈希: " + storedHash);
        System.out.println();
        
        for (String password : testPasswords) {
            boolean matches = encoder.matches(password, storedHash);
            System.out.println("密码 '" + password + "' 匹配: " + matches);
        }
        
        System.out.println("\n生成新的123456密码哈希：");
        String newHash = encoder.encode("123456");
        System.out.println("新哈希: " + newHash);
        System.out.println("验证新哈希: " + encoder.matches("123456", newHash));
    }
}
