MyBatis Generator (MBG)
=======================

在原有的MBG上扩展了一些辅助功能。
需要依赖`javaparser`项目
- 合并Java代码
    + model类合并字段
    + Mapper类合并方法
    + Example类不会被合并
- 根据element id 合并XML mapper文件。 
- model类可以使用lombok

maven 示例
```
<plugin>
	<groupId>org.mybatis.generator</groupId>
	<artifactId>mybatis-generator-maven-plugin</artifactId>
	<version>1.4.0</version>
	<configuration>
		<overwrite>false</overwrite>
		<configurationFile>${basedir}/src/main/resources/generatorConfig.xml</configurationFile>
	</configuration>
	<dependencies>
		<dependency>
			<groupId>com.github.javaparser</groupId>
			<artifactId>javaparser-core</artifactId>
			<version>3.15.15</version>
		</dependency>
	</dependencies>
</plugin>
```
配置文件实例
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>

    <classPathEntry
            location="D:\maven\repository\mysql\mysql-connector-java\8.0.17\mysql-connector-java-8.0.17.jar"/>

    <context id="DB2Tables" targetRuntime="MyBatis3" defaultModelType="flat">
        <!-- 指定Java编码格式 -->
        <property name="javaFileEncoding" value="UTF-8"/>
        <!-- 使用自定义注释生成 -->
        <commentGenerator type="org.mybatis.generator.internal.CustomCommentGenerator">
            <property name="suppressAllComments" value="false"/>
            <property name="addRemarkComments" value="true"/>
            <property name="suppressDate" value="true"/>
        </commentGenerator>
        <jdbcConnection driverClass="com.mysql.cj.jdbc.Driver"
                        connectionURL="jdbc:mysql://localhost:3306/myproject"
                        userId="******"
                        password="******">
        </jdbcConnection>

        <javaTypeResolver>
            <property name="forceBigDecimals" value="false"/>
        </javaTypeResolver>

        <javaModelGenerator targetPackage="com.xuersheng.myProject.model"
                            targetProject="src/main/java">
            <property name="enableSubPackages" value="true"/>
            <property name="trimStrings" value="true"/>
            <!-- 指定example生成包 -->
            <property name="exampleTargetPackage" value="com.xuersheng.myProject.model.example"/>
            <!-- 使用Lombok -->
            <property name="useLombok" value="true"/>
        </javaModelGenerator>

        <sqlMapGenerator targetPackage="sqlMapper"
                         targetProject="src/main/resources">
            <property name="enableSubPackages" value="true"/>
        </sqlMapGenerator>

        <javaClientGenerator type="XMLMAPPER" targetPackage="com.xuersheng.myProject.mapper"
                             targetProject="src/main/java">
            <property name="enableSubPackages" value="true"/>
        </javaClientGenerator>

        <table schema="myproject" tableName="table" alias="t"/>
    </context>
</generatorConfiguration>
```