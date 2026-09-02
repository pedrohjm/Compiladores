# Esquemas de Tradução da Linguagem PASCAL SIMPLIFICADA

## Gramática

```
<programa> ::= program id {A01} ; <corpo> . {A45}

----------------------------------------------------------------------

<corpo> ::= <declara> <rotina> {A44} begin <sentencas> end {A46}

----------------------------------------------------------------------

<declara> ::= var <dvar> <mais_dc> | ε
<mais_dc> ::=  ; <cont_dc>
<cont_dc> ::= <dvar> <mais_dc> | ε
<dvar> ::= <variaveis> : <tipo_var> {A02}
<tipo_var> ::= integer
<variaveis> ::= id {A03} <mais_var>
<mais_var> ::=  ,  <variaveis> | ε

--------------------------------------------------------------------

<rotina> ::= <procedimento> | <funcao> | ε
<procedimento> ::= procedure id {A04} <parametros> {A48} ; <corpo> {A56} ; <rotina>
<funcao> ::= function id {A05} <parametros> {A48} : <tipo_funcao> {A47} ; <corpo> {A56} ; <rotina>
<parametros> ::= ( <lista_parametros> ) | ε
<lista_parametros> ::= <lista_id> : <tipo_var> {A06} <cont_lista_par>
<cont_lista_par> ::= ; <lista_parametros> | ε
<lista_id> ::= id {A07} <cont_lista_id>
<cont_lista_id> ::= , <lista_id> | ε
<tipo_funcao> ::= integer

--------------------------------------------------------------------

<sentencas> ::= <comando> <mais_sentencas>
<mais_sentencas> ::= ; <cont_sentencas>
<cont_sentencas> ::= <sentencas> | ε
<var_read> ::= id {A08} <mais_var_read>
<mais_var_read> ::= , <var_read> | ε
<exp_write> ::= id {A09} <mais_exp_write> |
                string {A59} <mais_exp_write> |
                intnum {A43} <mais_exp_write>
<mais_exp_write> ::=  ,  <exp_write> | ε
<comando> ::=
   read ( <var_read> ) |
   write ( <exp_write> ) |
   writeln ( <exp_write> ) {A61} |
   for id {A57} := <expressao> {A11} to <expressao> {A12} do begin <sentencas> end {A13} |
   repeat {A14} <sentencas> until ( <expressao_logica> ) {A15} |
   while {A16} ( <expressao_logica> ) {A17} do begin <sentencas> end {A18} |
   if ( <expressao_logica> ) {A19} then begin <sentencas> end {A20} <pfalsa> {A21} |
   id {A49} := <expressao> {A22} |
   <chamada_procedimento>
<pfalsa> ::= {A25} else begin <sentencas> end | ε

----------------------------------------------------------------------

<chamada_procedimento> ::= id {A50} <argumentos> {A23}
<argumentos> ::= ( <lista_arg> ) | ε
<lista_arg> ::= <expressao> <cont_lista_arg>
<cont_lista_arg> ::= , <lista_arg> | ε

----------------------------------------------------------------------

<expressao_logica> ::= <termo_logico> <mais_expr_logica>
<mais_expr_logica> ::= or <termo_logico> {A26} <mais_expr_logica>  | ε
<termo_logico> ::= <fator_logico> <mais_termo_logico>
<mais_termo_logico> ::= and <fator_logico> {A27} <mais_termo_logico>  | ε
<fator_logico> ::= <relacional> |
                   ( <expressao_logica> ) |
                   not <fator_logico> {A28} |
                   true {A29} |
                   false {A30}
<relacional> ::= <expressao> =  <expressao> {A31} |
                 <expressao> >  <expressao> {A32} |
                 <expressao> >= <expressao> {A33} |
                 <expressao> <  <expressao> {A34} |
                 <expressao> <= <expressao> {A35} |
                 <expressao> <> <expressao> {A36}
<expressao> ::= <termo> <mais_expressao>
<mais_expressao> ::= + <termo> {A37} <mais_expressao>  |
                     - <termo> {A38} <mais_expressao>  | ε
<termo> ::= <fator> <mais_termo>
<mais_termo> ::= * <fator> {A39} <mais_termo>  |
                 / <fator> {A40} <mais_termo>  | ε

----------------------------------------------------------------------

<fator> ::= id {A55} | intnum {A41} | ( <expressao> ) | id {A60} <argumentos> {A42}
```

## Ações Semânticas para o Assembly do Intel 80x86

### Código Necessário

Na classe do sintático:

