package io.github.bash;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Mojo(name = Constant.EXEC_GOAL)
public class BashExecuteMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = Constant.EXEC_GOAL + ".file")
    private String file;

    private Set<String> blacklist = new HashSet<>();

    {
        String[] arr = {"getParentFile", "getAbsoluteFile", "getCanonicalFile","getParentPath", "getAbsolutePath", "getCanonicalPath", "getProjectBuildingRequest", "getExecutionProject"};
        blacklist.addAll(Arrays.asList(arr));
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            List<String> list = new LinkedList<>();
            getEnv(list, project, "project");
            String[] env = list.toArray(new String[0]);
//            for (String s : env) {
//                System.out.println("env:" + s);
//            }
            Process process = Runtime.getRuntime().exec(file, env);
            int exitCode = process.waitFor();
            String stdout = toString(process.getInputStream());
            System.out.println(stdout);
            if (0 != exitCode) {
                String stderr = toString(process.getErrorStream());
                throw new MojoExecutionException(stderr);
            }
        } catch (IOException | InterruptedException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
    }

    private void getEnv(List<String> list, Object obj, String prefix) throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = obj.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("get")
                    && (method.getModifiers() & 0x1) == 1 //public modifier
                    && method.getParameterCount() == 0
                    && !blacklist.contains(methodName)) {
                String key = prefix + "_" + toLowerCaseFirstOne(methodName.substring(3, methodName.length()));
//                System.out.println(key);
                Object val = method.invoke(obj);
                if (null != val) {
                    if (method.getReturnType().equals(String.class)) {
                        list.add(key + "=" + val);
                    } else {
                        getEnv(list, val, key);
                    }
                }
            }
        }
    }

    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    private String toString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader inputStreamReader = new InputStreamReader(in);
             BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            if (line != null) {
                sb.append(line);
            }
            while ((line = reader.readLine()) != null) {
                sb.append('\n').append(line);
            }
        }

        return sb.toString();
    }
}
