package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.DateRangeValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DateRangeValidator implements ConstraintValidator<DateRangeValid, Object> {

    private String startField;

    private String endField;

    private boolean allowEqual;

    @Override
    public void initialize(DateRangeValid constraintAnnotation) {
        this.startField = constraintAnnotation.startField();
        this.endField = constraintAnnotation.endField();
        this.allowEqual = constraintAnnotation.allowEqual();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        Object start = wrapper.getPropertyValue(startField);
        Object end = wrapper.getPropertyValue(endField);
        if (start == null || end == null) {
            return true;
        }
        if (!(start instanceof Comparable comparableStart) || !(end instanceof Comparable)) {
            return false;
        }
        int compare = comparableStart.compareTo(end);
        return allowEqual ? compare <= 0 : compare < 0;
    }
}
