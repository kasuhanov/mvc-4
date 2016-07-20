package su.asgor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import su.asgor.config.db.util.RoutingDataSource;

import java.util.Properties;

@Configuration
@EnableJpaRepositories("su.asgor")
@EnableTransactionManagement
@ComponentScan("su.asgor")
public class DatabaseConfig {

    @Bean(destroyMethod = "close")
    public RoutingDataSource routingDataSource(){
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setLenientFallback(false);
        return routingDataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(routingDataSource());
        em.setPackagesToScan("su.asgor.model");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties hibernateProperties = new Properties();
        hibernateProperties.put("hibernate.dialect","org.hibernate.dialect.PostgreSQL9Dialect");
        hibernateProperties.put("hibernate.show_sql","false");
        hibernateProperties.put("hibernate.enable_lazy_load_no_trans","true");
        //hibernateProperties.put("hibernate.hbm2ddl.auto","update");

        em.setJpaProperties(hibernateProperties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager manager = new JpaTransactionManager();
        manager.setEntityManagerFactory(entityManagerFactory().getObject());
        return manager;
    }
}
