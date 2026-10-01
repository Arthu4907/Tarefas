package tarefas;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Tarefa {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int id;
    private String titulo;
    private String descricao;
    private Prioridade prioridade;
    private Status status;
    private LocalDate prazo; // pode ser null

    public Tarefa(int id, String titulo, String descricao, Prioridade prioridade, LocalDate prazo) {
        this(id, titulo, descricao, prioridade, Status.PENDENTE, prazo);
    }

    public Tarefa(int id, String titulo, String descricao, Prioridade prioridade, Status status, LocalDate prazo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título não pode ser vazio.");
        }
        this.id = id;
        this.titulo = titulo.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
        this.prioridade = prioridade;
        this.status = status;
        this.prazo = prazo;
    }

    public boolean estaAtrasada() {
        return prazo != null && status != Status.CONCLUIDA && prazo.isBefore(LocalDate.now());
    }

    // --- Persistência simples em linha de texto (separador ;) ---
    public String paraLinha() {
        return String.join(";",
                String.valueOf(id),
                escapar(titulo),
                escapar(descricao),
                prioridade.name(),
                status.name(),
                prazo == null ? "" : prazo.toString());
    }

    public static Tarefa deLinha(String linha) {
        String[] p = linha.split(";", -1);
        LocalDate prazo = p[5].isEmpty() ? null : LocalDate.parse(p[5]);
        return new Tarefa(Integer.parseInt(p[0]), desescapar(p[1]), desescapar(p[2]),
                Prioridade.valueOf(p[3]), Status.valueOf(p[4]), prazo);
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace(";", "\\p").replace("\n", "\\n");
    }

    private static String desescapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                sb.append(n == 'p' ? ';' : n == 'n' ? '\n' : n);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        String prazoTxt = prazo == null ? "sem prazo" : prazo.format(FORMATO);
        String alerta = estaAtrasada() ? "  ⚠ ATRASADA" : "";
        return String.format("#%-3d [%-12s] %-5s | %s (prazo: %s)%s%s",
                id, status, prioridade, titulo, prazoTxt, alerta,
                descricao.isEmpty() ? "" : "\n      " + descricao);
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Prioridade getPrioridade() { return prioridade; }
    public Status getStatus() { return status; }
    public LocalDate getPrazo() { return prazo; }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) throw new IllegalArgumentException("O título não pode ser vazio.");
        this.titulo = titulo.trim();
    }
    public void setDescricao(String descricao) { this.descricao = descricao == null ? "" : descricao.trim(); }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public void setStatus(Status status) { this.status = status; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }
}
