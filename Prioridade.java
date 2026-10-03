package tarefas;

/**
 * Nível de prioridade de uma {@link Tarefa}, em ordem crescente de importância.
 */
public enum Prioridade {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta");

    private final String rotulo;

    Prioridade(String rotulo) {
        this.rotulo = rotulo;
    }

    /** Texto amigável para exibição ao usuário. */
    public String getRotulo() {
        return rotulo;
    }

    /**
     * Converte a entrada do usuário em uma prioridade.
     * Aceita o número (1, 2, 3) ou o nome ("baixa", "média", "alta").
     * Qualquer outro valor resulta em {@link #MEDIA}.
     */
    public static Prioridade deTexto(String texto) {
        return switch (texto.trim().toLowerCase()) {
            case "1", "baixa" -> BAIXA;
            case "3", "alta" -> ALTA;
            default -> MEDIA;
        };
    }
}
