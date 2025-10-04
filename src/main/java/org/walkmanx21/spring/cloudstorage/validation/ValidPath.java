package org.walkmanx21.spring.cloudstorage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PathConstraintValidator.class)
@Documented
@NotBlank
@Size(max = 1000, message = "Поле from должно быть не более 1000 символов")
public @interface ValidPath {
    String message() default "Невалидный либо отсутствующий путь";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
