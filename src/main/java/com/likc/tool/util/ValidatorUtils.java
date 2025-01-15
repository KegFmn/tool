package com.likc.tool.util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidatorUtils {

    private static final Validator validator;

    static {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public static <T> String validate(T obj) {
        Set<ConstraintViolation<T>> set = validator.validate(obj);
        return set.stream().sorted(Comparator.comparing(ConstraintViolation::getMessage)).map(ConstraintViolation::getMessage).collect(Collectors.joining("，"));
    }
}
