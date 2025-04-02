package com.webserver.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class PhpScriptProcessorTest {
    @Test
    public void testCreateJSONInput(){
        PhpScriptProcessor processor = new PhpScriptProcessor();
        String input = "Hello World";
        String jsonOutput = processor.createJSONInput(input);
        assertEquals("{\"code\": \"Hello World\"}", jsonOutput);
    }
    @Test
    public void testRunSimplePHPScript() {
        PhpScriptProcessor processor = new PhpScriptProcessor();
        String filePath = System.getProperty("user.dir") + "\\test_scripting\\hello.php";
        String response = processor.processScript(filePath);
        assertNotNull(response);
        assertTrue(response.contains("\"output\":\"<h1>Hello from PHP!</h1>"));
        assertTrue(response.contains("\"exit_code\":0"), "Script should exit successfully");
    }
    @Test
    public void testNotPHPScript() {
        PhpScriptProcessor processor = new PhpScriptProcessor();
        String filePath = System.getProperty("user.dir") + "\\test_scripting\\not_valid_php.php";
        String response = processor.processScript(filePath);
        assertNotNull(response);
        assertTrue(response.contains("unexpected double-quoted string"), "Response should not run because of invalid php");
        assertTrue(response.contains("\"exit_code\":"), "Script should exit unsuccessfully");
    }
    @Test
    public void testEmptyPhpScript() {
        PhpScriptProcessor processor = new PhpScriptProcessor();
        String filePath = System.getProperty("user.dir") + "\\test_scripting\\empty.php";
        String response = processor.processScript(filePath);
        assertNotNull(response);
        assertTrue(response.contains("\"error\":\"Missing PHP code\""));
        assertTrue(response.contains("\"exit_code\":1"), "Script should exit unsuccessfully");
    }
    @Test
    public  void testTimeoutPhpScript(){
        PhpScriptProcessor processor = new PhpScriptProcessor();
        String filePath = System.getProperty("user.dir") + "\\test_scripting\\timeout.php";
        String response = processor.processScript(filePath);
        assertNotNull(response);
        assertTrue(response.contains("\"error\":\"PHP script timed out after 10 seconds\""));
        assertTrue(response.contains("\"exit_code\":124"), "Script should exit unsuccessfully");

    }

}
