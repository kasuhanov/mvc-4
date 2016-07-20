package su.asgor.config.db.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    DataSourceProvider dataSourceProvider;
    private static String ds;

    public static void setDs(String ds) {
        if(ds == null){
            throw new NullPointerException();
        }
        RoutingDataSource.ds = ds;
    }

    public static String getDs() {
        return RoutingDataSource.ds;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        setDataSources(dataSourceProvider);
        return RoutingDataSource.getDs();
    }

    @Autowired
    public void setDataSources(DataSourceProvider dataSourceProvider) {
        setTargetDataSources(Collections.singletonMap((Object)"ds",(Object)dataSourceProvider.getDataSource()));
    }
    @Override
    protected DataSource determineTargetDataSource() {
        return dataSourceProvider.getDataSource();
    }

    public void setDataSource( String ip, String port, String dbname, String username, String password){
        dataSourceProvider.setIp(ip);
        dataSourceProvider.setPort(port);
        dataSourceProvider.setDbname(dbname);
        dataSourceProvider.setUsername(username);
        dataSourceProvider.setPassword(password);
        dataSourceProvider.close(dataSourceProvider.getDataSource());
        dataSourceProvider.setDataSource(ip, port, dbname, username, password);
    }

    public void updateSchema(){
    	/*
	        DataSource dataSource = dataSourceProvider.getDataSource();
	        LocalSessionFactoryBuilder sessionFactory = new LocalSessionFactoryBuilder(dataSource);
	        sessionFactory.scanPackages("su");
	        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	        try{
	            List<SchemaUpdateScript> scripts = sessionFactory.generateSchemaUpdateScriptList(new PostgreSQL9Dialect(),
	                    new DatabaseMetadata(dataSource.getConnection(), new PostgreSQL9Dialect(), sessionFactory));
	            log.info("Schema update scripts["+scripts.size()+"]");
	            for (SchemaUpdateScript script:scripts ) {
	                log.info(script.getScript());
	                jdbcTemplate.execute(script.getScript());
	            }
	        }catch (Exception e){
	            log.error("error updating schema",e);
	        }
	    */    
    	log.error("error updating schema");
    }

    public void updateSchema(String ip, String port, String dbname, String username, String password) throws SQLException {
    	log.error("error updating schema");
    	/*
	        DataSource dataSource = dataSourceProvider.dataSource(ip,port,dbname,username,password);
	        LocalSessionFactoryBuilder sessionFactory = new LocalSessionFactoryBuilder(dataSource);
	        sessionFactory.scanPackages("su");
	        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	        List<SchemaUpdateScript> scripts = sessionFactory.generateSchemaUpdateScriptList(new PostgreSQL9Dialect(),
	                    new DatabaseMetadata(dataSource.getConnection(), new PostgreSQL9Dialect(), sessionFactory));
	        log.info("Schema update scripts["+scripts.size()+"]");
	        for (SchemaUpdateScript script:scripts ) {
	            log.info(script.getScript());
	            jdbcTemplate.execute(script.getScript());
        }
        */
    }

    public void close(){
        dataSourceProvider.close(dataSourceProvider.getDataSource());
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
               log.error("Error destroy driver ",e);
            }

        }
    }
}