package uk.gov.cshr.vcm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${spring.security.search_username}")
    private String searchUsername;

    @Value("${spring.security.search_password}")
    private String searchPassword;

    @Value("${spring.security.crud_username}")
    private String crudUsername;

    @Value("${spring.security.crud_password}")
    private String crudPassword;

    @Autowired
	private AuthenticationEntryPoint authEntryPoint;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.csrf().disable()
            .authorizeRequests().antMatchers("/vacancy/search/**", "/department/", "/department").hasRole("SEARCH_ROLE")
            .and()
        	.authorizeRequests().antMatchers("/department/**", "/vacancy/**").hasRole("CRUD_ROLE")
            .and()
            .httpBasic().authenticationEntryPoint(authEntryPoint);
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

		auth.inMemoryAuthentication()
                .withUser(crudUsername)
                .password(crudPassword)
                .roles("CRUD_ROLE");

        auth.inMemoryAuthentication()
                .withUser(searchUsername)
                .password(searchPassword)
                .roles("SEARCH_ROLE");
	}
}