```java
private TabelaSimbolos tabela = new TabelaSimbolos();
private String rotulo = "";
private int contRotulo = 1;
private int offsetVariavel = 0;
private String nomeArquivoSaida;
private String caminhoArquivoSaida;
private BufferedWriter bw;
private FileWriter fw;
private static final int TAMANHO_INTEIRO = 4;
private List<String> variaveis = new ArrayList<>();
private List<String> sectionData = new ArrayList<>();
private Registro registro;
private String rotuloElse;

public Sintatico(String nomeArquivo) {
	this.nomeArquivo = nomeArquivo;
	lexico = new Lexico(nomeArquivo);
	nomeArquivoSaida = "queronemver.asm";
	caminhoArquivoSaida = Paths.get(nomeArquivoSaida).toAbsolutePath().toString();
	bw = null;
	fw = null;
	try {
		fw = new FileWriter(caminhoArquivoSaida, Charset.forName("UTF-8"));
		bw = new BufferedWriter(fw);
	} catch (Exception e) {
		System.err.println("Erro ao criar arquivo de saída");
	}
}

    private void escreverCodigo(String instrucoes) {
        try {
            if (rotulo.isEmpty()) {
                bw.write(instrucoes + "\n");
            } else {
                bw.write(rotulo + ": " + instrucoes + "\n");
                rotulo = "";
            }
        } catch (IOException e) {
            System.err.println("Erro escrevendo no arquivo de saída");
        }
    }

    private String criarRotulo(String texto) {
        String retorno = "rotulo" + texto + contRotulo;
        contRotulo++;
        return retorno;
    }
```

### Tabela de Ações Semânticas

