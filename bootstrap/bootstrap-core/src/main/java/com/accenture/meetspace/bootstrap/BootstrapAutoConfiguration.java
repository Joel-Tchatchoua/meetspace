package com.accenture.meetspace.bootstrap;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.accenture.meetspace.bootstrap.infrastructure")
public class BootstrapAutoConfiguration {
}
