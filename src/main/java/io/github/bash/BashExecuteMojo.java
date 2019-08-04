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

    @Parameter(property = Constant.EXEC_GOAL + ".commands", required = true, readonly = true)
    private List<String> commands;

    private Set<String> blacklist = new HashSet<>();

    {
        String[] arr = {"getParentFile", "getAbsoluteFile", "getCanonicalFile", "getParentPath", "getAbsolutePath", "getCanonicalPath", "getProjectBuildingRequest", "getExecutionProject"};
        blacklist.addAll(Arrays.asList(arr));
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            List<String> list = new LinkedList<>();
            getEnv(list, project, "project");
            String[] env = list.toArray(new String[0]);
            for (String s : env) {
                getLog().debug("env:" + s);
            }
            System.out.println(commands);
            for (String command : commands) {
                executeCommand(command, env);
            }
        } catch (IOException | InterruptedException | IllegalAccessException | InvocationTargetException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void executeCommand(String command, String[] env) throws MojoExecutionException, InterruptedException, IOException {
        getLog().info("Executing commands:" + command);
        Process process = Runtime.getRuntime().exec(command, env);
        int exitCode = process.waitFor();
        String stdout = toString(process.getInputStream());
        System.out.println(stdout);
        if (exitCode != 0) {
            String stderr = toString(process.getErrorStream());
            throw new MojoExecutionException(stderr);
        }

    }

    private void getEnv(List<String> list, Object node, String prefix) throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = node.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("get")
                    && (method.getModifiers() & 0x1) == 1 //public modifier
                    && method.getParameterCount() == 0
                    && !blacklist.contains(methodName)) {
                String key = prefix + "_" + toLowerCaseFirstOne(methodName.substring(3, methodName.length()));
//                getLog().info(key);
                Object val = method.invoke(node);
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

    private static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
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
