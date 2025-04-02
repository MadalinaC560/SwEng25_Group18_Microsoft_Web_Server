
package com.webserver.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


public class ScriptProcessorFactoryTest {
    @Test
    public void testScriptProcessorFactory() {
        ScriptProcessorFactory factory = new ScriptProcessorFactory();
        ScriptProcessor processor = factory.getProcessorForExtension("php");
        System.out.println(processor.getClass().getSimpleName()); // Prints: PhpScriptProcessor
        assertNotNull(processor, "Processor should not be null for 'php'");
        String filePath = System.getProperty("user.dir") + "\\test_scripting\\hello.php";
        try {
            String response = processor.processScript(filePath);
            assertNotNull(response);
            assertTrue(response.contains("\"output\":\"<h1>Hello from PHP!</h1>"));
            assertTrue(response.contains("\"exit_code\":0"), "Script should exit successfully");
        } catch (Exception e) {
            e.printStackTrace();
            fail("processScript threw an exception: " + e.getMessage());
        }

    }
}