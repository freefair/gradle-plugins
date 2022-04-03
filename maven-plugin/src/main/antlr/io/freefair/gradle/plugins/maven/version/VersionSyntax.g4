grammar VersionSyntax;

@header {
package io.freefair.gradle.plugins.maven.version;
}

version_syntax  : range | (operator ? parts);
range      : parts ' - ' parts;
operator   : '<' | '<=' | '>' | '>=' | '=' | '!=';
parts      : xr ( '.' xr ) * ( qualifier )?;
xr         : part | 'x' | 'X' | '*';
qualifier  : '-' part ( '.' part ) *;
part       : nr | WORD_CHAR+;
nr         : '0' | NUMBER_POSITIVE ( NUMBER ) *;

WORD_CHAR : [0-9A-Za-z];
NUMBER_POSITIVE : [1-9];
NUMBER: [0-9];