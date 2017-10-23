package ru.sedi.customerclient.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class: Validator
 * Author: RAM
 * Description: Проверка строк на совместимость через регулярные выражения;
 */
public class Validator {
    public static final String PHONE_PATTERN = "^[+][0-9]{10,15}$";
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    public static boolean Valid(String pattern, String validateString) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(validateString);
        return m.matches();
    }

}
