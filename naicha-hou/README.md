# 奶茶小程序后端服务

## 项目简介

奶茶小程序后端服务，基于Spring Boot 3.2.0开发，提供完整的奶茶小程序后端API服务。

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.2.0
- **MyBatis Plus**: 3.5.4.1
- **MySQL**: 8.0.33
- **Redis**: 6.0+
- **Lombok**: 1.18.30
- **Knife4j**: 4.3.0 (API文档)
- **Hutool**: 5.8.22 (工具类)
- **JWT**: 0.11.5 (认证)
- **FastJSON**: 2.0.43 (JSON处理)

## 项目结构

```
naicha-hou/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/naicha/hou/
│   │   │       ├── NaichaHouApplication.java    # 启动类
│   │   │       ├── config/                     # 配置类
│   │   │       ├── controller/                  # 控制器
│   │   │       ├── service/                    # 服务层
│   │   │       ├── mapper/                     # 数据访问层
│   │   │       ├── entity/                    # 实体类
│   │   │       ├── dto/                        # 数据传输对象
│   │   │       ├── vo/                         # 视图对象
│   │   │       ├── common/                     # 公共类
│   │   │       ├── utils/                      # 工具类
│   │   │       ├── exception/                  # 异常处理
│   │   │       └── aspect/                     # 切面
│   │   └── resources/
│   │       ├── application.yml                 # 主配置文件
│   │       ├── application-dev.yml             # 开发环境配置
│   │       ├── application-prod.yml            # 生产环境配置
│   │       └── mapper/                         # MyBatis映射文件
│   └── test/                                   # 测试代码
├── pom.xml                                     # Maven配置
└── README.md                                   # 项目说明
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE naicha_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改配置文件中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/naicha_db?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: 123456
```

### 启动项目

1. 克隆项目到本地
2. 修改配置文件中的数据库和Redis连接信息
3. 执行以下命令启动项目：

```bash
# 开发环境启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者直接运行主类
mvn clean compile
java -jar target/naicha-hou-1.0.0.jar --spring.profiles.active=dev
```

### 访问地址

- 应用地址: http://localhost:8080/api
- API文档: http://localhost:8080/api/doc.html
- 健康检查: http://localhost:8080/api/test/health

## 功能模块

### 已实现功能

- ✅ 项目基础架构搭建
- ✅ 统一返回结果封装
- ✅ 全局异常处理
- ✅ 跨域配置
- ✅ MyBatis Plus配置
- ✅ Knife4j API文档
- ✅ 测试接口

### 待实现功能

- 🔲 用户管理模块
- 🔲 商品管理模块
- 🔲 订单管理模块
- 🔲 支付模块
- 🔲 消息通知模块
- 🔲 帮助中心模块
- 🔲 文件上传模块
- 🔲 短信服务模块
- 🔲 邮件服务模块

## 开发规范

### 代码规范

1. 使用Lombok减少样板代码
2. 统一使用Result封装返回结果
3. 使用BusinessException处理业务异常
4. 遵循RESTful API设计规范
5. 使用Swagger注解完善API文档

### 包命名规范

- `controller`: 控制器层
- `service`: 服务层
- `mapper`: 数据访问层
- `entity`: 实体类
- `dto`: 数据传输对象
- `vo`: 视图对象
- `config`: 配置类
- `common`: 公共类
- `utils`: 工具类
- `exception`: 异常类
- `aspect`: 切面类

## 配置说明

### 环境配置

- `dev`: 开发环境
- `prod`: 生产环境

### 主要配置项

- 数据库连接配置
- Redis连接配置
- 邮件服务配置
- 文件上传配置
- JWT配置
- 日志配置

## 部署说明

### Docker部署

```dockerfile
FROM openjdk:17-jre-slim
COPY target/naicha-hou-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 传统部署

1. 打包项目：`mvn clean package`
2. 上传jar包到服务器
3. 启动应用：`java -jar naicha-hou-1.0.0.jar --spring.profiles.active=prod`

## 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交代码
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License

## 联系方式

- 作者: naicha
- 邮箱: naicha@example.com
- 项目地址: https://github.com/naicha/naicha-hou
