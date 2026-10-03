package tarefas;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação: interface de linha de comando (menu no console).
 * Toda regra de negócio fica em {@link GerenciadorTarefas}.
 */
public class Main {

    private static final Path ARQUIVO_DADOS = Path.of("tarefas.csv");
    private static final Scanner ENTRADA = new Scanner(System.in);

    private static GerenciadorTarefas gerenciador;

    public static void main(String[] args) {
        gerenciador = new GerenciadorTarefas(ARQUIVO_DADOS);
        System.out.println("=== Sistema de Gestão de Tarefas ===");

        boolean executando = true;
        while (executando) {
            exibirMenu();
            switch (ler("Opção: ")) {
                case "1" -> novaTarefa();
                case "2" -> mostrar(gerenciador.listarTodas());
                case "3" -> mudarStatus();
                case "4" -> editar();
                case "5" -> remover();
                case "6" -> filtrar();
                case "7" -> resumo();
                case "0" -> {
                    System.out.println("Até mais!");
                    executando = false;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void exibirMenu() {
        System.out.println("""

                1) Nova tarefa          5) Remover tarefa
                2) Listar tarefas       6) Filtrar / pesquisar
                3) Mudar status         7) Resumo
                4) Editar tarefa        0) Sair""");
    }

    // ---------------------------------------------------------------
    // Ações do menu
    // ---------------------------------------------------------------

    private static void novaTarefa() {
        String titulo = ler("Título: ");
        if (titulo.isBlank()) {
            System.out.println("Título obrigatório.");
            return;
        }
        String descricao = ler("Descrição (opcional): ");
        Prioridade prioridade = Prioridade.deTexto(ler("Prioridade (1-baixa, 2-média, 3-alta) [2]: "));
        LocalDate prazo = lerData("Prazo dd/mm/aaaa (vazio = sem prazo): ");

        Tarefa tarefa = gerenciador.adicionar(titulo, descricao, prioridade, prazo);
        System.out.println("Criada: " + tarefa);
    }

    private static void mudarStatus() {
        Integer id = lerId();
        if (id == null) {
            return;
        }
        Status status = Status.deOpcao(ler("Novo status (1-pendente, 2-em andamento, 3-concluída): "));
        if (status == null) {
            System.out.println("Status inválido.");
            return;
        }
        System.out.println(gerenciador.alterarStatus(id, status) ? "Status atualizado." : "Tarefa não encontrada.");
    }

    private static void editar() {
        Integer id = lerId();
        if (id == null) {
            return;
        }
        gerenciador.buscar(id).ifPresentOrElse(tarefa -> {
            System.out.println("Deixe em branco para manter o valor atual.");

            String titulo = ler("Título [" + tarefa.getTitulo() + "]: ");
            if (!titulo.isBlank()) {
                tarefa.setTitulo(titulo);
            }

            String descricao = ler("Descrição [" + tarefa.getDescricao() + "]: ");
            if (!descricao.isBlank()) {
                tarefa.setDescricao(descricao);
            }

            String prioridade = ler("Prioridade (1/2/3) [" + tarefa.getPrioridade().getRotulo() + "]: ");
            if (!prioridade.isBlank()) {
                tarefa.setPrioridade(Prioridade.deTexto(prioridade));
            }

            String prazoAtual = tarefa.getPrazo() == null ? "sem prazo" : tarefa.getPrazo().format(Tarefa.FORMATO_DATA);
            String prazo = ler("Prazo dd/mm/aaaa ('-' remove) [" + prazoAtual + "]: ");
            if (prazo.equals("-")) {
                tarefa.setPrazo(null);
            } else if (!prazo.isBlank()) {
                LocalDate data = converterData(prazo);
                if (data != null) {
                    tarefa.setPrazo(data);
                }
            }

            gerenciador.atualizar();
            System.out.println("Atualizada: " + tarefa);
        }, () -> System.out.println("Tarefa não encontrada."));
    }

    private static void remover() {
        Integer id = lerId();
        if (id == null) {
            return;
        }
        if (!ler("Confirmar remoção? (s/n): ").equalsIgnoreCase("s")) {
            System.out.println("Remoção cancelada.");
            return;
        }
        System.out.println(gerenciador.remover(id) ? "Removida." : "Tarefa não encontrada.");
    }

    private static void filtrar() {
        System.out.println("1) Pendentes  2) Em andamento  3) Concluídas  4) Alta prioridade  5) Atrasadas  6) Pesquisar texto");
        switch (ler("Filtro: ")) {
            case "1" -> mostrar(gerenciador.filtrarPorStatus(Status.PENDENTE));
            case "2" -> mostrar(gerenciador.filtrarPorStatus(Status.EM_ANDAMENTO));
            case "3" -> mostrar(gerenciador.filtrarPorStatus(Status.CONCLUIDA));
            case "4" -> mostrar(gerenciador.filtrarPorPrioridade(Prioridade.ALTA));
            case "5" -> mostrar(gerenciador.atrasadas());
            case "6" -> mostrar(gerenciador.pesquisar(ler("Termo: ")));
            default -> System.out.println("Filtro inválido.");
        }
    }

    private static void resumo() {
        Map<Status, Long> contagem = gerenciador.resumo();
        long total = contagem.values().stream().mapToLong(Long::longValue).sum();
        System.out.printf("Total: %d | Pendentes: %d | Em andamento: %d | Concluídas: %d | Atrasadas: %d%n",
                total,
                contagem.get(Status.PENDENTE),
                contagem.get(Status.EM_ANDAMENTO),
                contagem.get(Status.CONCLUIDA),
                gerenciador.atrasadas().size());
    }

    // ---------------------------------------------------------------
    // Utilitários de entrada e saída
    // ---------------------------------------------------------------

    private static void mostrar(List<Tarefa> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhuma tarefa encontrada.");
            return;
        }
        lista.forEach(System.out::println);
    }

    /** Lê uma linha do console; se a entrada terminar (EOF), retorna "0" para encerrar o programa. */
    private static String ler(String mensagem) {
        System.out.print(mensagem);
        return ENTRADA.hasNextLine() ? ENTRADA.nextLine().trim() : "0";
    }

    private static Integer lerId() {
        try {
            return Integer.parseInt(ler("ID da tarefa: "));
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return null;
        }
    }

    private static LocalDate lerData(String mensagem) {
        String texto = ler(mensagem);
        return texto.isBlank() ? null : converterData(texto);
    }

    private static LocalDate converterData(String texto) {
        try {
            return LocalDate.parse(texto, Tarefa.FORMATO_DATA);
        } catch (DateTimeParseException e) {
            System.out.println("Data inválida, ignorada.");
            return null;
        }
    }
}
