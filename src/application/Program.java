package application;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import xadrez.PartidaXadrez;
import xadrez.PecaXadrez;
import xadrez.PosicaoXadrez;
import xadrez.XadrezException;

public class Program {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		PartidaXadrez partidaXadrez = new PartidaXadrez();

		List<PecaXadrez> capturadas = new ArrayList<>();

		while (!partidaXadrez.getCheckMate()) {
			try {
				UI.limparTela();
				UI.mostrarPartida(partidaXadrez, capturadas);

				System.out.println();
				System.out.print("Origem: ");
				PosicaoXadrez origem = UI.lerPosicaoXadrez(sc);

				boolean[][] movimentosPossiveis = partidaXadrez.movimentosPossiveis(origem);
				UI.limparTela();
				UI.mostrarTabuleiro(partidaXadrez.getPecas(), movimentosPossiveis);

				System.out.println();
				System.out.print("Destino: ");
				PosicaoXadrez destino = UI.lerPosicaoXadrez(sc);

				PecaXadrez pecaCapturada = partidaXadrez.executarMovimentoXadrez(origem, destino);

				if (pecaCapturada != null) {
					capturadas.add(pecaCapturada);
				}
				
				if(partidaXadrez.getPromovido() != null) {
					System.out.print("Informe peça para promoção (B/C/T/D): ");
					String tipo = sc.nextLine();
					
					partidaXadrez.substituirPecaPromovida(tipo);
				}
			} catch (XadrezException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			} catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		}
		
		UI.limparTela();
		UI.mostrarPartida(partidaXadrez, capturadas);
	}

}
