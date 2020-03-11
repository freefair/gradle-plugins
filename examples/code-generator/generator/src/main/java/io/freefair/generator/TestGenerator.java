package io.freefair.generator;


import io.freefair.gradle.codegenerator.api.Generator;
import io.freefair.gradle.codegenerator.api.ProjectContext;
import io.freefair.gradle.codegenerator.api.annotations.CodeGenerator;

@CodeGenerator
public class TestGenerator implements Generator {
    @Override
    public void generate(ProjectContext context) throws Exception {
        context.writeOutputFile("io.freefair.gradle.codegen.test","TestClass.java", "package io.freefair.gradle.codegen.test;\nclass TestClass {\n}");
    }
}
