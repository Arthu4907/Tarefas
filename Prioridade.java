package tarefas;

public enum Prioridade {
    BAIXA, MEDIA, ALTA;

    public static Prioridade deTexto(String texto) {
        return switch (texto.trim().toLowerCase()) {
            case "1", "baixa" -> BAIXA;
            case "3", "alta" -> ALTA;
            default -> MEDIA;
        };
    }
}
