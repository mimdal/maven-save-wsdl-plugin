SAVE WSDL MAVEN PLUGIN
======================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)][license]

About
---------------
In briefly, It Saves the soap WSDL file and all xsd files. 

Building
------------------
You need Java JDK 8 to build Plugin. Make sure Maven is using Java JDK 8 by setting JAVA_HOME before running Maven:

- export JAVA_HOME=/PATH/TO/JDK/8
- mvn clean install


Config
------------------
Add this setting to maven pom file. Default lifecycle set to PACKAGE phase.
```xml
<plugin>
        <groupId>com.github.mimdal</groupId>
        <artifactId>save-wsdl-maven-plugin</artifactId>
        <version>${save.wsdl.maven.plugin.version}</version>
        <executions>
            <execution>
                <goals>
                    <goal>save</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <endPoint>http://ip:port/wsdl</endPoint>
            <xsdDirectory>xsd</xsdDirectory>
            <relativeSaveDirectory>src/main/resources/wsdl</relativeSaveDirectory>
        </configuration>
</plugin>
```

Contribute
------------------
You have found a bug or you have an idea for a cool new feature? Contributing
code is a great way to give something back to the open source community. 


[license]: https://www.apache.org/licenses/LICENSE-2.0
