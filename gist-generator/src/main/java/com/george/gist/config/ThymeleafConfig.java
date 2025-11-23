package com.george.gist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class ThymeleafConfig {
    @Bean
    @Primary
    public SpringResourceTemplateResolver templateResolver() {
        var tr = new SpringResourceTemplateResolver();
        tr.setPrefix("classpath:/templates/");
        tr.setSuffix(".html");
        tr.setTemplateMode(TemplateMode.HTML);
        tr.setCharacterEncoding("UTF-8");
        tr.setCacheable(false);
        return tr;
    }

    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver tr) {
        var te = new SpringTemplateEngine();
        te.setTemplateResolver(tr);
        return te;
    }
}
