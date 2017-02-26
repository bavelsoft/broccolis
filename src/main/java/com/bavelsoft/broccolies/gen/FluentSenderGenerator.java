package com.bavelsoft.broccolies.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.bavelsoft.broccolies.util.FluentSenderGeneratorBase;
import com.bavelsoft.broccolies.util.LastRunnable;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class FluentSenderGenerator extends FluentSenderGeneratorBase {
	public FluentSenderGenerator(Types typeUtils, Elements elementUtils, Filer filer) {
		super(typeUtils, elementUtils, filer);
	}

	@Override
	protected void populateSendMethod(MethodSpec.Builder methodBuilder) {
		methodBuilder
			.addStatement("$T.unset()", LastRunnable.class)
			.addStatement("$L.run()", onSend)
			.addStatement("$L.accept($L)", consumer, underlying);
	}

	@Override
	protected void makeRunnable(TypeSpec.Builder typeBuilder) {
		typeBuilder
			.addSuperinterface(ClassName.get(Runnable.class))
			.addMethod(MethodSpec.methodBuilder("run")
				.addModifiers(Modifier.PUBLIC)
				.addStatement("send()")
				.build());
	}

	@Override
	protected void addRunLast(MethodSpec.Builder constructor) {
		constructor.addStatement("$T.set(this)", LastRunnable.class);
	}
}
