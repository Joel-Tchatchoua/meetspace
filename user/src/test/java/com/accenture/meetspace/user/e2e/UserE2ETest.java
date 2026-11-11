package com.accenture.meetspace.user.e2e;

import com.accenture.meetspace.bootstrap.test.e2e.E2ETest;
import com.accenture.meetspace.bootstrap.test.security.TestJwtConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@E2ETest
@Import({TestJwtConfiguration.class, UserE2EConfiguration.class})
public @interface UserE2ETest {
}
