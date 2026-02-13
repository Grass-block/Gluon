<div align="center" id="readme-top">

<h2 align="center">Gluon</h2>

A lightweight, modular Java application framework for module-based application.

[Explore docs]() |
[Report issue](https://github.com/Grass-block/Gluon/issues)|
[Relevant project](https://github.com/Grass-block/Starlight-plugin)

![LGPL](https://img.shields.io/badge/Licence-LGPL-3366CC?style=for-the-badge&logoColor=blue&labelColor=29355F)
![Java17+](https://img.shields.io/badge/java-17+-009B98?style=for-the-badge&logoColor=blue&labelColor=29355F)
![Lines](https://img.shields.io/badge/Lines-~2300-AD3333?style=for-the-badge&logoColor=blue&labelColor=29355F)


</div>


Gluon is a general-purpose modular framework written in Java. 
It provides a clean and extensible foundation for building highly scalable applications.

Unlike heavyweight enterprise frameworks, Gluon focuses on simplicity, 
modular boundaries, and developer control —
making it ideal for custom platforms and extensible systems.

## ✨ Features

### 🧩 Modular Architecture

- Split your application into independent, composable modules.
- Clear separation of responsibilities.
- Clean boundaries and maintainable design.

### ⚙️ Lightweight & Extensible Core

- Minimal dependencies.
- Rewritable context and managers.
- No heavy runtime container required.



## 🏗 Architecture Overview

Gluon is built around a modular abstraction layer.

Each module:

Has a clearly defined lifecycle

Can declare dependencies

Can be initialized, loaded, or unloaded independently

Communicates through well-defined interfaces

The framework core is responsible for:

Module discovery

Dependency resolution

Lifecycle management

Bootstrapping

This structure enables scalable systems without tight coupling.

## 🚀 Use Cases

Gluon is especially suitable for:

- 🔧 Building plugin-based applications
- 🖥 Server platforms with hot-swappable light-weight components
- 📦 Other custom application containers

If your project requires module structure, Gluon is a clean alternative.

Note:
**Gluon is ideal when you need structure — not a full ecosystem.**

## 📦 Installation

```
git clone https://github.com/Grass-block/Gluon.git
```
1. Clone the repository or download jars from releases:
2. Set it as the library(along with all libs in libraries folder.)
3. Build with your preferred build tool (Gradle/Maven depending on project setup).

## 🧠 Example Concept

### Modules

Modules are the basic unit of functions.

Note that this layer can be re-packaged.

```java
import me.gb2022.gluon.module.*;

@ApplicationModule(id="example",version="1.1")
public class ExampleModule implements  AbstractModule {

    default void initialize() throws Exception {
        
    }

    default void enable() throws Exception {
        
    }

    default void disable() throws Exception {
        
    }

    default void checkCompatibility() throws APIIncompatibleException {
        
    }

}
```

Other cases can be found on **Relevant projects**

## 📄 License

Licensed under LGPL-3.0.

You are free to use, modify, and integrate Gluon into your own projects under the terms of the license.

<hr/>
<div align="center">

#### Gluon

A open project by GrassBlock2022, sponsored by @ATCraftMC 2020-2024
</div>