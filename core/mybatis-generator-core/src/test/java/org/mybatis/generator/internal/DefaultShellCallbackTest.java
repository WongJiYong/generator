/**
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.internal;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.mybatis.generator.exception.ShellException;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DefaultShellCallbackTest {

    private String model = "import lombok.Getter;\n" +
            "import lombok.Setter;\n" +
            "\n" +
            "/**\n" +
            " *   部门\n" +
            " *\n" +
            " * table dept\n" +
            " */\n" +
            "@Setter\n" +
            "@Getter\n" +
            "public class Test {\n" +
            "    /**\n" +
            "     * ID\n" +
            "     *\n" +
            "     * @mbg.generated\n" +
            "     */\n" +
            "    private Long id;\n" +
            "\n" +
            "    /**\n" +
            "     * 名称\n" +
            "     *\n" +
            "     * @mbg.generated\n" +
            "     */\n" +
            "    private String name;\n" +
            "\n" +
            "    /**\n" +
            "     * sex\n" +
            "     *\n" +
            "     * @mbg.generated\n" +
            "     */\n" +
            "    private String sex;\n" +
            "\n" +
            "}";

    private String mapperFile = "import com.test.TestExample;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public interface TestMapper {\n" +
            "\n" +
            "    long countByExample(TestExample example);\n" +
            "\n" +
            "    List<Test> selectMenusByRoleId(Long roleId);\n" +
            "\n" +
            "}\n";

    @org.junit.Test
    public void mergeJavaFile() {
        DefaultShellCallback shellCallback = new DefaultShellCallback(false);
        URL resource = getClass().getClassLoader().getResource("mergeFile/Test.java");
        URL mapper = getClass().getClassLoader().getResource("mergeFile/TestMapper.java");
        assert resource != null;
        assert mapper != null;
        File existingFile = new File(resource.getPath());
        Set<String> imports = new HashSet<>();
        imports.add("lombok.Getter");
        imports.add("lombok.Setter");
        imports.add("java.util.List");
        Set<String> fields = new HashSet<>();
        fields.add("id");
        fields.add("name");
        fields.add("newFields");
        fields.add("sex");
        try {
            String s = shellCallback.mergeJavaFile(model, existingFile, null, "UTF-8");
            ParserConfiguration config = new ParserConfiguration();
            config.setCharacterEncoding(StandardCharsets.UTF_8);
            CompilationUnit cu = StaticJavaParser.parse(s);
            NodeList<ImportDeclaration> importDeclarations = cu.getImports();
            for (ImportDeclaration anImport : importDeclarations) {
                assert imports.contains(anImport.getNameAsString());
            }
            Optional<ClassOrInterfaceDeclaration> testClassOp = cu.getClassByName("Test");
            assert testClassOp.isPresent();
            ClassOrInterfaceDeclaration testClass = testClassOp.get();
            List<FieldDeclaration> fieldDeclarations = testClass.getFields();
            assert fieldDeclarations.size() == 4;
            for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
                String variableName = fieldDeclaration.getVariable(0).getNameAsString();
                assert fields.contains(variableName);
            }

            String s1 = shellCallback.mergeJavaFile(mapperFile, new File(mapper.getPath()), null, "UTF-8");
            System.out.println(s1);
        } catch (ShellException e) {
            e.printStackTrace();
        }
    }
}