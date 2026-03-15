@echo off
echo 正在生成BCrypt密码哈希...
cd meituan-backend
javac -cp "target/classes;%USERPROFILE%\.m2\repository\org\springframework\security\spring-security-crypto\6.1.5\spring-security-crypto-6.1.5.jar" ..\GenerateBCryptHash.java
java -cp ".;target/classes;%USERPROFILE%\.m2\repository\org\springframework\security\spring-security-crypto\6.1.5\spring-security-crypto-6.1.5.jar;%USERPROFILE%\.m2\repository\org\springframework\spring-core\6.0.13\spring-core-6.0.13.jar;%USERPROFILE%\.m2\repository\org\springframework\spring-jcl\6.0.13\spring-jcl-6.0.13.jar" GenerateBCryptHash
cd ..
pause