| Ação | Descrição do que deverá ser realizado |
| --- | --- |
| {A01} | Criar uma tabela de símbolos. Fazer o campo tabelaPai ser null. Inserir o ID na tabela de símbolos. Inicializar nivel com zero. Incializar o campo nivel com zero. Incializar o campo categoria com a informação que se trata do programa principal. Incializar o campo rotulo com "main". Declarar a variável offsetVariavel e iniciá-la com zero. Gerar o cabeçalho do programa, contendo a seção de instruções (global, section .text, etc.). `Registro registro = tabela.add(token.getValor().getValorTexto()); offsetVariavel = 0; registro.setCategoria(Categoria.PROGRAMA_PRINCIPAL); escreverCodigo("global main"); escreverCodigo("extern printf"); escreverCodigo("extern scanf\n"); escreverCodigo("section .text"); rotulo = "main"; escreverCodigo("\t; Entrada do programa"); escreverCodigo("\tpush ebp"); escreverCodigo("\tmov ebp, esp");` |
| {A02} | Atualizar o campo tipo, da tabela de símbolos, de cada uma das variáveis descobertas pela ação semântica {03} (como estamos trabalhando apenas com o tipo inteiro, esta ação pode ser desprezada). Atualizar o registrador esp com essa quantidade de inteiros `int tamanho = 0; for (String var : variaveis) { tabela.get(var).setTipo(Tipo.INTEGER); tamanho += TAMANHO_INTEIRO; } escreverCodigo("\tsub esp, " + tamanho); variaveis.clear();` |
| {A03} | Verificar se o identificador está na tabela de símbolos. Caso esteja, emitir uma mensagem apropriada dizendo que o mesmo já foi declarado anteriormente. Caso contrário, executar as seguir operações: ▪ Inserir o identificador na tabela de símbolos. ▪ Atualizar o campo categoria, com a informação de que o identificador é uma variável. ▪ Incrementar offsetVariavel com o tamanho do tipo (como neste caso só temos inteiros, então ele deverá ser incrementado de SIZEOF_INT). ▪ Atualizar os campos nivel e o offset com os valores das variáveis nivel e offsetVariavel. O valor de offset deverá ser negativo, já que a pilha cresce de cima para baixo (do maior endereço para o menor) e as variáveis estão abaixo do endereço apontado pelo registrador EBP. `String variavel = token.getValor().getValorTexto(); if (tabela.isPresent(variavel)) { System.err.println("Variável " + variavel + " já foi declarada anteriormente"); System.exit(-1); } else { tabela.add(variavel); tabela.get(variavel).setCategoria(Categoria.VARIAVEL); tabela.get(variavel).setOffset(offsetVariavel); offsetVariavel += TAMANHO_INTEIRO; variaveis.add(variavel); }` |
| {A04} | Verificar se o identificador está na tabela de símbolos corrente ou em outras tabelas ligada pelo campo tabelaPai (esta operação é recursiva e deve vasculhar todas as tabelas de símbolos, desde o procedimento corrente até a do programa principal, ou seja, até tabelaPai ser null). Caso esteja, emitir uma mensagem apropriada dizendo que o mesmo já foi declarado anteriormente. Caso contrário, executar as seguir operações: ▪ Incremetar a variável nivel em uma unidade. ▪ Inserir o identificador id na tabela de símbolos. ▪ Atualizar o campo categoria, com a informação de que o identificador é um procedimento. ▪ Atualizar o campo nivel com o valor da variável nivel. ▪ Criar uma nova tabela de símbolos e fazer seu campo tabelaPai apontar para a tabela de símbolos anterior. ▪ Criar uma nova variável offsetVariavel com o valor inicial igual a zero. Ambas, a tabela de símbolos criada e as novas variáveis, serão utilizadas apenas na derivação do procedimento. ▪ Criar um rótulo para a entrada do procedimento e salvá-lo na tabela de símblos, no campo rotulo. Este rótulo pode ser o próprio nome do procedimento. |
| {A05} | Verificar se o identificador está na tabela de símbolos corrente ou em outras tabelas ligadas pelo campo tabelaPai (esta operação é recursiva e deve vasculhar todas as tabelas de símbolos, desde a função corrente até a do programa principal, ou seja, até tabelaPai ser null). Caso esteja, emitir uma mensagem apropriada dizendo que o mesmo já foi declarado anteriormente. Caso contrário, executar as seguir operações: ▪ Incremetar a variável nivel em uma unidade. ▪ Inserir o identificador id na tabela de símbolos. ▪ Atualizar o campo categoria, com a informação de que o identificador é uma função. ▪ Atualizar o campo nivel com o valor da variável nivel. ▪ Criar uma nova tabela de símbolos e fazer seu campo tabelaPai apontar para a tabela de símbolos anterior. ▪ Inserir o identificador da função, com todas as suas informações, na nova tabela, para que ela possa ser localizada pela atribuição em <comando>. ▪ Criar uma nova variável offsetVariavel com o valor inicial igual a zero. Ambas, a tabela de símbolos criada e as novas variáveis, serão utilizadas apenas na derivação do procedimento. ▪ Criar um rótulo para a entrada da função e salvá-lo na tabela de símblos, no campo rotulo. Este rótulo pode ser o próprio nome do procedimento. |
| {A06} | Atualizar o identificador de cada um dos parâmetros formais identificados com o tipo respectivo (neste caso, como estamos trabalhando apenas com o tipo inteiro, esta ação pode ser desprezada). |
| {A07} | Verificar se o identificador está na tabela de símbolos. Caso esteja, emitir uma mensagem apropriada dizendo que o mesmo já foi declarado anteriormente. Caso contrário, executar as seguir operações: ▪ Inserir o identificador na tabela de símbolos. ▪ Atualizar o campo categoria, com a informação de que o identificador é um parâmetro. ▪ Atualizar o campo nivel com o valor da variável nivel. |
| {A08} | Verificar se o identificador está na tabela de símbolos corrente ou em outras tabelas ligada pelo campo tabelaPai (esta operação é recursiva e deve vasculhar todas as tabelas de símbolos, desde de o procedimento corrente até a do programa principal, ou seja, até tabelaPai ser null). Caso não esteja, emitir uma mensagem apropriada dizendo que o mesmo ainda não foi declarado. Caso contrário, executar as seguir operações: ▪ Verificar se a sua categoria é variável ou parâmetro. Se não for, emitir um erro apropriado, indicando que o identificador não é uma variável. ▪ Caso contrário, gerar instrução de leitura de um inteiro do teclado, armazenando o resultado digitado no endereço de memória de id. Lembre-se, o endereço de memória é calculado em função da base da pilha (EBP) e do deslocamento contido em display. ▪ O exemplo a seguir ilustra a tradução de read(x), considerando que x tenha offset –4 e se econtra no nível corrente: `String variavel = token.getValor().getValorTexto(); if (!tabela.isPresent(variavel)) { System.err.println("Variável " + variavel + " não foi declarada"); System.exit(-1); } else { Registro registro = tabela.get(variavel); if (registro.getCategoria() != Categoria.VARIAVEL) { System.err.println("Identificador " + variavel + " não é uma variável"); System.exit(-1); } else { escreverCodigo("\tmov edx, ebp"); escreverCodigo("\tlea eax, [edx - " + registro.getOffset() + "]"); escreverCodigo("\tpush eax"); escreverCodigo("\tpush @Integer"); escreverCodigo("\tcall scanf"); escreverCodigo("\tadd esp, 8"); if (!sectionData.contains("@Integer: db '%d',0")) { sectionData.add("@Integer: db '%d',0"); } } }` |
| {A09} | Verificar se o identificador está na tabela de símbolos corrente ou em outras tabelas ligada pelo campo tabelaPai (esta operação é recursiva e deve vasculhar todas as tabelas de símbolos, desde de o procedimento corrente até a do programa principal, ou seja, até tabelaPai ser null). Caso não esteja, emitir uma mensagem apropriada dizendo que o mesmo ainda não foi declarado. Caso contrário, executar as seguintes operações: ▪ Verificar se a sua categoria é variável ou parâmetro. Se não for, emitir um erro apropriado, indicando que o identificador não é uma variável. ▪ Caso contrário, gerar a instrução para impressão do conteúdo armazenado no endereço de memória de id. Lembre-se, o endereço de memória é calculado em função da base da pilha (EBP) e do deslocamento contido em display. `String variavel = token.getValor().getValorTexto(); if (!tabela.isPresent(variavel)) { System.err.println("Variável " + variavel + " não foi declarada"); System.exit(-1); } else { Registro registro = tabela.get(variavel); if (registro.getCategoria() != Categoria.VARIAVEL) { System.err.println("Identificador " + variavel + " não é uma variável"); System.exit(-1); } else { escreverCodigo("\tpush dword[ebp - " + registro.getOffset() + "]"); escreverCodigo("\tpush @Integer"); escreverCodigo("\tcall printf"); escreverCodigo("\tadd esp, 8"); if (!sectionData.contains("@Integer: db '%d',0")) { sectionData.add("@Integer: db '%d',0"); } } }` |
| {A10} | NEM EXISTE |
| {A11} | ▪ Desempilhar o resultado da avaliação da <expressao> e armazená-lo no endereço de memória de id. (Lembre-se, o endereço de memória é calculado em função da base da pilha (EBP) e do deslocamento contido em display.) ▪ Criar um novo rótulo para a entrada do laço (digamos que este rótulo seja denominado por rotuloFor) ▪ Criar um novo rótulo para a saída do laço (digamos que este rótulo seja denominado por rotuloFim) ▪ Gerar o rotulo rotuloFor. `escreverCodigo("\tpop dword[ebp - " + registro.getOffset() + "]"); String rotuloEntrada = criarRotulo("FOR"); String rotuloSaida = criarRotulo("FIMFOR"); rotulo = rotuloEntrada;` |
| {A12} | Gerar um desvio para rotuloFim se o valor armazenado no endereço de memória de id é maior que o resultado da avaliação de expressao (lembre-se, o resultado de expressao está no topo da pilha). Não se esqueça, o endereço de memória de id é calculado em função da base da pilha (EBP) e do deslocamento contido em display. `escreverCodigo("\tpush ecx\n" + "\tmov ecx, dword[ebp - " + registro.getOffset() + "]\n" + "\tcmp ecx, dword[esp+4]\n" // +4 por causa do ecx + "\tjg " + rotuloSaida + "\n" + "\tpop ecx");` |
| {A13} | Gerar as instruções para incrementar a variável id. Gerar um desvio para rotuloFor. Gerar o rótulo rotuloFim. `escreverCodigo("\tadd dword[ebp - " + registro.getOffset() + "], 1"); escreverCodigo("\tjmp " + rotuloEntrada); rotulo = rotuloSaida;` |
| {A14} | Criar um rótulo para a entrada do laço (digamos que este rótulo seja denominado por rotuloRepeat) Gerar o rótulo rotuloRepeat. `String rotRepeat = criarRotulo("Repeat"); rotulo = rotRepeat;` |
| {A15} | Gerar um desvio para rotuloRepeat, caso o resultado da avaliação de expressao_logica seja falso (novamente, o resultado de expressao está no topo da pilha). `escreverCodigo("\tcmp dword[esp], 0"); escreverCodigo("\tje " + rotRepeat);` |
| {A16} | Criar um rótulo para a entrada do laço (digamos que este rótulo seja denominado por rotuloWhile). Criar um rótulo para a saída do laço (digamos que este rótulo seja denominado por rotuloFim). Gerar o rótulo rotuloWhile. `String rotuloWhile = criarRotulo("While"); String rotuloFim = criarRotulo("FimWhile"); rotulo = rotuloWhile;` |
| {A17} | Gerar um desvio para rotuloFim, caso o resultado da avaliação de expressao_logica seja falso (o resultado da avaliação de expressao_logica está no topo da pilha). `escreverCodigo("\tcmp dword[esp], 0"); escreverCodigo("\tje " + rotuloFim);` |
| {A18} | Gerar um desvio para rotuloWhile. Gerar o rótulo rotuloFim. `escreverCodigo("\tjmp " + rotuloWhile); rotulo = rotuloFim;` |
| {A19} | Criar um rótulo para a entrada do else (digamos que este rótulo seja denominado por rotuloElse). Criar um rótulo para a saída da construção, após a execução das instruções contidas no bloco do then (digamos que este rótulo seja denominado por rotuloFim). Gerar um desvio para rotuloElse, caso o resultado da avaliação de expressao_logica seja falso (novamente, o resultado de <expressao_logica> está no topo da pilha). `rotuloElse = criarRotulo("Else"); String rotuloFim = criarRotulo("FimIf"); escreverCodigo("\tcmp dword[esp], 0\n"); escreverCodigo("\tje " + rotuloElse);` |
| {A20} | Gerar o desvio para o rotuloFim. `escreverCodigo("\tjmp " + rotuloFim);` |
| {A21} | Gerar o rótulo rotuloFim. `rotulo = rotuloFim;` |
| {A22} | Armazenar o resultado da avaliação de expressao no endereço de memória apontado por id, se for uma variável ou parâmetro, ou no endereço de memória utilizado para o valor de retorno da função. Lembre-se, que o resultado da avaliação de expressao está no topo da pilha e que o endereço de memória de id é calculado em função da base da pilha (EBP) e, no caso de variáveis e parâmetros, do deslocamento contido em display. `registro = tabela.get(variavel); escreverCodigo("\tpop eax"); escreverCodigo("\tmov dword[ebp - " + registro.getOffset() + "], eax");` |
| {A23} | Verificar se o número de argumentos fornecido em argumentos é igual ao número numeroElementos, do id reconhecido. Caso não seja, emiti mensagem dizendo que o número de argumentos é inválido. Caso contrário: ▪ Gerar instrução para chamada do procedimento, ou seja, uma chamada para o rotulo armazenado na tabela de símbolos. ▪ Gerar a instrução para desempilhar os parâmetros. Como estamos trabalhando apenas com inteiros, devemos desalocar numeroElementos * SIZEOF_INT. |
| {A24} | NEM EXISTE |
| {A25} | Gerar o rótulo rotuloElse. `escreverCodigo(rotuloElse + ":");` |
| {A26} | Empilhar 1, caso o valor de expressao_logica ou termo_logico seja 1, e 0 (falso), caso seja diferente. Isto pode ser feito da seguinte forma: Crie um novo rótulo, digamos rotSaida Crie um novo rótulo, digamos rotVerdade Gere a instrução: cmp dword [ESP + 4], 1 Gere a instrução je para rotVerdade Gere a instrução: cmp dword [ESP], 1 Gere a instrução je para rotVerdade Gere a instrução: mov dword [ESP + 4], 0 Gere a instrução jmp para rotSaida Gere o rótulo rotVerdade Gere a instrução: mov dword [ESP + 4], 1 Gere o rótulo rotSaida Gere a instrução: add esp, 4 `String rotSaida = criarRotulo("SaidaMEL"); String rotVerdade = criarRotulo("VerdadeMEL"); escreverCodigo("\tcmp dword [ESP + 4], 1"); escreverCodigo("\tje " + rotVerdade); escreverCodigo("\tcmp dword [ESP], 1"); escreverCodigo("\tje " + rotVerdade); escreverCodigo("\tmov dword [ESP + 4], 0"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotVerdade; escreverCodigo("\tmov dword [ESP + 4], 1"); rotulo = rotSaida; escreverCodigo("\tadd esp, 4");` |
| {A27} | Empilhar 1 (verdadeiro), caso o valor de termo_logico e fator_logico seja 1, e 0 (falso), caso seja diferente. Proceda de forma semelhante a ação 26. `String rotSaida = criarRotulo("SaidaMTL"); String rotFalso = criarRotulo("FalsoMTL"); escreverCodigo("\tcmp dword [ESP + 4], 1"); escreverCodigo("\tjne " + rotFalso); escreverCodigo("\tpop eax"); escreverCodigo("\tcmp dword [ESP], eax"); escreverCodigo("\tjne " + rotFalso); escreverCodigo("\tmov dword [ESP], 1"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotFalso; escreverCodigo("\tmov dword [ESP], 0"); rotulo = rotSaida;` |
| {A28} | Empilhar 1 (verdadeiro), caso o valor de fator_logico seja 0, e 0 (falso), caso seja diferente. Proceda da seguinte forma: Crie um rótulo Falso e outro Saida. Gere a instrução: cmp dword [ESP], 1 Gere a instrução: jne Falso Gere a instrução: mov dword [ESP], 0 Gere a instrução: jmp Fim Gere o rótulo Falso Gere a instrução: mov dword [ESP], 1 Gere o rótulo Fim `String rotFalso = criarRotulo("FalsoFL"); String rotSaida = criarRotulo("SaidaFL"); escreverCodigo("\tcmp dword [ESP], 1"); escreverCodigo("\tjne " + rotFalso); escreverCodigo("\tmov dword [ESP], 0"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotFalso; escreverCodigo("\tmov dword [ESP], 1"); rotulo = rotSaida;` |
| {A29} | Empilhar 1. `escreverCodigo("\tpush 1");` |
| {A30} | Empilhar 0. `escreverCodigo("\tpush 0");` |
| {A31} | Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja igual a segunda, ou 0 (falso), caso contrário. Isto pode ser feito da seguinte forma: Crie um rótulo Falso e outro Saida. Gere a instrução: pop eax Gere a instrução: cmp dword [ESP], eax Gere a instrução: jne Falso Gere a instrução: mov dword [ESP], 1 Gere a instrução: jmp Fim Gere o rótulo Falso Gere a instrução: mov dword [ESP], 0 Gere o rótulo Fim `String rotFalso = criarRotulo("FalsoREL"); String rotSaida = criarRotulo("SaidaREL"); escreverCodigo("\tpop eax"); escreverCodigo("\tcmp dword [ESP], eax"); escreverCodigo("\tjne " + rotFalso); escreverCodigo("\tmov dword [ESP], 1"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotFalso; escreverCodigo("\tmov dword [ESP], 0"); rotulo = rotSaida;` |
| {A32} | Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja maior que a segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação 31. `String rotFalso = criarRotulo("FalsoREL"); String rotSaida = criarRotulo("SaidaREL"); escreverCodigo("\tpop eax"); escreverCodigo("\tcmp dword [ESP], eax"); escreverCodigo("\tjle " + rotFalso); escreverCodigo("\tmov dword [ESP], 1"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotFalso; escreverCodigo("\tmov dword [ESP], 0"); rotulo = rotSaida;` |
| {A33} | Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja maior ou igual a segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação 31. `String rotFalso = criarRotulo("FalsoREL"); String rotSaida = criarRotulo("SaidaREL"); escreverCodigo("\tpop eax"); escreverCodigo("\tcmp dword [ESP], eax"); escreverCodigo("\tjl " + rotFalso); escreverCodigo("\tmov dword [ESP], 1"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotFalso; escreverCodigo("\tmov dword [ESP], 0"); rotulo = rotSaida;` |
| {A34} | Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja menor que a segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação 31. `String rotFalso = criarRotulo("FalsoREL"); String rotSaida = criarRotulo("SaidaREL"); escreverCodigo("\tpop eax"); escreverCodigo("\tcmp dword [ESP], eax"); escreverCodigo("\tjge " + rotFalso); escreverCodigo("\tmov dword [ESP], 1"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotFalso; escreverCodigo("\tmov dword [ESP], 0"); rotulo = rotSaida;` |
| {A35} | Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja menor ou igual a segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação 31. `String rotFalso = criarRotulo("FalsoREL"); String rotSaida = criarRotulo("SaidaREL"); escreverCodigo("\tpop eax"); escreverCodigo("\tcmp dword [ESP], eax"); escreverCodigo("\tjg " + rotFalso); escreverCodigo("\tmov dword [ESP], 1"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotFalso; escreverCodigo("\tmov dword [ESP], 0"); rotulo = rotSaida;` |
| {A36} | Empilhar 1 (verdadeiro), caso a primeira expressão expressao seja diferente da segunda, ou 0 (falso), caso contrário. Proceda como o exemplo da ação 31. `String rotFalso = criarRotulo("FalsoREL"); String rotSaida = criarRotulo("SaidaREL"); escreverCodigo("\tpop eax"); escreverCodigo("\tcmp dword [ESP], eax"); escreverCodigo("\tje " + rotFalso); escreverCodigo("\tmov dword [ESP], 1"); escreverCodigo("\tjmp " + rotSaida); rotulo = rotFalso; escreverCodigo("\tmov dword [ESP], 0"); rotulo = rotSaida;` |
| {A37} | Gerar instruções para realizar a soma entre os dois operandos no topo da pilha, devendo resultado ser colocado no topo da pilha. Como sugestão, você pode proceder da seguinte forma: ▪ pop eax ▪ add dword[ESP], eax `escreverCodigo("\tpop eax"); escreverCodigo("\tadd dword[ESP], eax");` |
| {A38} | Gerar instruções para realizar a subtração entre os dois operandos no topo da pilha, devendo resultado ser colocado no topo da pilha. Como sugestão, você pode proceder da seguinte forma: ▪ pop eax ▪ sub dword[ESP], eax `escreverCodigo("\tpop eax"); escreverCodigo("\tsub dword[ESP], eax");` |
| {A39} | Gerar instruções para desempilhar os dois valores no topo da pilha. Efetuar a multiplicação dos dois e empilhar o resultado. Como sugestão, você pode proceder da seguinte forma: ▪ pop eax ▪ imul eax, dword [ESP] ▪ mov dword [ESP], eax `escreverCodigo("\tpop eax"); escreverCodigo("\timul eax, dword [ESP]"); escreverCodigo("\tmov dword [ESP], eax");` |
| {A40} | Gerar instruções para desempilhar os dois valores no topo da pilha. Efetuar a divisão entre os dois e empilhar o resultado. Como sugestão, você pode proceder da seguinte forma: ▪ pop ecx ▪ pop eax ▪ idiv ecx ▪ push eax `escreverCodigo("\tpop ecx"); escreverCodigo("\tpop eax"); escreverCodigo("\tcdq"); // GEMINI escreverCodigo("\tidiv ecx"); escreverCodigo("\tpush eax");` |
| {A41} | Empilhar o número correspondente a num. `escreverCodigo("\tpush " + token.getValor().getValorInteiro());` |
| {A42} | Se a categoria do identificador id, reconhecido em fator, for função, então verificar se o número de argumentos fornecido em argumentos é igual ao numeroParametros do id. Caso não seja, emitir mensagem dizendo que o número de argumentos é insuficiente. Caso contrário: ▪ Gerar instrução para chamada do procedimento, ou seja, uma chamada para o rotulo armazenado na tabela de símbolos. ▪ Gerar a instrução para desempilhar os parâmetros. Como estamos trabalhando apenas com inteiros, devemos desalocar numeroParametros * SIZEOF_INT. |
| {A43} | `escreverCodigo("\tpush " + token.getValor().getInteiro()); escreverCodigo("\tpush @Integer"); escreverCodigo("\tcall printf"); escreverCodigo("\tadd esp, 8"); if (!sectionData.contains("@Integer: db '%d',0")) { sectionData.add("@Integer: db '%d',0"); }` |
| {A44} | O objetivo desta ação é gerar as instruções correspondentes ao corpo de um procedimento (o programa principal é considerado também um procedimento, assim como uma função). Deverá receber a tabela de símbolos a ser utilizada para pesquisa dos identificadores. Esta tabela é a última criada antes da chamada de corpo. Gerar o rótulo de entrada para o procedimento. Este rótulo é o mesmo salvo no campo rotulo da tabela de símbolos quando o procedimento foi descoberto. Gerar a instrução para empilhar o conteúdo do registrador EBP. Gerar a instrução para empilhar o display do nível. Gerar a instrução para mover o conteúdo do registrador ESP para o EBP. Gerar a instrução para armazenar o conteúdo do registrador EBP no display do nível. Gerar instrução para alocar espaço para as variáveis na pilha. Isto é feito subtraindo-se do registrador ESP a quantidade de bytes que todas as variáveis locais do procedimento ocuparão. A quantidade de bytes a ser alocados deve ser descoberta na derivação de declara. |
| {A45} | Gerar a seção de dados (section .data). Gerar instrução para alocar espaço para o display, cujo rótulo é display. O espaço a ser alocado é dado pela quantidade de níveis, descobertos durante a análise, somado com o tamanho ocupado por um inteiro. Isto é, alocar nivel * SIZEOF_INT bytes. `escreverCodigo("\tleave"); escreverCodigo("\tret"); if (!sectionData.isEmpty()) { escreverCodigo("\nsection .data\n"); for (String mensagem : sectionData) { escreverCodigo(mensagem); } } try { bw.close(); fw.close(); } catch (IOException e) { System.err.println("Erro ao fechar arquivo de saída"); }` |
| {A46} | Gerar instrução para desalocar o espaço alocado para as variáveis locais. Isto é feito somando-se ao ESP a quantidade de bytes ocupados pelas variáveis, sendo este total descoberto em declara. Gerar a instrução para mover o conteúdo de ESP para EBP Gerar a instrução necessária para restaurar o display do nível indicado por nivel. Gerar a instrução necessária para restaurar o registrador EBP. Gerar as instruções para retornar do procedimento. |
| {A47} | Atualizar o identificador da função com o seu tipo (neste caso, como estamos trabalhando apenas com o tipo inteiro, esta ação pode ser desprezada). Definir o offset da função como sendo o offsetParametro. |
| {A48} | Atualizar, na tabela de símbolos, o identificador da função ou procedimento com o número total de parâmetros identificados (campo numeroParametros). Calcular o offset de cada parâmetro da seguinte forma: Para i = 1 até numeroParametros faça offset do parâmetro i = 12 + ((numeroParametros – i) * SIZEOF_INT) Fim para Como os parâmetros devem começar a partir do endereço onde o registrado EIP do chamador foi salvo, que é o endereço dado por EBP + 8, então, se o procedimento tem n parâmetros (numeroParametros), o n-ésimo parâmetro deve estar no endereço EBP + 12, o (n – 1)-ésimo parâmetro deve estar no endereço EBP + 16, e assim sucessivamente, até chegarmos ao primeiro parâmetro, o qual deve estar no endereço EBP + 12 + ((n – 1) * SIZEOF_INT). |
| {A49} | Verificar se o identificador id está na tabela de símbolos corrente ou nas indicadas pela tabelaPai (esta operação é recursiva e deve vasculhar todas as tabelas de símbolos, desde o procedimento corrente até a do programa principal) e se sua categoria é variável ou parâmetro. Caso não esteja em nunhuma tabela, emitir uma mensagem apropriada dizendo que o mesmo ainda não foi declarado. `String variavel = token.getValor().getValorTexto(); if (!tabela.isPresent(variavel)) { System.err.println("Variável " + variavel + " não foi declarada"); System.exit(-1); } else { registro = tabela.get(variavel); if (registro.getCategoria() != Categoria.VARIAVEL) { System.err.println("O identificador " + variavel + "não é uma variável. A49"); System.exit(-1); } }` |
| {A50} | Verificar se o identificador id está na tabela de símbolos corrente ou nas indicadas pela tabelaPai (esta operação é recursiva e deve vasculhar todas as tabelas de símbolos, desde o procedimento corrente até a do programa principal) e se sua categoria é procedimento. Se não for emitir mensagem apropriada indicando que o identificador deve ser o nome de um procedimento. |
| {A51} | Se a categoria do identificador id, reconhecido em fator, for função, então prosseguir para argumentos. |
| {A55} | Se a categoria do identificador id, reconhecido em fator, for variável ou parâmetro, então empilhar o valor armazenado no endereço de memória de id. Lembre-se, que o endereço de memória de id é calculado em função da base da pilha (EBP) e do deslocamento contido em display. `String variavel = token.getValor().getValorTexto(); if (!tabela.isPresent(variavel)) { System.err.println("Variável " + variavel + " não foi declarada"); System.exit(-1); } else { registro = tabela.get(variavel); if (registro.getCategoria() != Categoria.VARIAVEL) { System.err.println("O identificador " + variavel + "não é uma variável. A55"); System.exit(-1); } } escreverCodigo("\tpush dword[ebp - " + registro.getOffset() + "]");` |
| {A56} | Decrementar a variável nivel em uma unidade. |
| {A57} | Verificar se o identificador id está na tabela de símbolos corrente ou nas apontadas por tabelaPai. Caso não esteja, emitir mensagem apropriada dizendo que o mesmo ainda não foi declarado. Caso contrário, verificar se sua categoria é variável, parâmetro ou é a função corrente. Caso não seja, emitir mensagem indicando que o identificador não é uma variável. `String variavel = token.getValor().getValorTexto(); if (!tabela.isPresent(variavel)) { System.err.println("Variável " + variavel + " não foi declarada"); System.exit(-1); } else { registro = tabela.get(variavel); if (registro.getCategoria() != Categoria.VARIAVEL) { System.err.println("O identificador " + variavel + "não é uma variável. A57"); System.exit(-1); } }` |
| {A58} | Verificar se o identificador id está na tabela de símbolos corrente, se sua categoria é função. Se for, verificar se o seu nível corresponde ao nível corrente. Caso isto não ocorra, emitir uma mensagem apropriada dizendo que o identificador deve corresponder a função corrente. |
| {A59} | Gerar instruções para imprimir a string no vídeo. Proceda da seguinte forma: ▪ Gere um rótulo na seção .data para a string, contendo a string finalizada com o caracter ASCII 0 (null). ▪ Empilhe o endereço do rótulo. ▪ Chame a função printf(). ▪ Desempilhe o endereço do rótulo empilhado anteriormente. Por exemplo, suponha a instrução write('Olá mundo!'). Para gerá-la faremos: `<outras instruções> push messageOlaMundo call _printf add esp, 4 <outras instruções> section .data messageOlaMundo: db 'Olá Mundo!', 0` No caso da instrução writeln('Olá mundo!'), devemos adicionar o caracter ASCII 10 (line feed) após a string, ficando assim: `messageOlaMundo: db 'Olá Mundo!', 10, 0` `String string = token.getValor().getValorTexto(); String rotulo = criarRotulo("String"); sectionData.add(rotulo + ": db '" + string + "',0"); escreverCodigo("\tpush " + rotulo); escreverCodigo("\tcall printf"); escreverCodigo("\tadd esp, 4");` |
| {A60} | Se a categoria do identificador id for função, então gerar a instrução sub esp, 4 |
| {A61} | Gerar um avanço de linha, ou seja, um line feed (ASCII 10). `String novaLinha = "rotuloStringLN: db '',10,0"; if (!sectionData.contains(novaLinha)) { sectionData.add(novaLinha); } escreverCodigo("\tpush rotuloStringLN"); escreverCodigo("\tcall printf"); escreverCodigo("\tadd esp, 4");` |

