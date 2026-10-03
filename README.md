# Gestão de Tarefas

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/status-concluído-brightgreen)

Aplicação de console em **Java** para organizar tarefas por **prioridade**, **status** e **prazo**, com os dados salvos automaticamente em um arquivo CSV.

Projeto desenvolvido para praticar Programação Orientada a Objetos, coleções, Streams e manipulação de arquivos em Java.

## Funcionalidades

- **CRUD completo**: criar, listar, editar e remover tarefas
- **Prioridades** (baixa, média, alta) e **status** (pendente, em andamento, concluída)
- **Prazos** opcionais com destaque automático para tarefas **atrasadas**
- **Ordenação inteligente**: pendentes primeiro, depois maior prioridade e prazo mais próximo
- **Filtros** por status, prioridade e atrasadas, além de **busca por texto** no título e na descrição
- **Resumo** com a quantidade de tarefas em cada status
- **Persistência em CSV**: as tarefas são salvas a cada alteração e recarregadas ao iniciar
- **Validação de entrada**: datas, IDs e opções inválidas são tratados sem encerrar o programa

## Tecnologias e conceitos

- Java 17 (switch expressions, text blocks, `Stream.toList()`)
- POO: encapsulamento, enums com comportamento, separação de responsabilidades
- Collections (`LinkedHashMap`, `EnumMap`) e API de Streams com `Comparator` encadeado
- `java.time` (`LocalDate`, `DateTimeFormatter`) para datas
- `java.nio.file` para leitura e escrita de arquivos em UTF-8

## Estrutura do projeto

```
Tarefas/
├── Main.java                 # Interface de console (menu e leitura de entradas)
├── GerenciadorTarefas.java   # Regras de negócio, consultas e persistência em CSV
├── Tarefa.java               # Entidade Tarefa e conversão de/para linha de texto
├── Prioridade.java           # Enum BAIXA, MEDIA, ALTA
└── Status.java               # Enum PENDENTE, EM_ANDAMENTO, CONCLUIDA
```

A interface (`Main`) não contém regras de negócio: ela apenas lê o que o usuário digita e chama o `GerenciadorTarefas`, o que facilita trocar o console por outra interface no futuro.

## Como executar

**Pré-requisito:** [JDK 17 ou superior](https://adoptium.net/) instalado.

```bash
# 1. Clone o repositório
git clone https://github.com/Arthu4907/Tarefas.git
cd Tarefas

# 2. Compile
javac -encoding UTF-8 -d out *.java

# 3. Execute
java -cp out tarefas.Main
```

O arquivo `tarefas.csv` é criado automaticamente na pasta onde o programa for executado.

## Exemplo de uso

```
=== Sistema de Gestão de Tarefas ===

1) Nova tarefa          5) Remover tarefa
2) Listar tarefas       6) Filtrar / pesquisar
3) Mudar status         7) Resumo
4) Editar tarefa        0) Sair
Opção: 2
#3   [Pendente    ] Alta  | Estudar Java (prazo: 10/10/2026)
      Revisar POO e coleções
#4   [Pendente    ] Média | Entregar trabalho (prazo: 01/10/2026)  [ATRASADA]
#2   [Em andamento] Média | Arrumar a casa (prazo: sem prazo)

Opção: 7
Total: 3 | Pendentes: 2 | Em andamento: 1 | Concluídas: 0 | Atrasadas: 1
```

## Próximos passos

- [ ] Testes unitários com JUnit
- [ ] Organizar o projeto com Maven
- [ ] Persistência em banco de dados (MySQL/JDBC)
- [ ] Interface gráfica ou API REST

## Autor

**Arthur de Castro Martins Guaitanele**
Estudante de Análise e Desenvolvimento de Sistemas — Senac DF

[![GitHub](https://img.shields.io/badge/GitHub-Arthu4907-181717?logo=github)](https://github.com/Arthu4907)
