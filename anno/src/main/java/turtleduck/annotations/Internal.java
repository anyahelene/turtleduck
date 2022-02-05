package turtleduck.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a type is internal to a library, and is not meant to be used
 * directly.
 * 
 * Used as an IDE hint to:
 * <ul>
 * <li>For interfaces, display the name of more specific interface instead
 * (i.e., one that extends this interface)
 * <li>For classes, display the name of the interface this class is (most
 * likely) primarily implementing
 * <li>Avoid using this type in completions or in suggestions to the user
 * <li>Warn against importing it in user code
 * </ul>
 * 
 * <p>
 * For example, this could used for
 * <ul>
 * <li>a helper interface that declares methods common to several other
 * interfaces, particularly when it has a non-userfriendly generic signature.
 * <li>a class that implmenents an interface but is never meant to be used
 * directly – only obtained through a factory
 * </ul>
 * 
 * 
 *         TODO: See if sealed types could work instead
 *         https://openjdk.java.net/jeps/360
 *
 * @author Anya Helene Bagge
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Internal {

	/**
	 * @return A list of interfaces that may be more appropriate to show to a user.
	 */
	Class<?>[] to() default {};

}
