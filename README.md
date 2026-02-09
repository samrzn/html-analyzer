# HtmlAnalyzer

Solução em Java 17 para encontrar o texto no nível mais profundo de um arquivo HTML simples.

## Compilação

No diretório fonte, executar CLI bash:

```bash
javac HtmlAnalyzer.java
```

## Execução

No diretório fonte, executar CLI bash:

```bash
java HtmlAnalyzer inserir-url-aqui
```

## Saídas possíveis

- Texto encontrado no maior nível de profundidade;
- `malformed HTML` (estrutura HTML inválida);
- `URL connection error` (falha ao obter conteúdo da URL).

## Empacotamento para entrega

CLI para converter código fonte e README.md em arquivo `tar.gz`:

```bash
tar -czf seu_nome_aqui.tar.gz HtmlAnalyzer.java README.md
```

O arquivo `.tar.gz` contém apenas arquivos na raiz (sem diretórios internos).
