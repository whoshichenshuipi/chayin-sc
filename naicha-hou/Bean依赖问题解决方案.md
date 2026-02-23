# Bean依赖问题解决方案

## 问题描述
```
Parameter 0 of constructor in com.naicha.hou.service.impl.AuthServiceImpl required a bean of type 'com.naicha.hou.mapper.UserMapper' that could not be found.
```

## 问题分析
1. `AuthServiceImpl`需要`UserMapper`
2. 但`UserMapper`需要MyBatis配置
3. 我们排除了数据库自动配置
4. 所以`UserMapper`无法被创建

## 解决方案

### 方案1：使用纯测试控制器（推荐）

#### 步骤1：运行纯测试启动
```bash
start-pure.bat
```

#### 步骤2：测试纯功能
```bash
# 健康检查
curl http://localhost:8081/pure/health

# 系统信息
curl http://localhost:8081/pure/info

# 登录测试
curl -X POST http://localhost:8081/pure/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123123123","userType":"admin"}'

# 注册测试
curl -X POST http://localhost:8081/pure/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","userType":"user"}'

# 用户列表
curl http://localhost:8081/pure/users

# 商品列表
curl http://localhost:8081/pure/products
```

#### 步骤3：如果成功，逐步添加数据库支持
1. 添加数据库配置
2. 添加MyBatis配置
3. 添加Service支持

### 方案2：添加数据库配置

#### 步骤1：创建数据库配置类
```java
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
            .getResources("classpath:mapper/**/*.xml"));
        return sessionFactory.getObject();
    }
    
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer scannerConfigurer = new MapperScannerConfigurer();
        scannerConfigurer.setBasePackage("com.naicha.hou.mapper");
        return scannerConfigurer;
    }
}
```

#### 步骤2：更新启动类
```java
@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class
})
@MapperScan("com.naicha.hou.mapper")
@EnableTransactionManagement
public class NaichaHouApplication {
    // ...
}
```

### 方案3：使用JPA替代MyBatis

#### 步骤1：添加JPA依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

#### 步骤2：创建JPA实体
```java
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username")
    private String username;
    
    @Column(name = "password")
    private String password;
    
    // 其他字段...
}
```

#### 步骤3：创建Repository
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
```

#### 步骤4：更新Service
```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserInfoResponse login(LoginRequest loginRequest) {
        // 使用JPA Repository实现
    }
}
```

## 测试步骤

### 1. 纯功能测试
```bash
# 启动纯测试
start-pure.bat

# 测试所有接口
curl http://localhost:8081/pure/health
curl http://localhost:8081/pure/info
curl http://localhost:8081/pure/users
curl http://localhost:8081/pure/products
```

### 2. 功能测试
```bash
# 测试登录
curl -X POST http://localhost:8081/pure/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123123123","userType":"admin"}'

# 测试注册
curl -X POST http://localhost:8081/pure/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","userType":"user"}'
```

### 3. 完整功能测试
```bash
# 如果纯功能测试成功，可以尝试添加数据库支持
# 或者使用JPA替代MyBatis
```

## 常见问题

### 1. Bean无法创建
- 检查依赖注入
- 检查配置类
- 检查包扫描

### 2. 数据库连接失败
- 检查数据库配置
- 检查数据库服务
- 使用内存数据库测试

### 3. 编译失败
- 检查依赖版本
- 检查Java版本
- 清理项目重新编译

## 推荐步骤

1. **首选方案**：运行 `start-pure.bat` 测试纯功能
2. **如果成功**：逐步添加数据库支持
3. **如果失败**：使用JPA替代MyBatis
4. **最后方案**：使用原生MyBatis

## 联系支持

如果问题仍然无法解决，请提供：
1. 完整的错误堆栈
2. 项目结构信息
3. 依赖版本信息
4. 测试结果
