package usac.eps.seguridad;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para proteger endpoints REST con roles específicos.
 * 
 * Uso:
 * @RequiresRole({"ADMIN"}) // Solo ADMIN
 * @RequiresRole({"ADMIN", "SUPERVISOR"}) // ADMIN o SUPERVISOR
 * 
 * @author Sistema de Mantenimientos INACIF
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    /**
     * Lista de roles permitidos. El usuario debe tener AL MENOS UNO de estos roles.
     */
    String[] value();

    /**
     * Si es true, el usuario debe tener TODOS los roles especificados.
     * Por defecto es false (solo necesita uno).
     */
    boolean requireAll() default false;

    /**
     * Mensaje personalizado de error cuando no tiene permisos.
     */
    String message() default "No tiene permisos para realizar esta acción";
}
