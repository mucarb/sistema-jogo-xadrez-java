package xadrez;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jogotabuleiro.Peca;
import jogotabuleiro.Posicao;
import jogotabuleiro.Tabuleiro;
import xadrez.pecas.Bispo;
import xadrez.pecas.Cavalo;
import xadrez.pecas.Peao;
import xadrez.pecas.Rainha;
import xadrez.pecas.Rei;
import xadrez.pecas.Torre;

public class PartidaXadrez {

	private int turno;
	private Cor jogadorAtual;
	private Tabuleiro tabuleiro;
	private boolean check;
	private boolean checkMate;
	private PecaXadrez enPassantVulneravel;
	private PecaXadrez promovido;

	private List<Peca> pecasNoTabuleiro = new ArrayList<>();
	private List<Peca> pecasCapturadas = new ArrayList<>();

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

	public boolean getCheck() {
		return check;
	}

	public boolean getCheckMate() {
		return checkMate;
	}

	public PecaXadrez getEnPassantVulneravel() {
		return enPassantVulneravel;
	}

	public PecaXadrez getPromovido() {
		return promovido;
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

		if (testeCheck(jogadorAtual)) {
			desfazerMovimento(origem, destino, pecaCapturada);

			throw new XadrezException("Voc? n?o pode se colocar em check");
		}

		PecaXadrez pecaMovida = (PecaXadrez) tabuleiro.peca(destino);

		// #movimentoespecial promocao
		promovido = null;
		if (pecaMovida instanceof Peao) {
			if ((pecaMovida.getCor() == Cor.BRANCO && destino.getLinha() == 0)
					|| (pecaMovida.getCor() == Cor.PRETO && destino.getLinha() == 7)) {
				promovido = (PecaXadrez) tabuleiro.peca(destino);
				promovido = substituirPecaPromovida("D");
			}
		}

		check = (testeCheck(oponente(jogadorAtual))) ? true : false;

		if (testeCheckMate(oponente(jogadorAtual))) {
			checkMate = true;
		} else {
			proximoTurno();
		}

		// #movimentoespecial en passant
		if (pecaMovida instanceof Peao
				&& (destino.getLinha() == origem.getLinha() - 2 || destino.getLinha() == origem.getLinha() + 2)) {
			enPassantVulneravel = pecaMovida;
		} else {
			enPassantVulneravel = null;
		}

		return (PecaXadrez) pecaCapturada;
	}

	public PecaXadrez substituirPecaPromovida(String tipo) {
		if (promovido == null) {
			throw new IllegalStateException("N?o h? pe?a para ser promovida!");
		}

		if (!tipo.equals("B") && !tipo.equals("C") && !tipo.equals("T") && !tipo.equals("D")) {
			return promovido;
		}

		Posicao pos = promovido.getPosicaoXadrez().posicionar();
		Peca p = tabuleiro.removerPeca(pos);

		pecasNoTabuleiro.remove(p);

		PecaXadrez novaPeca = novaPeca(tipo, promovido.getCor());

		tabuleiro.colocarPeca(novaPeca, pos);
		pecasNoTabuleiro.add(novaPeca);

		return novaPeca;
	}

	private PecaXadrez novaPeca(String tipo, Cor cor) {
		if (tipo.equals("B")) {
			return new Bispo(tabuleiro, cor);
		}

		if (tipo.equals("C")) {
			return new Cavalo(tabuleiro, cor);
		}

		if (tipo.equals("D")) {
			return new Rainha(tabuleiro, cor);
		}

		return new Torre(tabuleiro, cor);
	}

	private Peca mover(Posicao origem, Posicao destino) {
		PecaXadrez p = (PecaXadrez) tabuleiro.removerPeca(origem);
		p.aumentarContagemDeMovimento();

		Peca pecaCapturada = tabuleiro.removerPeca(destino);

		tabuleiro.colocarPeca(p, destino);

		if (pecaCapturada != null) {
			pecasNoTabuleiro.remove(pecaCapturada);
			pecasCapturadas.add(pecaCapturada);
		}

		// #movimentoespecial castling torre ao lado do rei
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() + 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() + 3);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removerPeca(origemT);

