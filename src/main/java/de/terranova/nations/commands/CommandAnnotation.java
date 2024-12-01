package de.terranova.nations.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandAnnotation {
    String domain(); // Command name (e.g., "region.create")

    String permission() default ""; // Required permission

    String description() default ""; // Command description

    String usage() default ""; // Usage information

    String[] tabCompletion() default {}; // Tab completion options (including dynamic placeholders)
}