package xadrez;

import jogotabuleiro.Peca;
import jogotabuleiro.Posicao;
import jogotabuleiro.Tabuleiro;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {

	private int turno;
	private Cor jogadorAtual;
	private Tabuleiro tabuleiro;

	public PartidaXadrez() {
		tabuleiro = new Tabuleiro(8, 8);
		turno = 1;
		jogadorAtual = Cor.BRANCO;
		configuracaoInicial();
	}

	public int getTurno() {
		return turno;
	}

	public Cor getJogadorAtual() {
		return jogadorAtual;
	}

	public PecaXadrez[][] getPecas() {
		PecaXadrez[][] mat = new PecaXadrez[tabuleiro.getLinhas()][tabuleiro.getColunas()];

		for (int i = 0; i < tabuleiro.getLinhas(); i++) {
			for (int j = 0; j < tabuleiro.getColunas(); j++) {
				mat[i][j] = (PecaXadrez) tabuleiro.peca(i, j);
			}
		}

		return mat;
	}

	public boolean[][] movimentosPossiveis(PosicaoXadrez posicaoOrigem) {
		Posicao posicao = posicaoOrigem.posicionar();
		validarPosicaoOrigem(posicao);

		return tabuleiro.peca(posicao).movimentosPossiveis();
	}

	public PecaXadrez executarMovimentoXadrez(PosicaoXadrez posicaoOrigem, PosicaoXadrez posicaoDestino) {
		Posicao origem = posicaoOrigem.posicionar();
		Posicao destino = posicaoDestino.posicionar();

		validarPosicaoOrigem(origem);
		validarPosicaoDestino(origem, destino);

		Peca pecaCapturada = mover(origem, destino);

		proximoTurno();

		return (PecaXadrez) pecaCapturada;
	}

	private Peca mover(Posicao origem, Posicao destino) {
		Peca p = tabuleiro.removerPeca(origem);
		Peca pecaCapturada = tabuleiro.removerPeca(destino);

		tabuleiro.colocarPeca(p, destino);

		return pecaCapturada;
	}

	private void validarPosicaoOrigem(Posicao posicao) {
		if (!tabuleiro.haUmaPeca(posicao)) {
			throw new XadrezException("Não há peça na posição de origem");
		}

		if (jogadorAtual != ((PecaXadrez) tabuleiro.peca(posicao)).getCor()) {
			throw new XadrezException("A peça escolhida não é sua!");
		}

		if (!tabuleiro.peca(posicao).existeAlgumMovimentoPossivel()) {
			throw new XadrezException("Não existe movimentos possíveis para a peça escolhida");
		}
	}

	private void validarPosicaoDestino(Posicao origem, Posicao destino) {
		if (!tabuleiro.peca(origem).movimentoPossivel(destino)) {
			throw new XadrezException("A peça escolhida não pode se mover para a posição de destino");
		}
	}

	private void proximoTurno() {
		turno++;
		jogadorAtual = (jogadorAtual == Cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
	}

	private void colocandoNovaPeca(char coluna, int linha, PecaXadrez pecaXadrez) {
		tabuleiro.colocarPeca(pecaXadrez, new PosicaoXadrez(coluna, linha).posicionar());
	}

	private void configuracaoInicial() {
		colocandoNovaPeca('c', 1, new Torre(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('c', 2, new Torre(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('d', 2, new Torre(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('e', 2, new Torre(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('e', 1, new Torre(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('d', 1, new Rei(tabuleiro, Cor.BRANCO));

		colocandoNovaPeca('c', 7, new Torre(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('c', 8, new Torre(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('d', 7, new Torre(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('e', 7, new Torre(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('e', 8, new Torre(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('d', 8, new Rei(tabuleiro, Cor.PRETO));
	}

}