			tabuleiro.colocarPeca(torre, destinoT);
			torre.aumentarContagemDeMovimento();
		}

		// #movimentoespecial castling torre ao lado da rainha/dama
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() - 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() - 4);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() - 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removerPeca(origemT);

			tabuleiro.colocarPeca(torre, destinoT);
			torre.aumentarContagemDeMovimento();
		}

		// #movimentoespecial en passant
		if (p instanceof Peao) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == null) {
				Posicao posicaoPeao;

				if (p.getCor() == Cor.BRANCO) {
					posicaoPeao = new Posicao(destino.getLinha() + 1, destino.getColuna());
				} else {
					posicaoPeao = new Posicao(destino.getLinha() - 1, destino.getColuna());
				}

				pecaCapturada = tabuleiro.removerPeca(posicaoPeao);
				pecasCapturadas.add(pecaCapturada);
				pecasNoTabuleiro.remove(pecaCapturada);
			}
		}

		return pecaCapturada;
	}

	private void desfazerMovimento(Posicao origem, Posicao destino, Peca pecaCapturada) {
		PecaXadrez p = (PecaXadrez) tabuleiro.removerPeca(destino);
		p.diminuirContagemDeMovimento();

		tabuleiro.colocarPeca(p, origem);

		if (pecaCapturada != null) {
			tabuleiro.colocarPeca(pecaCapturada, destino);
			pecasCapturadas.remove(pecaCapturada);
			pecasNoTabuleiro.add(pecaCapturada);
		}

		// #movimentoespecial castling torre ao lado do rei (roque pequeno)
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() + 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() + 3);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() + 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removerPeca(destinoT);

			tabuleiro.colocarPeca(torre, origemT);
			torre.diminuirContagemDeMovimento();
		}

		// #movimentoespecial castling torre ao lado da rainha/dama (roque grande)
		if (p instanceof Rei && destino.getColuna() == origem.getColuna() - 2) {
			Posicao origemT = new Posicao(origem.getLinha(), origem.getColuna() - 4);
			Posicao destinoT = new Posicao(origem.getLinha(), origem.getColuna() - 1);
			PecaXadrez torre = (PecaXadrez) tabuleiro.removerPeca(destinoT);

			tabuleiro.colocarPeca(torre, origemT);
			torre.diminuirContagemDeMovimento();
		}

		// #movimentoespecial en passant
		if (p instanceof Peao) {
			if (origem.getColuna() != destino.getColuna() && pecaCapturada == enPassantVulneravel) {
				PecaXadrez peao = (PecaXadrez) tabuleiro.removerPeca(destino);

				Posicao posicaoPeao;

				if (p.getCor() == Cor.BRANCO) {
					posicaoPeao = new Posicao(3, destino.getColuna());
				} else {
					posicaoPeao = new Posicao(4, destino.getColuna());
				}

				tabuleiro.colocarPeca(peao, posicaoPeao);
			}
		}

	}

	private void validarPosicaoOrigem(Posicao posicao) {
		if (!tabuleiro.haUmaPeca(posicao)) {
			throw new XadrezException("N?o h? pe?a na posi??o de origem");
		}

		if (jogadorAtual != ((PecaXadrez) tabuleiro.peca(posicao)).getCor()) {
			throw new XadrezException("A pe?a escolhida n?o ? sua!");
		}

		if (!tabuleiro.peca(posicao).existeAlgumMovimentoPossivel()) {
			throw new XadrezException("N?o existe movimentos poss?veis para a pe?a escolhida");
		}
	}

	private void validarPosicaoDestino(Posicao origem, Posicao destino) {
		if (!tabuleiro.peca(origem).movimentoPossivel(destino)) {
			throw new XadrezException("A pe?a escolhida n?o pode se mover para a posi??o de destino");
		}
	}

	private void proximoTurno() {
		turno++;
		jogadorAtual = (jogadorAtual == Cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
	}

	private Cor oponente(Cor cor) {
		return (cor == Cor.BRANCO) ? Cor.PRETO : Cor.BRANCO;
	}

	private PecaXadrez rei(Cor cor) {
		List<Peca> lista = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == cor)
				.collect(Collectors.toList());

		for (Peca p : lista) {
			if (p instanceof Rei) {
				return (PecaXadrez) p;
			}
		}

		throw new IllegalStateException("N?o existe o rei " + cor + " no tabuleiro!");
	}

	private boolean testeCheck(Cor cor) {
		Posicao posicaoDoRei = rei(cor).getPosicaoXadrez().posicionar();

		List<Peca> pecasDoOponente = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == oponente(cor))
				.collect(Collectors.toList());

		for (Peca p : pecasDoOponente) {
			boolean[][] mat = p.movimentosPossiveis();

			if (mat[posicaoDoRei.getLinha()][posicaoDoRei.getColuna()]) {
				return true;
			}
		}

		return false;
	}

	private boolean testeCheckMate(Cor cor) {
		if (!testeCheck(cor)) {
			return false;
		}

		List<Peca> lista = pecasNoTabuleiro.stream().filter(x -> ((PecaXadrez) x).getCor() == cor)
				.collect(Collectors.toList());

		for (Peca p : lista) {
			boolean[][] mat = p.movimentosPossiveis();

			for (int i = 0; i < tabuleiro.getLinhas(); i++) {
				for (int j = 0; j < tabuleiro.getColunas(); j++) {
					if (mat[i][j]) {
						Posicao origem = ((PecaXadrez) p).getPosicaoXadrez().posicionar();
						Posicao destino = new Posicao(i, j);
						Peca pecaCapturada = mover(origem, destino);
						boolean testeCheck = testeCheck(cor);

						desfazerMovimento(origem, destino, pecaCapturada);

						if (!testeCheck) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	private void colocandoNovaPeca(char coluna, int linha, PecaXadrez pecaXadrez) {
		tabuleiro.colocarPeca(pecaXadrez, new PosicaoXadrez(coluna, linha).posicionar());

		pecasNoTabuleiro.add(pecaXadrez);
	}

	private void configuracaoInicial() {
		colocandoNovaPeca('a', 1, new Torre(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('b', 1, new Cavalo(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('c', 1, new Bispo(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('d', 1, new Rainha(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('e', 1, new Rei(tabuleiro, Cor.BRANCO, this));
		colocandoNovaPeca('f', 1, new Bispo(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('g', 1, new Cavalo(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('h', 1, new Torre(tabuleiro, Cor.BRANCO));
		colocandoNovaPeca('a', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		colocandoNovaPeca('b', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		colocandoNovaPeca('c', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		colocandoNovaPeca('d', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		colocandoNovaPeca('e', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		colocandoNovaPeca('f', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		colocandoNovaPeca('g', 2, new Peao(tabuleiro, Cor.BRANCO, this));
		colocandoNovaPeca('h', 2, new Peao(tabuleiro, Cor.BRANCO, this));

		colocandoNovaPeca('a', 8, new Torre(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('b', 8, new Cavalo(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('c', 8, new Bispo(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('d', 8, new Rainha(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('e', 8, new Rei(tabuleiro, Cor.PRETO, this));
		colocandoNovaPeca('f', 8, new Bispo(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('g', 8, new Cavalo(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('h', 8, new Torre(tabuleiro, Cor.PRETO));
		colocandoNovaPeca('a', 7, new Peao(tabuleiro, Cor.PRETO, this));
		colocandoNovaPeca('b', 7, new Peao(tabuleiro, Cor.PRETO, this));
		colocandoNovaPeca('c', 7, new Peao(tabuleiro, Cor.PRETO, this));
		colocandoNovaPeca('d', 7, new Peao(tabuleiro, Cor.PRETO, this));
		colocandoNovaPeca('e', 7, new Peao(tabuleiro, Cor.PRETO, this));
		colocandoNovaPeca('f', 7, new Peao(tabuleiro, Cor.PRETO, this));
		colocandoNovaPeca('g', 7, new Peao(tabuleiro, Cor.PRETO, this));
		colocandoNovaPeca('h', 7, new Peao(tabuleiro, Cor.PRETO, this));
	}

}
