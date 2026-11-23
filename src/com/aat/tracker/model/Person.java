package com.aat.tracker.model;

import com.aat.tracker.exception.ValidationException;
import com.aat.tracker.util.Validator;

/**
 * Abstract base class for every person stored by the system
 * (Week 5: abstract classes & inheritance).
 *
 * <p>Demonstrates encapsulation: all fields are private and every setter
 * guards its own value by throwing a user-defined {@link ValidationException}
 * (Week 4: access modifiers, Week 8: user-defined exceptions).</p>
 */
public abstract class Person {

    /** Maximum allowed length of a stored name (Week 4: static + final). */
    public static final int MAX_NAME_LENGTH = 100;

    private String name;
    private String email;
    private String phone;

    /** Primary constructor (Week 4: constructors). */
    public Person(String name, String email, String phone) throws ValidationException {
        setName(name);
        setEmail(email);
        setPhone(phone);
    }

    /** Convenience overload with default contact details (Week 4: overloading). */
    public Person(String name) throws ValidationException {
        this(name, "", "");
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name cannot be blank.");
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Name must be at most " + MAX_NAME_LENGTH + " characters.");
        }
        this.name = name.trim();
    }

    public void setEmail(String email) throws ValidationException {
        String value = email == null ? "" : email.trim();
        if (!value.isEmpty() && !Validator.isValidEmail(value)) {
            throw new ValidationException("Invalid email address: " + value);
        }
        this.email = value;
    }

    public void setPhone(String phone) throws ValidationException {
        String value = phone == null ? "" : phone.trim();
        if (!value.isEmpty() && !Validator.isValidPhone(value)) {
            throw new ValidationException("Invalid phone number: " + value);
        }
        this.phone = value;
    }

    /** Abstract contract: each subclass states its role (polymorphism). */
    public abstract String getRoleLabel();

    /** Polymorphic display text used by GUI tables and combo boxes. */
    @Override
    public String toString() {
        return name;
    }
}
