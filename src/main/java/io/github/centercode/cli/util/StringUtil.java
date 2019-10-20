package io.github.centercode.cli.util;

public class StringUtil {

  private StringUtil() {
  }

  public static String lowercaseFirst(String s) {
    if (Character.isLowerCase(s.charAt(0))) {
      return s;
    } else {
      return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
  }
}