## Compilando e Executando

### Linux

```bash
sudo apt install nasm gcc-multilib
nasm -f elf queronemver.asm -o queronemver.o
gcc -m32 -z noexecstack -no-pie -o queronemver queronemver.o
./queronemver
```

### Windows

Instalar o [MSYS2](https://www.msys2.org):

```bash
pacman -Syu
pacman -S --needed base-devel mingw-w64-ucrt-x86_64-toolchain mingw-w64-ucrt-x86_64-nasm
nasm -f win32 queronemver.asm -o queronemver.o
gcc -m32 -z noexecstack -no-pie -o queronemver.exe queronemver.o
queronemver
```

Se o nasm do msys2 não funcionar, baixar do https://www.nasm.us/pub/nasm/releasebuilds/3.01/win64/

## Programas de Teste

### Write

```pascal
Program TesteWrite;
Begin
    Write(10);
    Write('teste');
End.
```

```pascal
// Depois que a atribuição estiver pronta
Program TesteWrite;
Var x : Integer;
Begin
    x := 5;
    Write(x);
End.
```

### Read

```pascal
Program TesteRead;
Var x : Integer;
Begin
    Read(x);
    Write(x);
End.
```

### For

```pascal
Program TesteFor;
Var x : Integer;
Begin
    for x := 1 to 10 do
    begin
        writeln(x);
    end;
End.
```

### Repeat

```pascal
Program TesteRepeat;
Var x : Integer;
Begin
    x := 1;
    repeat
        writeln(x);
        x := x + 1;
    until (x > 10);
End.
```

### While

```pascal
Program TesteWhile;
Var x : Integer;
Begin
    x := 1;
    while (x <= 10) do
    begin
        writeln(x);
        x := x + 1;
    end;
End.
```

### If

```pascal
Program TesteIf;
Var x : Integer;
Begin
    x := 5;
    if (x < 10) then
    begin
        writeln('Valor menor que 10');
    end
    else
    begin
        writeln('Valor maior ou igual a 10');
    end;
End.
```

### Fibonacci

```pascal
Program Fibonacci;
Var termo1, termo2, aux, cont, quantos : Integer;
Begin
    writeln('----------FIBONACCI------------');
    termo1 := 1;
    termo2 := 1;
    quantos := 0;
    aux := 0;
    write('Informe a quantidade de numeros que deseja ver da sequencia fibonacci: ');
    read(quantos);
    writeln(termo1);
    writeln(termo2);
    cont := 2;
    while (cont < quantos) do
    begin
        aux := termo1 + termo2;
        writeln(aux);
        termo1 := termo2;
        termo2 := aux;
        cont := cont + 1;
    end;
End.
```
