package net.echo.solar.checks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CheckData {

    String name();

    String description();

    int flagDelay() default 1;
}
