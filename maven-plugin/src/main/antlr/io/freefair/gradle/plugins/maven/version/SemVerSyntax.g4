grammar SemVerSyntax;

@header {
package io.freefair.gradle.plugins.maven.version;
}

sem_ver_syntax  : parts;
parts      : part ( '.' part ) * ( qualifier )?;
qualifier  : '-' part ( '.' part ) *;
part       : nr | WORD_CHAR+;
nr         : '0' | NUMBER_POSITIVE ( NUMBER ) *;

WORD_CHAR : [0-9A-Za-z];
NUMBER_POSITIVE : [1-9];
NUMBER: [0-9];