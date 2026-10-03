package tarefas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Representa uma tarefa com título, descrição, prioridade, status e prazo opcional.
 * <p>
 * Também sabe se converter de/para uma linha de texto, usada na persistência em arquivo.
 */
public final class Tarefa {

    /** Formato de data exibido e aceito na interface (dd/MM/yyyy). */
    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String SEPARADOR = ";";
    private static final int TOTAL_CAMPOS = 6;

    private final int id;
    private String titulo;
    private String descricao;
    private Prioridade prioridade;
    private Status status;
    private LocalDate prazo; // null = sem prazo

    /** Cria uma nova tarefa com status {@link Status#PENDENTE}. */
    public Tarefa(int id, String titulo, String descricao, Prioridade prioridade, LocalDate prazo) {
        this(id, titulo, descricao, prioridade, Status.PENDENTE, prazo);
    }

    public Tarefa(int id, String titulo, String descricao, Prioridade prioridade, Status status, LocalDate prazo) {
        this.id = id;
        setTitulo(titulo);
        setDescricao(descricao);
        this.prioridade = prioridade;
        this.status = status;
        this.prazo = prazo;
    }

    /** Uma tarefa está atrasada se tem prazo, ainda não foi concluída e o prazo já passou. */
    public boolean estaAtrasada() {
        return prazo != null && status != Status.CONCLUIDA && prazo.isBefore(LocalDate.now());
    }

    // ---------------------------------------------------------------
    // Persistência em linha de texto
    // Formato: id;titulo;descricao;PRIORIDADE;STATUS;aaaa-mm-dd
    // ---------------------------------------------------------------

    /** Converte a tarefa em uma linha de texto para gravação no arquivo. */
    public String paraLinha() {
        return String.join(SEPARADOR,
                String.valueOf(id),
                escapar(titulo),
                escapar(descricao),
                prioridade.name(),
                status.name(),
                prazo == null ? "" : prazo.toString());
    }

    /**
     * Reconstrói uma tarefa a partir de uma linha gravada por {@link #paraLinha()}.
     *
     * @throws IllegalArgumentException se a linha estiver em formato inválido
     */
    public static Tarefa deLinha(String linha) {
        String[] campos = linha.split(SEPARADOR, -1);
        if (campos.length != TOTAL_CAMPOS) {
            throw new IllegalArgumentException("esperados " + TOTAL_CAMPOS + " campos, encontrados " + campos.length);
        }
        LocalDate prazo = campos[5].isEmpty() ? null : LocalDate.parse(campos[5]);
        return new Tarefa(
                Integer.parseInt(campos[0]),
                desescapar(campos[1]),
                desescapar(campos[2]),
                Prioridade.valueOf(campos[3]),
                Status.valueOf(campos[4]),
                prazo);
    }

    /** Protege caracteres especiais para que não quebrem o formato da linha. */
    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace(";", "\\p").replace("\n", "\\n");
    }

    private static String desescapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char proximo = s.charAt(++i);
                sb.append(switch (proximo) {
                    case 'p' -> ';';
                    case 'n' -> '\n';
                    default -> proximo;
                });
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        String prazoTxt = prazo == null ? "sem prazo" : prazo.format(FORMATO_DATA);
        String alerta = estaAtrasada() ? "  [ATRASADA]" : "";
        String detalhe = descricao.isEmpty() ? "" : System.lineSeparator() + "      " + descricao;
        return String.format("#%-3d [%-12s] %-5s | %s (prazo: %s)%s%s",
                id, status.getRotulo(), prioridade.getRotulo(), titulo, prazoTxt, alerta, detalhe);
    }

    // ---------------------------------------------------------------
    // Getters e setters
    // ---------------------------------------------------------------

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Prioridade getPrioridade() { return prioridade; }
    public Status getStatus() { return status; }
    public LocalDate getPrazo() { return prazo; }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título não pode ser vazio.");
        }
        this.titulo = titulo.trim();
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public void setStatus(Status status) { this.status = status; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }
}
