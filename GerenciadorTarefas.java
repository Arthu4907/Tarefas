package tarefas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class GerenciadorTarefas {
    private final Map<Integer, Tarefa> tarefas = new LinkedHashMap<>();
    private final Path arquivo;
    private int proximoId = 1;

    public GerenciadorTarefas(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    public Tarefa adicionar(String titulo, String descricao, Prioridade prioridade, LocalDate prazo) {
        Tarefa t = new Tarefa(proximoId++, titulo, descricao, prioridade, prazo);
        tarefas.put(t.getId(), t);
        salvar();
        return t;
    }

    public Optional<Tarefa> buscar(int id) {
        return Optional.ofNullable(tarefas.get(id));
    }

    public boolean remover(int id) {
        boolean removida = tarefas.remove(id) != null;
        if (removida) salvar();
        return removida;
    }

    public boolean alterarStatus(int id, Status status) {
        Tarefa t = tarefas.get(id);
        if (t == null) return false;
        t.setStatus(status);
        salvar();
        return true;
    }

    /** Chame depois de editar uma tarefa obtida por buscar(). */
    public void atualizar() {
        salvar();
    }

    /** Ordena: não concluídas primeiro, depois maior prioridade, depois prazo mais próximo. */
    public List<Tarefa> listarTodas() {
        Comparator<Tarefa> ordem = Comparator
                .comparing((Tarefa t) -> t.getStatus() == Status.CONCLUIDA)
                .thenComparing(Tarefa::getPrioridade, Comparator.reverseOrder())
                .thenComparing(Tarefa::getPrazo, Comparator.nullsLast(Comparator.naturalOrder()));
        return tarefas.values().stream().sorted(ordem).collect(Collectors.toList());
    }

    public List<Tarefa> filtrarPorStatus(Status status) {
        return listarTodas().stream().filter(t -> t.getStatus() == status).toList();
    }

    public List<Tarefa> filtrarPorPrioridade(Prioridade p) {
        return listarTodas().stream().filter(t -> t.getPrioridade() == p).toList();
    }

    public List<Tarefa> atrasadas() {
        return listarTodas().stream().filter(Tarefa::estaAtrasada).toList();
    }

    public List<Tarefa> pesquisar(String termo) {
        String q = termo.toLowerCase();
        return listarTodas().stream()
                .filter(t -> t.getTitulo().toLowerCase().contains(q)
                        || t.getDescricao().toLowerCase().contains(q))
                .toList();
    }

    public Map<Status, Long> resumo() {
        Map<Status, Long> r = new EnumMap<>(Status.class);
        for (Status s : Status.values()) r.put(s, 0L);
        tarefas.values().forEach(t -> r.merge(t.getStatus(), 1L, Long::sum));
        return r;
    }

    // --- Persistência ---
    private void carregar() {
        if (!Files.exists(arquivo)) return;
        try {
            for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
                if (linha.isBlank()) continue;
                Tarefa t = Tarefa.deLinha(linha);
                tarefas.put(t.getId(), t);
                proximoId = Math.max(proximoId, t.getId() + 1);
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Aviso: não foi possível ler " + arquivo + " (" + e.getMessage() + ")");
        }
    }

    private void salvar() {
        List<String> linhas = tarefas.values().stream().map(Tarefa::paraLinha).toList();
        try {
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Erro ao salvar tarefas: " + e.getMessage());
        }
    }
}
