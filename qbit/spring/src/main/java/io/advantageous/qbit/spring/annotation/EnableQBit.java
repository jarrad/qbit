package io.advantageous.qbit.spring.annotation;

import io.advantageous.qbit.spring.config.PlatformConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable the QBit platform and default queue.
 *
 * @author richardhightower@gmail.com (Rick Hightower)
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({PlatformConfiguration.class})
public @interface EnableQBit {
}