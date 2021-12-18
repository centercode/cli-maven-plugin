package io.github.centercode.cli.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOUtil {

    private IOUtil() {
    }

    public static String read(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader streamReader = new InputStreamReader(in);
             BufferedReader reader = new BufferedReader(streamReader)) {
            String line;
            if ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            while ((line = reader.readLine()) != null) {
                sb.append('\n').append(line);
            }
        }

        return sb.toString();
    }
}
