package org.example.flowmanager.api.utils;

import java.util.regex.Pattern;

public class Utils {

    public final static int ONLY_NAME = 0;

    public final static int ONLY_EXTENSION = 1;

    public final static Pattern REMOVE_LAST_ELEMENT_PATTERN = Pattern.compile("[\\\\/][^\\\\/]+$");

    public final static Pattern REPLACE_FIRST_WORD_PATTERN = Pattern.compile("^[^\\\\/]+");

    public final static Pattern REPLACE_EXTENSION_PATTERN = Pattern.compile("\\.[^.]+$");

    public final static String SLASH = "/";

    public final static String NO_LOGIN = "no_login";

    public final static String FIELD_USER_LOGIN = "X-User-Login";

    public final static String TYPE_REQUEST = "request";

    public final static double BYTES_IN_MB = 1_048_576.0;

    public final static double MAX_SIZE_AVAILABLE_FILE_IN_MB = 100.0;
}