package broccolies.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import javax.annotation.processing.Filer;
import java.io.Writer;
import java.io.IOException;

public class WriterUtil {
	public static void write(Filer filer, ClassName className, TypeSpec.Builder typeBuilder) throws IOException {
		String fullyQualifiedClassName = className.packageName().equals("")
			? className.simpleName()
			: className.packageName() + "." + className.simpleName();
		Writer writer = filer.createSourceFile(fullyQualifiedClassName).openWriter();
		JavaFile javaFile = JavaFile.builder(className.packageName(), typeBuilder.build()).build();
		javaFile.writeTo(writer);
		writer.close();
	}
}
