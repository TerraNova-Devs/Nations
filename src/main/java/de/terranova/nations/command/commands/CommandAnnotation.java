package de.terranova.nations.command.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as command handlers within the Terranova library. This annotation
 * provides metadata about the command, such as its domain, required permissions, description, usage
 * instructions, and aliases.
 *
 * <p><b>Usage Example:</b>
 *
 * <pre>{@code
 * public class SpecificSubCommand {
 *
 *     @CommandAnnotation(
 *         domain = "sub.command.$<name>",
 *         permission = "sub.command.permission",
 *         description = "Executes a sub command, auto generates help & tabCompletion.",
 *         usage = "/sub command <name>",
 *     )
 *     public void subCommandName(CommandSender sender, String[] args) {
 *         // Command implementation
 *     }
 * }
 * }</pre>
 *
 * <p>This annotation can be processed at runtime to automatically register and handle commands
 * based on the provided metadata.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandAnnotation {

  /**
   * The domain of the command, typically representing the command's hierarchical structure or
   * categorization (e.g., "sub.command.$type.%name").
   *
   * <p>The domain can include placeholders using special symbols:
   *
   * <ul>
   *   <li><b>$</b>: Indicates that any argument can be passed in this position. Tab completion will
   *       suggest replacements based on the provided supplier or defaulting the concatenated text.
   *   <li><b>%</b>: Similar to '$', but can only be used on the last argument of a domain. It
   *       implies that an infinite number of arguments can be passed after this position. Tab
   *       completion will suggest replacements based on the provided supplier or defaulting the
   *       concatenated text.
   * </ul>
   *
   * <p><b>Examples:</b>
   *
   * <ul>
   *   <li><code>"sub.command.$<name>"</code>: The <code>$</code> indicates that any <code>
   *       &lt;name&gt;</code> can be passed, and tab completion will suggest possible names.
   *   <li><code>"sub.command.%<options>"</code>: The <code>%</code> indicates that multiple <code>
   *       &lt;options&gt;</code> can be passed after the command, each with its own tab completion
   *       suggestions.
   * </ul>
   *
   * @return the command domain with optional placeholders
   */
  String domain(); // Command domain (e.g., "region.create")

  /**
   * The required permission node for executing this command. If left empty, no specific permission
   * is required.
   *
   * @return the permission node, or an empty string if no permission is required
   */
  String permission() default ""; // Required permission

  /**
   * A brief description of what the command does. This can be used in help menus or documentation.
   *
   * @return the command description
   */
  String description() default ""; // Command description

  /**
   * Instructions on how to use the command, including required and optional arguments.
   *
   * @return the usage information for the command
   */
  String usage() default ""; // Usage information
}
