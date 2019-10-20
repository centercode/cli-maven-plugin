# Cli-Maven-Plugin

### Introduction

A maven plugin passing maven properties to cli script executing, support variables including but not limited to these:

一个支持运行cli脚本的maven插件，可以将maven的变量传递到脚本的环境变量中，可以在脚本中引用包括但不限于以下的maven变量：

```
$project_groupId
$project_artifactId
$project_version
$project_packaging
$project_basedir
$project_build
$project_name
$project_resources
$project_parent
$project_properties
...
```

### Usage

1.Configure pom.xml:

1.配置项目的pom.xml文件：

```
<plugin>
    <groupId>io.github.yx91490</groupId>
    <artifactId>cli-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <commands>
            <command>${basedir}/example.sh a b c</command>
            <!-- 支持多条命令顺序执行 -->
            <!-- <command>${basedir}/example.sh d e f</command> -->
        </commands>
    </configuration>
</plugin>
```

2.execute(on linux):

2.执行(以linux环境示例)：

```
$ cat example.sh

#! /bin/bash
echo "Project:[${project_artifactId}], parameters:[${@}]"

$ mvn cli:run
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building cli-maven-plugin 1.0.0
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- cli-maven-plugin:1.0.0:run (default-cli) @ cli-maven-plugin ---
[/Users/user/cli-maven-plugin/example.sh]
[INFO] Executing commands:/Users/user/cli-maven-plugin/example.sh
Project:[cli-maven-plugin], parameters:[a b c]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.357 s
[INFO] Finished at: 2019-08-10T13:37:27+08:00
[INFO] Final Memory: 9M/217M
[INFO] ------------------------------------------------------------------------
```

### Debug

print maven environment:

打印maven变量:

```
mvn cli:run --debug
```

the maven environment output:

输出的maven变量:

```
[DEBUG][cli-maven-plugin] env: project_version=1.0.0
[DEBUG][cli-maven-plugin] env: project_artifactId=cli-maven-plugin
[DEBUG][cli-maven-plugin] env: project_basedir_name=cli-maven-plugin
...
```
