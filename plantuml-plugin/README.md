# PlantUML plugin

This plugin generates images from plantuml files. 

## Basic usage
```
plugins {
    id "io.freefair.plantuml"
}
```
This adds a PlantUML task to gradle as well as a new source set. The images will be saved to the build directory.

## Configuration options
| Option            | Description                                                                                                                                                        | Default          |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|
| `includePattern`  | Pattern for filenames to include in the image generation process. This is different to `source` so that changes in included files will trigger a recompile as well | `**/*.puml`      |
| `fileFormat`      | File format to generate. All valid options for PlantUML can be chosen.                                                                                             | `PNG`            |
| `outputDirectory` | Directory to save generated files in.                                                                                                                              | `build/plantuml` |

## Custom Task
```
tasks.register("plantUml2", PlantumlTask) {
    source("src/plantuml2")
    includePattern = "**/*.tuml"
    fileFormat = "SVG"
    outputDirectory = layout.buildDirectory.dir("dist2")
}
```
