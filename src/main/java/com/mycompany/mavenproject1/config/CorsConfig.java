//package com.mycompany.mavenproject1.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
//import java.util.Arrays;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//
//        // Thêm cả localhost và vercel vào danh sách AllowedOrigins
//        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://cinemate-iv2x.vercel.app"));
//
//        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-CSRF-TOKEN"));
//        corsConfig.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
//        corsConfig.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//
//        return new CorsFilter(source);
//    }
//}
