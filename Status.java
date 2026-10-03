package tarefas;

/**
 * Situação atual de uma {@link Tarefa}.
 */
public enum Status {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída");

    private final String rotulo;

    Status(String rotulo) {
        this.rotulo = rotulo;
    }

    /** Texto amigável para exibição ao usuário. */
    public String getRotulo() {
        return rotulo;
    }

    /**
     * Converte a opção digitada no menu (1, 2 ou 3) em um status.
     *
     * @return o status correspondente ou {@code null} se a opção for inválida
     */
    public static Status deOpcao(String opcao) {
        return switch (opcao.trim()) {
            case "1" -> PENDENTE;
            case "2" -> EM_ANDAMENTO;
            case "3" -> CONCLUIDA;
            default -> null;
        };
    }
}
