package co.edu.usco.pw.tienda_cerca.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class BaseDatosConfig {

	@Bean
	@Primary
	@ConfigurationProperties("spring.datasource")
	public DataSourceProperties postgresProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	public DataSource dataSource(@Qualifier("postgresProperties") DataSourceProperties props) {
		return props.initializeDataSourceBuilder().build();
	}

	@Bean
	@Primary
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	@ConfigurationProperties("app.mysql")
	public DataSourceProperties mysqlProperties() {
		return new DataSourceProperties();
	}

	@Bean
	public DataSource mysqlDataSource(@Qualifier("mysqlProperties") DataSourceProperties props) {
		return props.initializeDataSourceBuilder().build();
	}

	@Bean
	public JdbcTemplate mysqlJdbc(@Qualifier("mysqlDataSource") DataSource mysql) {
		return new JdbcTemplate(mysql);
	}
}
