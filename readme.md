<div align="center" id="readme-top">

<h2 align="center">Gluon - [胶子]</h2>

<p align="center">
  <i>一个轻量的 Java 模块化应用框架 &mdash; A lightweight, modular Java application framework.</i>
</p>

[Explore the docs »](#使用--usage)
[Report Bug](https://github.com/Grass-Block/Gluon/issues)·
[Request Feature](https://github.com/Grass-Block/Gluon/issues)·
[Relevant project](https://github.com/Grass-Block/Starlight-plugin)

![LGPL](https://img.shields.io/badge/Licence-LGPL-3366CC?style=for-the-badge&logoColor=blue&labelColor=29355F)
![Java17+](https://img.shields.io/badge/java-17+-009B98?style=for-the-badge&logoColor=blue&labelColor=29355F)
![Lines](https://img.shields.io/badge/Lines-~2300-AD3333?style=for-the-badge&logoColor=blue&labelColor=29355F)

</div>

---

## 目录 · Table of Contents

1. [关于项目 · About the Project](#关于项目--about-the-project)
   - [技术栈 · Built With](#技术栈--built-with)
2. [快速开始 · Getting Started](#快速开始--getting-started)
   - [环境要求 · Prerequisites](#环境要求--prerequisites)
   - [安装 · Installation](#安装--installation)
3. [使用 · Usage](#使用--usage)
   - [启动上下文 · Bootstrapping the Context](#启动上下文--bootstrapping-the-context)
   - [包 · Package](#包--package)
   - [模块 · Module](#模块--module)
     - [模块生命周期 · Module Lifecycle](#模块生命周期--module-lifecycle)
   - [附件与子组件 · Attachments & SubComponents](#附件与子组件--attachments--subcomponents)
   - [服务 · Service](#服务--service)
4. [路线图 · Roadmap](#路线图--roadmap)
5. [参与贡献 · Contributing](#参与贡献--contributing)
6. [许可证 · License](#许可证--license)
7. [联系方式 · Contact](#联系方式--contact)
8. [致谢 · Acknowledgments](#致谢--acknowledgments)

<br/>

## 关于项目 · About the Project

Gluon 是一个**通用型模块化框架（库）**，使用纯 Java 编写。它不依赖任何重量级运行容器，而是围绕几个清晰的核心抽象构建，帮助你把应用拆分成**可组合、可独立启停的模块**，并通过**服务层**进行跨组件通信。

Gluon is a general-purpose **modular framework (library)** written in plain Java. Instead of relying on a heavyweight runtime container, it is built around a small set of well-defined abstractions that let you split your application into **composable modules** that can be enabled/disabled independently, and communicate across boundaries through a **service layer**.

Gluon 特别适用于需要模块结构、插件式扩展的**库作者与平台开发者**。若你只需要"结构"而非一整个生态，Gluon 是一个轻量而干净的替代方案。

Gluon is especially suited for **library authors and platform builders** who need structure and plugin-style extension. If you want the *structure* without the *entire ecosystem*, Gluon is a lightweight, clean alternative.

架构总览（`arch-uml.png`）：

<p align="center">
  <a href="https://github.com/Grass-Block/Gluon/raw/main/arch-uml.png">
    <img src="https://github.com/Grass-Block/Gluon/raw/main/arch-uml.png" alt="Architecture Overview" width="720px">
  </a>
</p>

核心由 `ModularApplicationContext` 统一编排，内部持有三个默认 Manager。它们**并非互为替代**，而是遵循「**可重写 + 注入**」模式：`ModularApplicationContext.Builder` 允许为每个 Manager 注入自定义实现（`Function<Context, T> provider`），以便宿主平台按需替换默认行为。

The core is orchestrated by a single `ModularApplicationContext`, which owns three default managers. They are **not** interchangeable substitutes for each other — instead they follow a *rewritable + injectable* pattern: the `Builder` lets you supply a custom implementation per manager via `Function<Context, T> provider`, so hosts can swap in their own behavior.

| 抽象层 · Layer | 职责 · Responsibility |
| :--- | :--- |
| `PackageManager` | 扫描并注册 **包**（一组模块 + 服务），管理其启停 Discover & register packages; manage lifecycle |
| `ModuleManager` | 模块的注册、构造与启停 Register, construct and toggle modules |
| `ServiceManager` | 服务的实现创建、注入与导出 Create, inject and export service implementations |

每个组件都实现了统一的 `FunctionalComponent`，但**各层的实际调用顺序不同**，详见下方「模块生命周期」一节。

Every component implements the shared `FunctionalComponent`, but the **concrete call order differs per layer** — see the "Module Lifecycle" section below.

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

### 技术栈 · Built With

- [Java 17+](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [Gradle](https://gradle.org/)
- [Log4j 2](https://logging.apache.org/log4j/2.x/) (`compileOnly`)
- [me.gb2022.commons](https://github.com/Grass-Block) （`commons-math` / `commons-container` / `commons-general` / `commons-compatibility`，均为 `compileOnly`）

> 依赖均以 `compileOnly` 提供，框架本身不打包运行时依赖，保持库的轻量。

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

---

## 快速开始 · Getting Started

### 环境要求 · Prerequisites

- JDK 17+
- Gradle（推荐使用仓库自带的 Gradle Wrapper：`./gradlew`）

### 安装 · Installation

通过 git 拉取源码，或直接从仓库 [Releases](https://github.com/Grass-Block/Gluon/releases) 获取已构建的 jar。

Clone the repository, or download a built jar from Releases.

```bash
git clone https://github.com/Grass-Block/Gluon.git
```

在 `settings.gradle` 中引入模块：

```groovy
include 'gluon-main'
```

以 `compileOnly` 依赖的方式将其加入你的项目（把 `gluon-main/` 的构建产物与 `libraries/` 下的 `me.gb2022.commons` 依赖一起引入）：

```groovy
dependencies {
    compileOnly files('libraries/gluon-main.jar')
    compileOnly gbuildLib('me.gb2022.commons:commons-general')
    // 其余 commons-* 按需引入
}
```

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

---

## 使用 · Usage

### 启动上下文 · Bootstrapping the Context

所有能力都通过 `ModularApplicationContext` 这座入口获取。使用 `Builder` 配置，并以 `holder` 关联你的宿主对象（通常是你插件的实例）。

Everything flows through `ModularApplicationContext`. Configure it with the `Builder` and bind a `holder` — typically your plugin/platform instance.

```java
import me.gb2022.gluon.ModularApplicationContext;

public class MyPlugin {
    private final ModularApplicationContext context;

    public MyPlugin() {
        // 1. 构建上下文
        this.context = ModularApplicationContext.builder(this)
                .applicationName("MyApp")
                .build();

        // 2. 注册包：扫描宿主上的 @ApplicationPackageProvider 方法
        this.context.registerPackage(this, MyPackage.class);

        // 3. 启动（启用包 -> 模块 -> 服务）
        this.context.initialize();
    }

    public void onDisable() {
        // 4. 关闭（按逆序优雅卸载）
        this.context.shutdown();
    }
}
```

### 包 · Package

**包（Package）** 是模块与服务的集合载体。通过 `@ApplicationPackageProvider`（可写在静态方法上）定义，利用 `ContentBuilder` 声明该包包含哪些模块与服务。

A **Package** is a collection of modules and services. Define it with `@ApplicationPackageProvider` on a static method, and declare its contents via `ContentBuilder`.

```java
package me.gb2022.gluon.example;

import me.gb2022.gluon.pack.ApplicationPackageProvider;
import me.gb2022.gluon.pack.ContentBuilder;

public final class MyPackage {

    @ApplicationPackageProvider(id = "mypkg", version = "1.0", description = "My example package")
    public static void define(ContentBuilder builder) {
        builder.module(ExampleModule.class);   // 注册一个模块
        builder.service(ExampleService.class); // 注册一个服务
    }
}
```

### 模块 · Module

**模块（Module）** 是最小的功能单元。通过 `@ApplicationModule` 注解声明元数据，并继承 `AbstractModule`（实现 `AppModule`）来覆写生命周期方法。

A **Module** is the smallest unit of functionality. Annotate it with `@ApplicationModule` for metadata and extend `AbstractModule` (which implements `AppModule`) to override the lifecycle hooks.

```java
package me.gb2022.gluon.example;

import me.gb2022.commons.compatibility.APIIncompatibleException;
import me.gb2022.gluon.module.AbstractModule;
import me.gb2022.gluon.module.ApplicationModule;
import me.gb2022.gluon.module.ModuleContainer;
import me.gb2022.gluon.pack.ApplicationPackage;

@ApplicationModule(id = "example", version = "1.1", description = "Example module")
public final class ExampleModule extends AbstractModule {

    @Override
    public void init(String id, ApplicationPackage parent, ModuleContainer handle) {
        super.init(id, parent, handle);   // 框架构造时调用，绑定父包与容器
    }
    
    @Override
    public void enable() throws Exception {
        // 启用：开始工作（可异步启动、注册监听器等）
    }

    @Override
    public void disable() throws Exception {
        // 停用：优雅停机、释放资源
    }

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        // 兼容性校验：不通过则拒绝启用
    }
}
```

> 模块可通过 `owner(Class)` 获取宿主对象，或通过 `handle()` 访问 `ModuleContainer`（其中暴露了元数据、日志器与附件容器）。

#### 模块生命周期 · Module Lifecycle

模块的启停由 `ModuleManager` + `ModuleContainer` 驱动（参考 `ModuleManager.register` 与 `ModuleContainer.construct/enable/disable`）：

The module lifecycle is driven by `ModuleManager` + `ModuleContainer` (see `ModuleManager.register` and `ModuleContainer.construct/enable/disable`):

| 阶段 · Phase | 触发时机 · When | 钩子 · Hook |
| :--- | :--- | :--- |
| **实例化 Instantiation** | 反射构造模块实例 | 构造函数，随后框架调用 `init(id, parent, handle)` |
| **构造 Construct** | 注册后、构造完成时 | `initialize()` |
| **兼容性检查 Compatibility** | 位于构造与启用的**之间** | `checkCompatibility()` |
| **启用 Enable** | 默认启用（或手动 enable） | `enable()` |
| **停用 Disable** | `shutdown()` 或手动 disable | `disable()` |

构造期实际顺序为：**构造函数 → `init(id, parent, handle)` → `initialize()` → `checkCompatibility()`**。其中 `init` 由框架在构造时调用（传占位 id `"__null__"`），用于把模块绑定到父包与容器；`AbstractModule.init` 并非 `final`，可覆写，但**应先调用 `super.init(...)`**。兼容性检查成功后才进入 `ENABLED`；若 `checkCompatibility()` 抛出 `APIIncompatibleException`，模块被标记为 `CONSTRUCT_FAILED` 并拒绝启用。

Construct-time order is actually **constructor → `init(id, parent, handle)` → `initialize()` → `checkCompatibility()`**. `init` is invoked by the framework during construction (with a placeholder id `"__null__"`) to bind the module to its parent package & container. `AbstractModule.init` is **not** `final` — it may be overridden, but you **should call `super.init(...)` first**. Only after the compatibility check passes does the module become `ENABLED`; if `checkCompatibility()` throws `APIIncompatibleException`, the module is flagged `CONSTRUCT_FAILED` and is not enabled.

### 附件与子组件 · Attachments & SubComponents

**附件（Attachment）** 是给「包 / 模块」附加额外能力与状态的扩展机制，按 `Class` 注册在容器上。**子组件（SubComponent）** 则是模块内部更细粒度的功能单元。`ContentBuilder`、`ApplicationPackage`、`ModuleContainer` 都继承自附件容器，因此均可挂载附件。

**Attachments** are the extension mechanism for attaching extra capabilities/state to a **package or module** (registered by `Class`). **SubComponents** are finer-grained functional units *inside* a module. `ContentBuilder`, `ApplicationPackage` and `ModuleContainer` all extend an attachment container, so each of them can carry attachments.

#### 附件容器 · Attachment Container

`AttachmentContainer<A>` 接口提供 `addAttachment / removeAttachment / getAttachment / getAttachments`；`SimpleAttachmentContainer<A>` 是线程安全（`ConcurrentHashMap`）的默认实现，且**同一 `Class` 只能注册一个附件**（重复注册会抛 `IllegalArgumentException`）。

`AttachmentContainer<A>` provides `addAttachment / removeAttachment / getAttachment / getAttachments`; `SimpleAttachmentContainer<A>` is the thread-safe (`ConcurrentHashMap`) default and allows **only one attachment per `Class`** (duplicates throw `IllegalArgumentException`).

- **包附件 · PackageAttachment**：`PackageAttachment`（extends `FunctionalComponent`）是包级附件，通过 `initContext(ctx, pkg)` 绑定上下文与所属包。
- **模块附件 · ModuleAttachment**：`ModuleAttachment`（extends `FunctionalComponent`）是模块级附件，通过 `initContext(ctx, container)` 绑定上下文与所在模块；便捷基类 `AbstractModuleAttachment` 额外暴露 `getModule()` / `getContext()`。

#### 模块子组件 · Module SubComponent

`SubComponent<E>`（abstract，extends `FunctionalComponent`）是模块内的最小功能件，通过父对象 `E` 共享上下文。用 `@ComponentProvider` 标注在 AppModule 类上以声明它导出的子组件列表：

`SubComponent<E>` (abstract, extends `FunctionalComponent`) is the minimal functional unit inside a module, sharing context through a parent object `E`. Annotate the AppModule class with `@ComponentProvider` to declare which sub-components it exports:

```java
package me.gb2022.gluon.example;

import me.gb2022.gluon.module.ApplicationModule;
import me.gb2022.gluon.module.AbstractModule;
import me.gb2022.gluon.module.component.ComponentProvider;
import me.gb2022.gluon.module.component.SubComponent;

@ComponentProvider({ExampleSubComponent.class})
@ApplicationModule(id = "example", version = "1.1")
public final class ExampleComponentModule extends AbstractModule {
    // ...
}

public final class ExampleSubComponent extends SubComponent<ExampleComponentModule> {
    @Override
    public void enable() throws Exception {
        ExampleComponentModule module = parent;   // 父模块上下文
    }
}
```

框架会在 `ModuleManager.handlePreEnable`（模块启用前）调用 `SubComponentHolder.createComponents`，按 `@ComponentProvider` 反射实例化各子组件、执行其 `checkCompatibility()` 并绑定父模块（`ctx(parent)`)；内置的 `ModuleComponentContainer`（一个模块附件，同时实现 `SubComponentHolder`）负责收集这些子组件，并在 enable/disable/checkCompatibility 时委派给它们（子组件独立于模块 `initialize` 生命周期）。

During pre-enable (`ModuleManager.handlePreEnable`) the framework calls `SubComponentHolder.createComponents`, which instantiates each declared `SubComponent`, runs its `checkCompatibility()` and binds the parent module via `ctx(parent)`. The built-in `ModuleComponentContainer` (a module attachment that also implements `SubComponentHolder`) collects them and delegates `enable`/`disable`/`checkCompatibility` to each sub-component — sub-components have their **own lifecycle, separate from the module's `initialize`**.

### 服务 · Service

**服务（Service）** 与模块的区别在于：服务在**整个包的生命周期内提供全局、共享的功能**——所有使用同一服务的调用方共享同一个实例；而模块则是可独立启停的功能单元，随模块的 enable/disable 而启停。用 `@ApplicationService` 声明一个服务接口，通过 `ServiceLayer` 控制其在关闭时的卸载顺序，用 `ServiceInject` 注入、`ServiceProvider` 提供实现。

**Services** differ from modules: a service provides **global, shared functionality across the entire package lifecycle** — every consumer of the same service shares a single instance, whereas a module is an independently toggleable unit that lives and dies with its `enable`/`disable`. Declare a service interface with `@ApplicationService`, control teardown ordering via `ServiceLayer`, and inject/implement it with `ServiceInject` / `ServiceProvider`.

```java
package me.gb2022.gluon.example;

import me.gb2022.gluon.service.ApplicationService;
import me.gb2022.gluon.service.Service;
import me.gb2022.gluon.service.ServiceHolder;
import me.gb2022.gluon.service.ServiceInject;
import me.gb2022.gluon.service.ServiceLayer;

@ApplicationService(id = "example-service", layer = ServiceLayer.USER, export = true)
public interface ExampleService extends Service {

    // 注入点：由框架写入实现实例的静态字段
    @ServiceInject
    ServiceHolder<ExampleService> INSTANCE = new ServiceHolder<>();

    String greet();

    default String profile(String name) {
        return "Hello, " + name;
    }
}
```

> `ServiceLayer` 枚举为 `FOUNDATION → FRAMEWORK → USER`，`shutdown()` 时按此顺序逆序卸载，便于依赖方先停止。

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

---

## 路线图 · Roadmap

- 更完善的依赖解析与模块间依赖声明
- 配置（Configuration）支持与热重载
- 更精细的 Debug / LogProvider 能力

参见 [open issues](https://github.com/Grass-Block/Gluon/issues) 查看完整的特性清单与已知问题。

See the [open issues](https://github.com/Grass-Block/Gluon/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

---

## 参与贡献 · Contributing

欢迎任何形式的贡献！如果你有改进建议：

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. 创建你的特性分支 Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

如果你有好的建议，也可以直接开一个带 `enhancement` 标签的 issue。别忘了给项目点个 Star！

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

---

## 许可证 · License

本项目基于 **LGPL-3.0** 许可分发。较宽松的 LGPL 允许你在自己的项目中自由使用、修改和集成 Gluon。

Distributed under the **LGPL-3.0** License. See [`licence.md`](https://github.com/Grass-Block/Gluon/blob/main/licence.md) for more information.

> 作为库使用（Application 链接）时相对宽松；对 Library 本身（的修改版本）仍需保持开源并遵循 LGPL 条款。

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

---

## 联系方式 · Contact

- Project Link: [https://github.com/Grass-Block/Gluon](https://github.com/Grass-Block/Gluon)
- Issue Tracker: [https://github.com/Grass-Block/Gluon/issues](https://github.com/Grass-Block/Gluon/issues)

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

---

## 致谢 · Acknowledgments

- [Starlight-plugin](https://github.com/Grass-Block/Starlight-plugin) — 本框架的落地参考项目 / reference project built on Gluon
- [me.gb2022.commons](https://github.com/Grass-Block) — 底层通用工具库 / underlying commons libraries
- [Best-README-Template](https://github.com/othneildrew/Best-README-Template) — 本 README 的结构参考 / structure reference
- [Shields.io](https://shields.io) — 徽章 / badges
- `@ATCraftMC` 2020–2024 — 赞助支持 / sponsorship

<p align="right">(<a href="#readme-top">back to top / 返回顶部</a>)</p>

---

<div align="center">

#### Gluon

An open project by GrassBlock2022, sponsored by @ATCraftMC 2020-2024.

</div>
