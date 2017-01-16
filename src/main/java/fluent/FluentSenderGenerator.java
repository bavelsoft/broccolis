package broccolies;

import java.util.function.Consumer;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.io.IOException;
import broccolies.util.GeneratorUtil;
import broccolies.util.LastRunnable;
import broccolies.annotation.FluentSender;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import broccolies.util.FluentSenderGeneratorBase;

public class FluentSenderGenerator extends FluentSenderGeneratorBase {
	public FluentSenderGenerator(Types typeUtils, Elements elementUtils, Filer filer) {
		super(typeUtils, elementUtils, filer);
	}

	@Override
	protected void addSendMethod(TypeSpec.Builder typeBuilder) {
    		typeBuilder.addMethod(MethodSpec.methodBuilder("send")
        		.addModifiers(Modifier.PUBLIC)
			.addStatement("$T.unset()", LastRunnable.class)
        		.addStatement("$L.run()", onSend)
        		.addStatement("$L.accept($L)", consumer, underlying)
        		.build());
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
