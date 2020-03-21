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
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class DefaultShellCallback implements ShellCallback {

    private boolean overwrite;

    public DefaultShellCallback(boolean overwrite) {
        super();
        this.overwrite = overwrite;
    }

    @Override
    public File getDirectory(String targetProject, String targetPackage)
            throws ShellException {
        // targetProject is interpreted as a directory that must exist
        //
        // targetPackage is interpreted as a sub directory, but in package
        // format (with dots instead of slashes). The sub directory will be
        // created
        // if it does not already exist

        File project = new File(targetProject);
        if (!project.isDirectory()) {
            throw new ShellException(getString("Warning.9", //$NON-NLS-1$
                    targetProject));
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, "."); //$NON-NLS-1$
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                throw new ShellException(getString("Warning.10", //$NON-NLS-1$
                        directory.getAbsolutePath()));
            }
        }

        return directory;
    }

    @Override
    public void refreshProject(String project) {
        // nothing to do in the default shell callback
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

    @Override
    public boolean isOverwriteEnabled() {
        return overwrite;
    }

    @Override
    public String mergeJavaFile(String newFileSource,
                                File existingFile, String[] javadocTags, String fileEncoding)
            throws ShellException {

//        throw new UnsupportedOperationException();
        //根据生成java文件名判断改使用怎么样的合并策略
        String publicClassName = existingFile.getName().substring(0, existingFile.getName().length() - ".java".length());

        if (publicClassName.endsWith("Example")) {
            //不合并
            return newFileSource;
        }
        ParserConfiguration pc = new ParserConfiguration();
        StaticJavaParser.setConfiguration(pc);
        CompilationUnit newCompilationUnit = StaticJavaParser.parse(newFileSource);
        CompilationUnit oldCompilationUnit;
        try {
            if (StringUtility.stringHasValue(fileEncoding)) {
                pc.setCharacterEncoding(Charset.forName(fileEncoding));
            }
            oldCompilationUnit = StaticJavaParser.parse(existingFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "merge error";
        }
        ClassVisitor classVisitor = new ClassVisitor();
        ClassInfo oldClassInfo = new ClassInfo();
        ClassInfo newClassInfo = new ClassInfo();
        classVisitor.visit(oldCompilationUnit, oldClassInfo);
        classVisitor.visit(newCompilationUnit, newClassInfo);

        mergeImports(oldClassInfo, newClassInfo, newCompilationUnit);

        if (publicClassName.endsWith("Mapper")) {
            //merge Mapper,only merge method;
            mergeMethods(oldClassInfo, newClassInfo, newCompilationUnit, publicClassName);
        } else {
            //merge model, only merge files;
            mergeFields(oldClassInfo, newClassInfo, newCompilationUnit, publicClassName);
        }
        PrettyPrinterConfiguration conf = new PrettyPrinterConfiguration();
        conf.setPrintComments(true);
        PrettyPrinter prettyPrinter = new PrettyPrinter(conf);
        return prettyPrinter.print(newCompilationUnit);
    }

    private void mergeImports(ClassInfo source, ClassInfo target, CompilationUnit output) {
        Map<String, ImportDeclaration> sourceImport = source.imports;
        Map<String, ImportDeclaration> targetImport = target.imports;

        targetImport.forEach((k, v) -> {
            sourceImport.remove(k);
        });
        sourceImport.forEach((k, v) -> {
            output.addImport(v);
        });
    }

    private void mergeFields(ClassInfo source, ClassInfo target, CompilationUnit output, String className) {

        Map<String, FieldDeclaration> sourceFields = source.fields;
        Map<String, FieldDeclaration> sourceAutoGeneratedFields = source.autoGeneratedFields;

        Map<String, FieldDeclaration> targetFields = target.fields;

        Map<String, FieldDeclaration> mergedFields = new LinkedHashMap<>();

        sourceFields.forEach((k, v) -> {
            if (sourceAutoGeneratedFields.containsKey(k)) {
                return;
            }
            if (targetFields.containsKey(k)) {
                return;
            }
            mergedFields.put(k, v);
        });

        Optional<ClassOrInterfaceDeclaration> optional = output.getClassByName(className);
        if (!optional.isPresent()) {
            optional = output.getInterfaceByName(className);
        }
        if (optional.isPresent()) {
            ClassOrInterfaceDeclaration classDeclaration = optional.get();
            NodeList<AnnotationExpr> annotations = classDeclaration.getAnnotations();
            boolean useLombok = false;
            for (AnnotationExpr annotationExpr : annotations) {
                String name = annotationExpr.getNameAsString();
                if (name.contains("Setter") ||
                        name.contains("Getter") ||
                        name.contains("Data")) {
                    useLombok = true;
                    break;
                }
            }
            boolean finalUseLombok = useLombok;
            mergedFields.forEach((k, v) -> {
                classDeclaration.addMember(v);
            });
            if (!finalUseLombok) {
                mergedFields.forEach((k, v) -> {
                    classDeclaration.addMember(v.createGetter());
                    classDeclaration.addMember(v.createSetter());
                });
            }
        }
    }

    private void mergeMethods(ClassInfo source, ClassInfo target, CompilationUnit output, String className) {
        Map<String, MethodDeclaration> targetMethods = target.methods;
        Map<String, MethodDeclaration> sourceMethods = source.methods;
        targetMethods.forEach((k, v) -> {
            sourceMethods.remove(k);
        });
        Optional<ClassOrInterfaceDeclaration> optional = output.getClassByName(className);
        if (!optional.isPresent()) {
            optional = output.getInterfaceByName(className);
        }
        if (optional.isPresent()) {
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration = optional.get();
            sourceMethods.forEach((k, v) -> {
                classOrInterfaceDeclaration.addMember(v);
            });
        }
    }

    static class ClassVisitor extends VoidVisitorAdapter<ClassInfo> {
        @Override
        public void visit(ImportDeclaration n, ClassInfo classInfo) {
            super.visit(n, classInfo);
            Map<String, ImportDeclaration> arg = classInfo.imports;
            arg.put(n.getNameAsString(), n);
        }

        @Override
        public void visit(MethodDeclaration n, ClassInfo classInfo) {
            super.visit(n, classInfo);
            Map<String, MethodDeclaration> arg = classInfo.methods;
            arg.put(getMethodTag(n), n);
        }

        @Override
        public void visit(FieldDeclaration n, ClassInfo classInfo) {
            super.visit(n, classInfo);
            Map<String, FieldDeclaration> fields = classInfo.fields;
            Map<String, FieldDeclaration> autoGeneratedFields = classInfo.autoGeneratedFields;
            Optional<Comment> comment = n.getComment();
            fields.put(n.getVariable(0).getNameAsString(), n);
            if (comment.isPresent()) {
                if (isGenerated(comment.get())) {
                    autoGeneratedFields.put(n.getVariable(0).getNameAsString(), n);
                }
            }
        }

        private boolean isGenerated(Comment comment) {
            String text = comment.getContent();
            for (String tag : MergeConstants.OLD_ELEMENT_TAGS) {
                if (text.contains(tag)) {
                    return true;
                }
            }
            return false;
        }

        public String getMethodTag(MethodDeclaration methodDeclaration) {
            StringBuilder sb = new StringBuilder(methodDeclaration.getNameAsString());
            NodeList<Parameter> parameters = methodDeclaration.getParameters();
            if (parameters != null) {
                for (Parameter parameter : parameters) {
                    sb.append(";").append(parameter.getTypeAsString()).append(":").append(parameter.getNameAsString());
                }
            }
            return sb.toString();
        }
    }

    static class ClassInfo {
        Map<String, ImportDeclaration> imports = new LinkedHashMap<>();
        Map<String, FieldDeclaration> fields = new LinkedHashMap<>();
        Map<String, FieldDeclaration> autoGeneratedFields = new LinkedHashMap<>();
        Map<String, MethodDeclaration> methods = new LinkedHashMap<>();
    }

}
