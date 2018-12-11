package io.freefair.gradle.plugin.codegenerator;

import io.freefair.gradle.codegenerator.api.Generator;
import io.freefair.gradle.codegenerator.api.ProjectContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

class CodeGeneratorExecutor {
	private Constructor<?> constructor;

	CodeGeneratorExecutor(Class<?> c) throws Exception {
		Constructor<?> constructor = Arrays.stream(c.getConstructors()).filter(ctr -> ctr.getParameterCount() == 0).findFirst().orElse(null);
		if(constructor == null) throw new Exception("No default constructor found for " + c.getCanonicalName());
		if(!constructor.isAccessible()) constructor.setAccessible(true);
		this.constructor = constructor;
	}


	public void execute(ProjectContext context) throws Exception {
		Object o = constructor.newInstance();
		Generator gen = null;
		if(isCodeGeneratorInterface(o))
			gen = (Generator) o;
		else
			gen = new GeneratorWrapper(o);

		gen.generate(context);
	}

	private boolean isCodeGeneratorInterface(Object o){
		return o instanceof Generator;
	}

	private class GeneratorWrapper implements Generator {
		private Object instance;
		private Method generateMethod;
		public GeneratorWrapper(Object o) throws Exception {
			this.instance = o;
			Class<?> aClass = this.instance.getClass();
			Method generate = aClass.getMethod("generate", ProjectContext.class);

			if(!generate.isAccessible()) generate.setAccessible(true);

			this.generateMethod = generate;
		}

		@Override
		public void generate(ProjectContext context) throws Exception {
			generateMethod.invoke(instance, context);
		}
	}
}
