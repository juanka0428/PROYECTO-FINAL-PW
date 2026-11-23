package co.edu.usco.pw.tienda_cerca.config;

import java.time.Duration;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class I18nConfig implements WebMvcConfigurer {

	public static final String[] IDIOMAS = { "es", "en", "pt" };

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource mensajes = new ReloadableResourceBundleMessageSource();
		mensajes.setBasename("classpath:i18n/messages");
		mensajes.setDefaultEncoding("UTF-8");
		mensajes.setFallbackToSystemLocale(false);
		mensajes.setDefaultLocale(Locale.forLanguageTag("es"));
		return mensajes;
	}

	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver resolver = new CookieLocaleResolver("idioma");
		resolver.setDefaultLocale(Locale.forLanguageTag("es"));
		resolver.setCookieMaxAge(Duration.ofDays(365));
		return resolver;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
		interceptor.setParamName("lang");
		interceptor.setIgnoreInvalidLocale(true);
		return interceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}
}
