import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension
    intellijPlatform {
        intellijIdea("2025.2.6.2")
        testFramework(TestFrameworkType.Platform)
        // Java PSI classes are included with intellijIdea
    }

    // MCP (Model Context Protocol) Java SDK - 官方SDK，用于MCP协议通信
    implementation("io.modelcontextprotocol.sdk:mcp:2.0.0")

    // Kotlin Coroutines - 用于异步网络请求与UI更新
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")

    // JSON 处理
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
}
