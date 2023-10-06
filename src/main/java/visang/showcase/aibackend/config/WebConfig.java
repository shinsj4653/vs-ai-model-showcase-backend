package visang.showcase.aibackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://13.209.224.148", "http://localhost:8004"
                        , "http://localhost", "https://10.216.2.158:8004"
                , "http://121.157.55.33:8004") //로컬, 개발
                .allowedHeaders("*")
                .allowedMethods("*") // 허용할 HTTP 메서드
                .allowCredentials(true) // 쿠키 허용
                .maxAge(3600); // preflight(사전 요청)의 캐시 시간 설정
    }
}
