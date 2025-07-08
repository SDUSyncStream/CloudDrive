//package cn.sdu.clouddrive.admin.config; // 根据你的实际包名修改
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy; // 导入SessionCreationPolicy
//
//@Configuration // 标识这是一个配置类
//@EnableWebSecurity // 启用Spring Security的Web安全功能
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                // 禁用CSRF防护
//                // WARNING: 禁用CSRF在生产环境中可能带来安全风险。
//                .csrf().disable()
//
//                // 禁用Spring Security的默认表单登录和HTTP Basic认证
//                .formLogin().disable()
//                .httpBasic().disable()
//
//                // 配置Session管理策略为无状态，适用于RESTful API
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//
//                // 配置请求授权规则
//                .authorizeRequests()
//                // 允许所有请求访问 /admin/login 接口
//                .antMatchers("/admin/login").permitAll()
//                // 允许所有请求访问 /admin/hello 接口
//                .antMatchers("/admin/hello").permitAll()
//                // 允许访问Actuator健康检查端点
//                .antMatchers("/actuator/**").permitAll()
//                // 其他所有请求都需要认证
//                .antMatchers("/admin/**").authenticated()
//                .anyRequest().authenticated();
//    }
//
//    // 通常你会在这里定义一个密码编码器Bean，用于对密码进行哈希和验证
//    // @Bean
//    // public PasswordEncoder passwordEncoder() {
//    //     // return new BCryptPasswordEncoder();
//    // }
//}