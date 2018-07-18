package br.com.diegomunhoz.game;

import br.com.diegomunhoz.core.Game;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import javax.imageio.ImageIO;

// Esta é a classe que representa nosso jogo. Ela é derivada de "Game", que
// possui o motor do jogo e chama os métodos abaixo quando necessário.
// Ela também implementa a interfaces de ouvinte de teclado (KeyListener).
public class JogoQuedaLivre extends Game implements KeyListener {

    // Estados do jogo.
    // Indica que o jogador não saltou (está agarrado pelo pterodátilo).
    static final int EST_VOANDO = 0;
    // Indica que o jogador saltou, está caindo de paraquedas.
    static final int EST_CAINDO = 1;
    // Indica que o jogador caiu sobre o alvo (acertou).
    static final int EST_ACERTO = 2;
    // Indica que o jogador caiu fora do alvo (errou).
    static final int EST_ERRO = 3;
    // Indica que o jogo está no finalizado, mostrando uma mensagem.
    static final int EST_FINAL = 4;
    // Modelo do jogo.
    // Representa o estado atual do jogo.
    int estado;
    // Representa a posição do pterodátilo na tela.
    Point posPtero;
    // Representa a velocidade horizntal do pterodátilo.
    int velPtero;
    // Representa a posição do paraquedista (jogador).
    // É utilizado ponto flutuante para conseguir um controle exato da velocidade.
    Point2D.Float posJogador;
    // Representa as velocidades (horizontal e vertical) do paraquedista.
    Point2D.Float velJogador;
    // Representa a posição do alvo.
    Point posAlvo;
    // Representa a alrgura do alvo (a altura é fixa no código).
    int largAlvo;
    // Armazena a quantidade de pontos que o jogador fez.
    int pontos;
    // Armazena a quantidade de pontos que o jogador ainda tem.
    int tentativas;
    // Imagens, sons e fontes.
    BufferedImage imgCaindo;
    BufferedImage imgAcerto;
    BufferedImage imgErro;
    BufferedImage imgCenario;
    BufferedImage imgPterodatilo;
    AudioClip sndPterodatilo;
    AudioClip sndCaindo;
    AudioClip sndAcerto;
    AudioClip sndErro;
    Font fontCrimewave;
    // Utilitários.
    // Objeto para geração de números aleatoriamente.
    Random rnd;
    // Numéro arbitrário de milisegundos que se o jogo vai parar a cada
    // volta do game loop. Serve para reduzir a velocidade do jogo.
    int delay;
    // Implementação do Key Pooling para o teclado.
    HashMap<Integer, Boolean> keyPool;

    public JogoQuedaLivre() {
        // Adiciona esta classe como ouvinte do teclado.
        getMainWindow().addKeyListener(this);
        // Criação dos objetos.
        keyPool = new HashMap<Integer, Boolean>();
        rnd = new Random();
        posJogador = new Point2D.Float(0, 0);
        // A posição do pterodátilo é criada na altura 150px, número
        // que não vai mudar.
        posPtero = new Point(0, 50);
        velJogador = new Point2D.Float();
        velPtero = 0;
        // A posição do alvo é criada na altura 575px, número que não vai mudar.
        posAlvo = new Point(0, 575);
        largAlvo = 150;
        estado = EST_VOANDO;
        pontos = 0;
        tentativas = 5;
        delay = 25;
    }

