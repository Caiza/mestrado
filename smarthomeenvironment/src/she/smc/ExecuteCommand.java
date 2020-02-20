package she.smc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/*
 * Classe com a implementação da interface Callable, normalmente é usado no FutureTask.
 * FutureTask é o processamento em paralelo com tipo de retorno defino, diferente da Thread da java
 * que não devolve nada.
 * 
 * O método call(), básicamente executa um comando no prompt e le o seu resultado linha a linha e coloca em uma string.
 * Serve diretamente para a leitura dos dados que o verify retorna no pompt.
 * 
 */
public class ExecuteCommand implements Callable<String> {
	
	String	command;
	
	public ExecuteCommand(String cmd) {
		this.command = cmd;
	}
	
	@Override
	public String call() throws Exception {
		StringBuffer output = new StringBuffer();
		
		Process p;
		try {
			
			p = Runtime.getRuntime().exec(command);
			//p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			
			if (output.length() == 0) {
				while ((line = errorReader.readLine()) != null) {
					output.append(line + "\n");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
		return output.toString();
	}
	
}
