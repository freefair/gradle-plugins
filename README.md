# javadoc.io gradle plugin
[![Build Status](https://travis-ci.org/freefair/javadoc-io-gradle-plugin.svg?branch=master)](https://travis-ci.org/freefair/javadoc-io-gradle-plugin) [![](https://jitpack.io/v/io.freefair/javadoc-io-gradle-plugin.svg)](https://jitpack.io/#io.freefair/javadoc-io-gradle-plugin)

Let your Javadoc-Tasks in Gradle link against javadoc.io

## How to use

1. Include the plugin via jitpack: [![](https://jitpack.io/v/io.freefair/javadoc-io-gradle-plugin.svg)](https://jitpack.io/#io.freefair/javadoc-io-gradle-plugin)

```gradle
buildscript {
    repositories {
        // ...
        maven { url "https://jitpack.io" }
    }
    dependencies {
        // ...
        classpath 'io.freefair:javadoc-io-gradle-plugin:$currentVersion'
    }
}
```

2. Apply the plugin: 

```gradle
apply plugin: 'io.freefair.javadoc-io'

```

3. Add javadoc dependencies to the `javadocIo` configuration:

```gradle
dependencies {
    // ...
    javadocIo 'com.google.code.gson:gson:2.7:javadoc'
}
```