package tarefas;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Scanner in = new Scanner(System.in);
    private static GerenciadorTarefas gerenciador;

    public static void main(String[] args) {
        gerenciador = new GerenciadorTarefas(Path.of("tarefas.csv"));
        System.out.println("=== Sistema de Gestão de Tarefas ===");

        while (true) {
            System.out.println("""

                    1) Nova tarefa          5) Remover tarefa
                    2) Listar tarefas       6) Filtrar / pesquisar
                    3) Mudar status         7) Resumo
                    4) Editar tarefa        0) Sair""");
            switch (ler("Opção: ")) {
                case "1" -> novaTarefa();
                case "2" -> mostrar(gerenciador.listarTodas());
                case "3" -> mudarStatus();
                case "4" -> editar();
                case "5" -> remover();
                case "6" -> filtrar();
                case "7" -> resumo();
                case "0" -> { System.out.println("Até mais!"); return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void novaTarefa() {
        String titulo = ler("Título: ");
        if (titulo.isBlank()) { System.out.println("Título obrigatório."); return; }
        String descricao = ler("Descrição (opcional): ");
        Prioridade p = Prioridade.deTexto(ler("Prioridade (1-baixa, 2-média, 3-alta) [2]: "));
        LocalDate prazo = lerData("Prazo dd/mm/aaaa (vazio = sem prazo): ");
        Tarefa t = gerenciador.adicionar(titulo, descricao, p, prazo);
        System.out.println("Criada: " + t);
    }

    private static void mudarStatus() {
        Integer id = lerId();
        if (id == null) return;
        String op = ler("Novo status (1-pendente, 2-em andamento, 3-concluída): ");
        Status s = switch (op) {
            case "1" -> Status.PENDENTE;
            case "2" -> Status.EM_ANDAMENTO;
            case "3" -> Status.CONCLUIDA;
            default -> null;
        };
        if (s == null) { System.out.println("Status inválido."); return; }
        System.out.println(gerenciador.alterarStatus(id, s) ? "Status atualizado." : "Tarefa não encontrada.");
    }

    private static void editar() {
        Integer id = lerId();
        if (id == null) return;
        gerenciador.buscar(id).ifPresentOrElse(t -> {
            System.out.println("Deixe em branco para manter o valor atual.");
            String titulo = ler("Título [" + t.getTitulo() + "]: ");
            if (!titulo.isBlank()) t.setTitulo(titulo);
            String desc = ler("Descrição [" + t.getDescricao() + "]: ");
            if (!desc.isBlank()) t.setDescricao(desc);
            String pr = ler("Prioridade (1/2/3) [" + t.getPrioridade() + "]: ");
            if (!pr.isBlank()) t.setPrioridade(Prioridade.deTexto(pr));
            String prazoTxt = ler("Prazo dd/mm/aaaa ('-' remove) [" +
                    (t.getPrazo() == null ? "sem prazo" : t.getPrazo().format(FORMATO)) + "]: ");
            if (prazoTxt.equals("-")) t.setPrazo(null);
            else if (!prazoTxt.isBlank()) {
                LocalDate d = converterData(prazoTxt);
                if (d != null) t.setPrazo(d);
            }
            gerenciador.atualizar();
            System.out.println("Atualizada: " + t);
        }, () -> System.out.println("Tarefa não encontrada."));
    }

    private static void remover() {
        Integer id = lerId();
        if (id == null) return;
        if (!ler("Confirmar remoção? (s/n): ").equalsIgnoreCase("s")) return;
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
        var r = gerenciador.resumo();
        long total = r.values().stream().mapToLong(Long::longValue).sum();
        System.out.printf("Total: %d | Pendentes: %d | Em andamento: %d | Concluídas: %d | Atrasadas: %d%n",
                total, r.get(Status.PENDENTE), r.get(Status.EM_ANDAMENTO),
                r.get(Status.CONCLUIDA), gerenciador.atrasadas().size());
    }

    // --- Utilitários de entrada ---
    private static void mostrar(List<Tarefa> lista) {
        if (lista.isEmpty()) { System.out.println("Nenhuma tarefa."); return; }
        lista.forEach(System.out::println);
    }

    private static String ler(String msg) {
        System.out.print(msg);
        return in.hasNextLine() ? in.nextLine().trim() : "0";
    }

    private static Integer lerId() {
        try {
            return Integer.parseInt(ler("ID da tarefa: "));
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return null;
        }
    }

    private static LocalDate lerData(String msg) {
        String txt = ler(msg);
        return txt.isBlank() ? null : converterData(txt);
    }

    private static LocalDate converterData(String txt) {
        try {
            return LocalDate.parse(txt, FORMATO);
        } catch (DateTimeParseException e) {
            System.out.println("Data inválida, ignorada.");
            return null;
        }
    }
}
