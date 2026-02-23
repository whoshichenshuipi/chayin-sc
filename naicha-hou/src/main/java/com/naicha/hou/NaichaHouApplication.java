package com.naicha.hou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;

/**
 * 奶茶小程序后端服务启动类
 *
 * @author naicha
 * @since 2023-12-01
 */
@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class
})
@MapperScan("com.naicha.hou.mapper")
public class NaichaHouApplication {

    /**
     * MyBatis Plus 分页插件配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    public static void main(String[] args) {
        SpringApplication.run(NaichaHouApplication.class, args);
        System.out.println("""
                
                ========================================
                奶茶小程序后端服务启动成功！
                访问地址: http://localhost:8081
                API文档: http://localhost:8081/doc.html
                ========================================
                """);
    }
}
