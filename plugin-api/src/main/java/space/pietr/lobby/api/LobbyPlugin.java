package space.pietr.lobby.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LobbyPlugin {

    String id();

    String name();

    String version() default "1.0.0";

    String[] authors() default {};
}
