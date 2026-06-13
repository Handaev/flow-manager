package org.example.flowmanager.api.service.utils;

import java.util.regex.Pattern;

public class Utils {

    public final static int ONLY_NAME = 0;

    public final static int ONLY_EXTENSION = 1;

    public final static Pattern REMOVE_LAST_ELEMENT_PATTERN = Pattern.compile("[\\\\/][^\\\\/]+$");

    public final static Pattern REPLACE_FIRST_WORD_PATTERN = Pattern.compile("^[^\\\\/]+");

    public final static Pattern REPLACE_EXTENSION_PATTERN = Pattern.compile("\\.[^.]+$");

    public final static String SLASH = "/";

}
