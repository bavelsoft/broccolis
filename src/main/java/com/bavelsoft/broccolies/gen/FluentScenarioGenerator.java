package com.bavelsoft.broccolies.gen;

import com.bavelsoft.broccolies.util.FluentSenderGeneratorBase;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class FluentScenarioGenerator extends FluentSenderGeneratorBase {
	public FluentScenarioGenerator(Types typeUtils, Elements elementUtils, Filer filer) {
		super(typeUtils, elementUtils, filer);
	}

	@Override
	protected TypeSpec.Builder getType(String initializer, TypeElement te, TypeElement reference, ClassName className) {
                return super.getType(initializer, te, reference, className)
			.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());
	}

	@Override
	protected void addSendMethod(TypeSpec.Builder typeBuilder) {
    		typeBuilder.addMethod(MethodSpec.methodBuilder("send")
        		.addModifiers(Modifier.PUBLIC)
        		.build());
	}

	@Override
	protected void addRunLast(MethodSpec.Builder constructor) {
	}

	@Override
	protected void makeRunnable(TypeSpec.Builder typeBuilder) {
	}

}
