package xadrez;

import jogotabuleiro.Peca;
import jogotabuleiro.Posicao;
import jogotabuleiro.Tabuleiro;

public abstract class PecaXadrez extends Peca {

	private Cor cor;
	private int contagemDeMovimento;

	public PecaXadrez(Tabuleiro tabuleiro, Cor cor) {
		super(tabuleiro);
		this.cor = cor;
	}

	public Cor getCor() {
		return cor;
	}

	public int getContagemDeMovimento() {
		return contagemDeMovimento;
	}

	public void aumentarContagemDeMovimento() {
		contagemDeMovimento++;
	}

	public void diminuirContagemDeMovimento() {
		contagemDeMovimento--;
	}

	public PosicaoXadrez getPosicaoXadrez() {
		return PosicaoXadrez.paraPosicao(posicao);
	}

	protected boolean existePecaAdversaria(Posicao posicao) {
		PecaXadrez p = (PecaXadrez) getTabuleiro().peca(posicao);

		return p != null && p.getCor() != cor;
	}

}
