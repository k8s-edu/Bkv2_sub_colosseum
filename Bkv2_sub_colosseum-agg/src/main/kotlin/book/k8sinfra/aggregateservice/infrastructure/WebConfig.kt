package book.k8sinfra.aggregateservice.infrastructure

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig: WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowedOrigins("*")
            .allowedMethods("GET", "POST","PUT", "DELETE")
            .allowedHeaders("Content-Type", "Authorization")
            .exposedHeaders("X-aggregate-service")
            .maxAge(3600L)
    }
}