    @Override
    public void onLoad() {
        try {
            // Carrega imagens, son e fontes.
            imgCenario = loadImage("/cenario.png");
            imgPterodatilo = loadImage("/pterodatilo.png");
            imgCaindo = loadImage("/caindo.png");
            imgAcerto = loadImage("/acerto.png");
            imgErro = loadImage("/erro.png");
            sndPterodatilo = Applet.newAudioClip(getClass().getResource(
                    "/aviao.wav"));
            sndCaindo = Applet.newAudioClip(getClass().getResource(
                    "/caindo.wav"));
            sndAcerto = Applet.newAudioClip(getClass().getResource(
                    "/acerto.wav"));
            sndErro = Applet.newAudioClip(
                    getClass().getResource("/erro.wav"));
            File tmp = new File(getClass().getResource("/CrimewaveBB.ttf").
                    toURI());
            fontCrimewave = Font.createFont(Font.TRUETYPE_FONT, tmp);
            // Determina a quantidade de tentativas.
            tentativas = 5;
            // Zera a pontuação.
            pontos = 0;
            // Executa a rotina que inicia a ação.
            runReinicio();
        } catch (FontFormatException ex) {
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    protected BufferedImage loadImage(String fileName) throws IOException {
        // Rotina auxiliar para carga das imagens.
        BufferedImage img = null;
        URL url = getClass().getResource(fileName);
        if (url == null) {
            throw new RuntimeException(
                    "A imagem " + fileName + " não foi encontrada.");
        } else {
            img = ImageIO.read(url);
        }
        return img;
    }

    @Override
    public void onUnload() {
        // Interrompe o som do pterodátilo
        // (que pode estar ocorrendo ao sair do jogo).
        sndPterodatilo.stop();
    }

    @Override
    public void onUpdate() {
        // Como sabemos está rotina é chamada a cada volta do game loop.
        // Como o código é extenso, ela foi dividida em rotinas menores.
        // Temos uma rotina para executar a lógica de cada estado do jogo.

        // A rotina abaixo é executada em todas voltas e controla a saída do jogo
        // (tecla ESC) e outras coisas gerais.
        runControleDoJogo();

        // Aqui começamos a testar os testados.
        if (estado == EST_FINAL) {
            // Se o estado é FINAL, executa a rotina abaixo e nenhuma outra.
            runEstadoFinal();
        } else {
            // Se não está no estado final, move o pterodátilo.
            runMoveAviao();
            // E verifica se está voando ou caindo.
            if (estado == EST_VOANDO) {
                runEstadoVoando();
            } else if (estado == EST_CAINDO) {
                runEstadoCaindo();
            }

        }
    }

    protected void runReinicio() {
        // Esta rotina dá os valores iniciais para os atributos, iniciando a ação.
        // Muda o estado para VOANDO.
        estado = EST_VOANDO;
        // Posiciona o pterodátilo 100px à esquerda (fora) da tela.
        // A posição vertical nunca muda.
        posPtero.x = -100;
        // Sorteia uma velocidade para o pterodátilo, entre 2px e 12px.
        velPtero = 2 + rnd.nextInt(10);
        // Sorteia a posição do alvo.
        // A posição vertical nunca muda.
        posAlvo.x = rnd.nextInt(600);
        // Pára o som atual do pterodátilo.
        sndPterodatilo.stop();
        // Inicia novamente o som do pterodátilo em loop.
        sndPterodatilo.loop();
    }

    protected void runControleDoJogo() {
        if (keyPool.get(KeyEvent.VK_ESCAPE) != null) {
            // Se a tecla ESC está pressionada, termina o jogo.
            terminate();
        }
        if (keyPool.get(KeyEvent.VK_UP) != null) {
            // Se a seta para cima está pressionada, diminui o delay.
            if (delay > 1) {
                // Só diminui se for maior que 1.
                delay--;
            }
        }
        if (keyPool.get(KeyEvent.VK_DOWN) != null) {
            // Se a seta para baixo está pressionada, aumenta o delay.
            if (delay < 100) {
                // Só aumenta se for menor que 100.
                delay++;
            }
        }
        try {
            // Faz o programa parar de executar durante delay milisegundos.
            // Isto serve para reduzir a velocidade em máquinas muito rápidas.
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
        }
    }

    protected void runMoveAviao() {
        // Atualiza a posição do pterodátilo conforme a velocidade.
        posPtero.x += velPtero;
        if (posPtero.x > getWidth() + 100) {
            // Se a posição do pterodátilo ultrapassa a largura da tela mais 100px,
            // chama a rotina para reiniciar a ação.
            runReinicio();
        }
    }

    protected void runEstadoFinal() {
        if (keyPool.get(KeyEvent.VK_SPACE) != null) {
            // Se a tecla ENTER está pressionada, recarrega o jogo.
            onLoad();
        }
        // Apesar deste estado ficar observando apenas a tecla ENTER, temos a
        // rotina runControleDoJogo, que aguarda ESC e executa o tempo todo.
        // Isso permite que, ao invés de reiniciar o jogo, a pessoa saia do mesmo.
    }

    protected void runEstadoVoando() {
        // Posiciona o jogador relativo à posição do pterodátilo.
        // Os valores abaixo foram escolhidos de forma a parecer que o jogador
        // é carregado pelo pterodátilo.
        posJogador.x = posPtero.x + 50;
        posJogador.y = posPtero.y + 90;
        if (keyPool.get(KeyEvent.VK_SPACE) != null) {
            // Se tecla ESPACO está pressionada, inicia o salto.
            // A velocidade horizontal do jogador é 10% maiopr que a velocidade
            // atual do pterodátilo. Quanto mais rápido o pterodátilo, mais
            // rápido o jogador.
            velJogador.x = velPtero * 1.1f;
            // A velocidade vertical (queda) do jogador é iniciada com 5px.
            velJogador.y = 5;
            // Muda o estado para CAINDO.
            estado = EST_CAINDO;
            // Inicia o som de caindo.
            sndCaindo.play();
        }
    }

    protected void runEstadoCaindo() {
        // Atualiza a posição do jogador com base em sua velocidade.
        posJogador.x += velJogador.x;
        posJogador.y += velJogador.y;
        if (velJogador.x > 0) {
            // Se a velocidade do jogador é maior do que zero,
            // diminui ela um pouco. Valor arbitrário.
            // Isso faz com que a valocidade horizontal seja freada durante a queda,
            // porém evitando de ficar negativa (caso em que o jogador
            // voaria para trás)
            velJogador.x -= 0.2f;
        }
        // A velocidade vertical é sempre aumentada um pouco. Valor arbitrário.
        velJogador.y += 0.2f;
        if (posJogador.y > posAlvo.y) {
            // Se apoisção vertical do jogador pasou da altura do alvo na tela,
            // então o jogador chegou ao chão.
            // Interrompe o som de caindo.
            sndCaindo.stop();
            // Ajuda a posição vertical para fica exatamente na linha do alvo.
            posJogador.y = posAlvo.y;
            // Verificação se o jogador está dentro do alvo.
            // Para isso sua posição horizontal deve estar entre a esquerda e a
            // direita do mesmo. A posição direita do alvo é obtida somando sua
            // largura à posição esquerda.
            if (posAlvo.x < posJogador.x && posJogador.x < posAlvo.x
                    + largAlvo) {
                // Se está sobre o alvo, muda o estado para acerto.
                estado = EST_ACERTO;
                // Executa o som de acerto.
                sndAcerto.play();
                // Adiciona 10 pontos.
                pontos += 10;
            } else {
                // Se está fora do alvo, muda o estado para erro.
                estado = EST_ERRO;
                // Executa o som de erro.
                sndErro.play();
                // Diminui uma tentativa.
                tentativas--;
                if (tentativas == 0) {
                    // Se as tentativas chegaram a zero, muda o estado para final.
                    estado = EST_FINAL;
                    // Interrompe o som do pterodátilo.
                    sndPterodatilo.stop();
                    // Posiciona o avimão fora da tela, para não fica aparecendo
                    // na tela de final;
                    posPtero.x = getWidth();
                }
            }
        }
    }

    @Override
    public void onRender(Graphics2D g) {
        // Esta rotina é chamada a cada volta do game loop.
        // Foi dividida em rotinas conforme a parte a ser desenhada.
        // Desenha o cenário, pterodátilo, jogador e alvo.
        renderJogo(g);
        // Desenha o Heads Up Display, que são as informações do jogo na tela.
        renderHUD(g);
        if (estado == EST_FINAL) {
            // Se está no estado final, desenha a mensagem de final.
            renderMensagemFinal(g);
        }
    }

    protected void renderJogo(Graphics2D g) {
        // Desenha a imagem de fundo.
        g.drawImage(imgCenario, 0, 0, null);
        // Muda a cor para branco.
        g.setColor(Color.white);
        // Especifica uma composição com alpha de 30% (ou seja, 70% de transparência)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                0.3f));
        // Preenche uma elipse na posição do alvo, observando sua largura e com
        // altura fixa de 10px. Será preenchida com a transparência acima.
        g.fillOval(posAlvo.x, posAlvo.y - 2, largAlvo, 10);
        // Especifica a composição normal (sem transparência).
        g.setPaintMode();
        // Desenha uma elipse na posição do alvo. Agorta é apenas o contorno.
        g.drawOval(posAlvo.x, posAlvo.y - 2, largAlvo, 10);
        // Declara um objeto para apontar para uma das imagens do jogador.
        BufferedImage img = null;
        switch (estado) {
            case EST_VOANDO:
                // Se esta no estado voando, não aponta para imagem alguma.
                img = imgCaindo;
                break;
            case EST_CAINDO:
                // Se está no estado caindo, aponta para a imagem do paraquedas.
                img = imgCaindo;
                break;
            case EST_ACERTO:
                // Se está no estado acerto, aponta para a imagem corespondente.
                img = imgAcerto;
                break;
            case EST_ERRO:
                // Se está no estado erro, aponta para a imagem corespondente.
                img = imgErro;
                break;
        }
        if (img != null) {
            // Se img está apontando para alguma imagem, desenha ela na posição
            // do jogador. É calculada a posição de forma a centralizar
            // horizontalmente a imagem na posição.
            g.drawImage(img, (int) posJogador.x - img.getWidth() / 2,
                    (int) posJogador.y - img.getHeight(), null);
        }
        // Desenha a imagem do pterodátilo na sua posição.
        g.drawImage(imgPterodatilo, posPtero.x, posPtero.y, null);
    }

    protected void renderMensagemFinal(Graphics2D g) {
        // Muda a cor para vermelho.
        g.setColor(Color.red);
        // Muda a fonte para uma derivada de tamanho 44.
        g.setFont(fontCrimewave.deriveFont(Font.BOLD, 44f));
        // Escreve as mensagens na tela, em posições determinadas.
        g.drawString("Terminaram suas tentativas!", 120, 200);
        g.drawString("Voce fez " + pontos + " pontos.", 200, 250);
        // Muda a cor para branco.
        g.setColor(Color.white);
        // Muda a fonte para uma derivada de tamanho 24.
        g.setFont(fontCrimewave.deriveFont(Font.BOLD, 24f));
        // Escreve a mensagem sobre continuar o jogo.
        g.drawString("PRESSIONE [ESPAÇO] PARA JOGAR NOVAMENTE", 150, 300);
    }

    protected void renderHUD(Graphics2D g) {
        // Muda a cor para amarelo.
        g.setColor(Color.yellow);
        // Muda a fonte para uma derivada de tamanho 44.
        g.setFont(fontCrimewave.deriveFont(Font.BOLD, 44f));
        // Escreve as informações na tela (pontos e tentativas).
        g.drawString(" PONTOS", getWidth() - 180, 40);
        g.drawString("" + pontos, getWidth() - 240, 40);
        g.drawString(tentativas + " TENTATIVAS", 20, 40);
    }

    public void keyTyped(KeyEvent e) {
        // Rotina não utilizada. Evento de tecla teclada.
    }

    public void keyPressed(KeyEvent e) {
        // Quando uma tecla é pressionada, adiciona ela no pool.
        keyPool.put(e.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent e) {
        // Quando uma tecla é liberada, remove ela do pool.
        keyPool.remove(e.getKeyCode());
    }
}
