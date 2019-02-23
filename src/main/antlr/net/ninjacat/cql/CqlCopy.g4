grammar CqlCopy;

@header {
    package net.ninjacat.cql;
}

options {
    language = Java;
}

copy_stmt:
    K_COPY table_def io_section options_section? ';'?;

console_io: K_STDIN | K_STDOUT;

table_def:  table_name ('(' column_list ')')?;

column_list: column_name(',' column_name)*;

column_name: IDENTIFIER;

keyspace: IDENTIFIER;
table: IDENTIFIER;

table_name: (keyspace '.')? table;

direction : K_FROM | K_TO;

io_section: direction files;

FILENAME: STRING_LITERAL;

file: FILENAME;

files: (file (',' file)*) | console_io;

option_name:
    IDENTIFIER
    ;

option_value:
    IDENTIFIER | STRING_LITERAL
    ;

option:
    option_name '=' option_value
    ;

options_section:
    K_WITH option (K_AND option)*
    ;

STRING_LITERAL
    : '\'' ( ~'\'' | '\'\'' )* '\''
    ;

COMMENT_INPUT
   : '/*' .*? '*/' -> channel (HIDDEN)
   ;

LINE_COMMENT
   : (('-- ' | '#' | '//') ~ [\r\n]* ('\r'? '\n' | EOF) | '--' ('\r'? '\n' | EOF)) -> channel (HIDDEN)
   ;

SPACE
   : [ \t\r\n] + -> channel (HIDDEN)
   ;

DQUOTE
   : '"'
   ;

SQUOTE
   : '\''
   ;

K_STDOUT: S T D O U T;
K_STDIN: S T D I N;
K_COPY: C O P Y ;
K_FROM: F R O M ;
K_TO: T O ;
K_WITH: W I T H ;
K_AND: A N D ;

IDENTIFIER: LETTER (DIGIT|LETTER|'_')+ ;

fragment LETTER: [A-Za-z];
fragment DIGITS: DIGIT+;
fragment DIGIT: '0'..'9';

fragment A
   : [aA]
   ;

fragment B
   : [bB]
   ;

fragment C
   : [cC]
   ;

fragment D
   : [dD]
   ;

fragment E
   : [eE]
   ;

fragment F
   : [fF]
   ;

fragment G
   : [gG]
   ;

fragment H
   : [hH]
   ;

fragment I
   : [iI]
   ;

fragment J
   : [jJ]
   ;

fragment K
   : [kK]
   ;

fragment L
   : [lL]
   ;

fragment M
   : [mM]
   ;

fragment N
   : [nN]
   ;

fragment O
   : [oO]
   ;

fragment P
   : [pP]
   ;

fragment Q
   : [qQ]
   ;

fragment R
   : [rR]
   ;

fragment S
   : [sS]
   ;

fragment T
   : [tT]
   ;

fragment U
   : [uU]
   ;

fragment V
   : [vV]
   ;

fragment W
   : [wW]
   ;

fragment X
   : [xX]
   ;

fragment Y
   : [yY]
   ;

fragment Z
   : [zZ]
   ;
