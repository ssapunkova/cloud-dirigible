package test.com.sap.dirigible.repository.ext;

import java.io.ByteArrayOutputStream;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.sap.dirigible.repository.ext.command.Piper;
import com.sap.dirigible.repository.ext.command.ProcessUtils;

public class ProcessTest {
	
	@Test
	public void testCommand() throws IOException {
		
		String[] args = null; 
				
		String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("win") >= 0) {
            // Windows Commands
        	args = new String[]{"cmd","/c","dir"}; //windows
        } else {
            // Linux Commands
        	args = new String[]{"bash","-c","ls"}; //windows
        }
		
				
		executeCommand(args);
	}

	private void executeCommand(String[] args) throws IOException {
		if (args.length <= 0) {
			System.err.println("Need command to run");
		}

		ProcessBuilder processBuilder = ProcessUtils.createProcess(args);
		ProcessUtils.addEnvironmentVariables(processBuilder, null);
		ProcessUtils.removeEnvironmentVariables(processBuilder, null);
		//processBuilder.directory(new File(workingDirectory));
		processBuilder.redirectErrorStream(true);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Process process = ProcessUtils.startProcess(args, processBuilder);
		Piper pipe = new Piper(process.getInputStream(), out);
        new Thread(pipe).start();
        // Wait for second process to finish
        try {
			process.waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(new String(out.toByteArray()));
	}

	@Test
	public void testSplit()  {
		String command  = "bash \"-c\" \"ps -ef\"";
		String[] args = ProcessUtils.translateCommandline(command);
		assertNotNull(args);
		assertEquals("bash", args[0]);
		assertEquals("-c", args[1]);
		assertEquals("ps -ef", args[2]);
	}
		
}
