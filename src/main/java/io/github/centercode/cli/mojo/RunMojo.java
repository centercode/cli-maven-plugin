package io.github.centercode.cli.mojo;

import io.github.centercode.cli.common.Constant;
import io.github.centercode.cli.util.IOUtil;
import io.github.centercode.cli.util.StringUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Mojo(name = Constant.GOAL_RUN)
public class RunMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = Constant.GOAL_RUN + ".commands", required = true, readonly = true)
    private List<String> commands;

    private final Set<String> variableBlacklist = new HashSet<>(Arrays.asList(
            "getParentFile",
            "getAbsoluteFile",
            "getCanonicalFile",
            "getParentPath",
            "getAbsolutePath",
            "getCanonicalPath",
            "getProjectBuildingRequest",
            "getExecutionProject"
    ));

    @Override
    public void execute() throws MojoExecutionException {
        try {
            String[] env = getVariables();
            for (int i = 0; i < commands.size(); i++) {
                String command = commands.get(i);
                executeCommand(i, command, env);
            }
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void executeCommand(int index, String command, String[] env)
            throws MojoExecutionException, InterruptedException, IOException {
        getLog().info("Executing command[" + index + "]:[" + command + "]");
        Process process = Runtime.getRuntime().exec(command, env);
        int exitCode = process.waitFor();
        String stdout = IOUtil.read(process.getInputStream());
        getLog().info("Output of command[" + index + "]:\n " + stdout);
        String stderr = IOUtil.read(process.getErrorStream());
        if (!stderr.isEmpty()) {
            getLog().error("Error of command[" + index + "]:\n" + stderr);
        }
        if (exitCode != 0) {
            throw new MojoExecutionException("Command failed with exit code:" + exitCode);
        }
    }

    private String[] getVariables() throws InvocationTargetException, IllegalAccessException {
        List<String> list = new LinkedList<>();
        collect(list, "project", project);
        String[] env = list.toArray(new String[0]);
        for (String s : env) {
            getLog().debug("[cli-maven-plugin] env: " + s);
        }

        return env;
    }

    /**
     * using reflect to recursively get maven variables
     */
    private void collect(List<String> list, String fullPrefix, Object node)
            throws InvocationTargetException, IllegalAccessException {
        for (Method method : node.getClass().getDeclaredMethods()) {
            if (canCollect(method)) {
                Object val = method.invoke(node);
                String variableName = toFullVariableName(fullPrefix, method.getName());
                if (null != val) {
                    if (String.class.equals(method.getReturnType())) {
                        //leaf node
                        list.add(variableName + "=" + val);
                    } else {
                        //recursively collect
                        collect(list, variableName, val);
                    }
                }
            }
        }
    }

    private String toFullVariableName(String prefix, String methodName) {
        String variableName = methodName.substring("get".length());
        return prefix + "_" + StringUtil.lowercaseFirst(variableName);
    }

    private boolean canCollect(Method method) {
        String methodName = method.getName();
        return methodName.startsWith("get")
                && (method.getModifiers() & 0x1) == 1 //public modifier
                && method.getParameterCount() == 0
                && !variableBlacklist.contains(methodName);
    }
}
