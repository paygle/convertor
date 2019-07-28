package compiler;

import com.dodar.compiler.MemoryJavaCompiler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.dodar.utils.Log.logger;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemoryJavaCompilerTest {
    private final static MemoryJavaCompiler compiler = new MemoryJavaCompiler();

    @Test
    public void compileStaticMethodTest() throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        final String source = "public final class Solution {\n"
                + "public static String greeting(String name) {\n"
                + "\treturn \"Hello \" + name;\n" + "}\n}\n";
        final Method greeting = compiler.compileStaticMethod("greeting", "Solution", source);
        final Object result = greeting.invoke(null, "soulmachine");
        logger.info(result.toString());
        assertEquals("Hello soulmachine", result.toString());
    }
}
