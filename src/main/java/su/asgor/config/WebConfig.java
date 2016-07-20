package su.asgor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import su.asgor.config.gson.AnnotationExclusionStrategy;

import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan("su.asgor")
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations(
                        "classpath:/META-INF/resources/webjars/");
    }
    /*private Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }
    
    private MappingJackson2HttpMessageConverter jacksonMessageConverter(){
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .modulesToInstall(hibernate5Module());
        ObjectMapper om = builder.build();
        om.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return new MappingJackson2HttpMessageConverter(om);
  }*/

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    	GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
    	Gson gson = new GsonBuilder()
    			.setExclusionStrategies(new AnnotationExclusionStrategy())
    			//.excludeFieldsWithoutExposeAnnotation()
    			.setPrettyPrinting()
    			.create(); 
    	gsonHttpMessageConverter.setGson(gson);
        converters.add(gsonHttpMessageConverter);
        super.configureMessageConverters(converters);
    }
    @Bean
    public InternalResourceViewResolver defaultViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("");
        resolver.setSuffix("");
        return resolver;
    }
}
