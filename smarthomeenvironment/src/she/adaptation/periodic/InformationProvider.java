package she.adaptation.periodic;

public class InformationProvider {

	// TODO: os valores da probabilidade de ocorrer o evento vem preenchido, por
	// padr�o, porem muda
	// com o decorrer da aplica��o, avaliar se os valores padr�es est�o correto.
	// Casso n�o carregue durante
	// a inicializa��o

	// Probabilidade de ocorrer o evento de add o sensor
	static public double removeP = 0.50;
	// Probabilidade de realizar o download do drive do sensor.
	static public double downloadP = 0.20;

	// taxa de falha do servi�o (instalar ou remover)
	// TODO: analisar se a matriz precisa de ser 2X5
	static public double[][] featureFailureRates = new double[3][5];

	// Tamanho da fila.
	// TODO: ainda n�o sei pra q server...
	static public int[][] queueLength = new int[3][5];

	// Tempo de servi�o
	static public double[][] serviceTimes = new double[3][5];

}
