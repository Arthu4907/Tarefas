package tarefas;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Camada de regras de negócio: mantém as tarefas em memória e
 * sincroniza cada alteração com um arquivo CSV.
 */
public class GerenciadorTarefas {

    /** Não concluídas primeiro, depois maior prioridade, depois prazo mais próximo. */
    private static final Comparator<Tarefa> ORDEM_PADRAO = Comparator
            .comparing((Tarefa t) -> t.getStatus() == Status.CONCLUIDA)
            .thenComparing(Tarefa::getPrioridade, Comparator.reverseOrder())
            .thenComparing(Tarefa::getPrazo, Comparator.nullsLast(Comparator.naturalOrder()));

    private final Map<Integer, Tarefa> tarefas = new LinkedHashMap<>();
    private final Path arquivo;
    private int proximoId = 1;

    /**
     * @param arquivo caminho do CSV; é criado no primeiro salvamento se ainda não existir
     */
    public GerenciadorTarefas(Path arquivo) {
        this.arquivo = arquivo;
        carregar();
    }

    // ---------------------------------------------------------------
    // Operações de CRUD
    // ---------------------------------------------------------------

    public Tarefa adicionar(String titulo, String descricao, Prioridade prioridade, LocalDate prazo) {
        Tarefa tarefa = new Tarefa(proximoId++, titulo, descricao, prioridade, prazo);
        tarefas.put(tarefa.getId(), tarefa);
        salvar();
        return tarefa;
    }

    public Optional<Tarefa> buscar(int id) {
        return Optional.ofNullable(tarefas.get(id));
    }

    public boolean remover(int id) {
        boolean removida = tarefas.remove(id) != null;
        if (removida) {
            salvar();
        }
        return removida;
    }

    public boolean alterarStatus(int id, Status status) {
        Tarefa tarefa = tarefas.get(id);
        if (tarefa == null) {
            return false;
        }
        tarefa.setStatus(status);
        salvar();
        return true;
    }

    /** Persiste as alterações feitas em uma tarefa obtida por {@link #buscar(int)}. */
    public void atualizar() {
        salvar();
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    public List<Tarefa> listarTodas() {
        return filtrar(t -> true);
    }

    public List<Tarefa> filtrarPorStatus(Status status) {
        return filtrar(t -> t.getStatus() == status);
    }

    public List<Tarefa> filtrarPorPrioridade(Prioridade prioridade) {
        return filtrar(t -> t.getPrioridade() == prioridade);
    }

    public List<Tarefa> atrasadas() {
        return filtrar(Tarefa::estaAtrasada);
    }

    /** Busca o termo no título ou na descrição, sem diferenciar maiúsculas de minúsculas. */
    public List<Tarefa> pesquisar(String termo) {
        String busca = termo.toLowerCase();
        return filtrar(t -> t.getTitulo().toLowerCase().contains(busca)
                || t.getDescricao().toLowerCase().contains(busca));
    }

    /** Quantidade de tarefas por status (inclui status com zero tarefas). */
    public Map<Status, Long> resumo() {
        Map<Status, Long> contagem = new EnumMap<>(Status.class);
        for (Status s : Status.values()) {
            contagem.put(s, 0L);
        }
        tarefas.values().forEach(t -> contagem.merge(t.getStatus(), 1L, Long::sum));
        return contagem;
    }

    private List<Tarefa> filtrar(Predicate<Tarefa> criterio) {
        return tarefas.values().stream()
                .filter(criterio)
                .sorted(ORDEM_PADRAO)
                .toList();
    }

    // ---------------------------------------------------------------
    // Persistência
    // ---------------------------------------------------------------

    /** Lê o arquivo ignorando linhas inválidas, sem perder as demais tarefas. */
    private void carregar() {
        if (!Files.exists(arquivo)) {
            return;
        }
        List<String> linhas;
        try {
            linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Aviso: não foi possível ler " + arquivo + " (" + e.getMessage() + ")");
            return;
        }
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) {
                continue;
            }
            try {
                Tarefa tarefa = Tarefa.deLinha(linha);
                tarefas.put(tarefa.getId(), tarefa);
                proximoId = Math.max(proximoId, tarefa.getId() + 1);
            } catch (RuntimeException e) {
                System.err.printf("Aviso: linha %d ignorada (%s)%n", i + 1, e.getMessage());
            }
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
