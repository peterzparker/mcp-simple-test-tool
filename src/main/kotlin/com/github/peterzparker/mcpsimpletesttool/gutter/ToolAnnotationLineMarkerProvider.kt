package com.github.peterzparker.mcpsimpletesttool.gutter

import com.github.peterzparker.mcpsimpletesttool.toolWindow.McpToolRunnerPanel
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import java.util.function.Supplier

/**
 * Gutter Icon 提供者. 识别 @Tool 注解方法.
 */
class ToolAnnotationLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val text = element.text ?: return null
        if (!text.contains("@Tool") && !text.contains("springframework.ai.tool")) return null

        val methodInfo = findMethodInfo(element) ?: return null
        val nameId = methodInfo.nameIdentifier ?: return null

        val project = element.project
        val handler = ClickHandler(project, methodInfo.name)

        // 使用唯一的公开构造器： (T, TextRange, Icon, Function<?, String>?, Handler?, Alignment, Supplier<String>)
        return LineMarkerInfo<PsiElement>(
            nameId,
            nameId.textRange,
            AllIcons.Nodes.Function,
            null,
            handler,
            GutterIconRenderer.Alignment.RIGHT,
            Supplier { "Click to debug @Tool: ${methodInfo.name}" }
        )
    }

    override fun collectSlowLineMarkers(
        elements: MutableList<out PsiElement>,
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        for (element in elements) {
            getLineMarkerInfo(element)?.let { result.add(it) }
        }
    }

    private data class MethodInfo(val name: String, val nameIdentifier: PsiElement?)

    private fun findMethodInfo(element: PsiElement): MethodInfo? {
        var current = element.parent
        while (current != null) {
            if (current.javaClass.name.contains("PsiMethod")) {
                val name = try {
                    current.javaClass.getMethod("getName").invoke(current) as? String
                } catch (e: Exception) { null } ?: return null
                val nameId = try {
                    current.javaClass.getMethod("getNameIdentifier").invoke(current) as? PsiElement
                } catch (e: Exception) { null }
                return MethodInfo(name, nameId)
            }
            current = current.parent
        }
        return null
    }
}

/**
 * Gutter Icon 点击导航.
 */
private class ClickHandler(
    private val project: Project,
    private val toolName: String
) : com.intellij.codeInsight.daemon.GutterIconNavigationHandler<PsiElement> {

    override fun navigate(mouseEvent: java.awt.event.MouseEvent?, elt: PsiElement) {
        val tw = ToolWindowManager.getInstance(project).getToolWindow("MCP Tool Runner") ?: return
        tw.activate({
            val c = tw.contentManager.getContent(0)
            (c?.component as? java.awt.Container)?.let { findToolPanel(it) }
        }, true)
    }

    private fun findToolPanel(c: java.awt.Container) {
        for (i in 0 until c.componentCount) {
            when (val comp = c.getComponent(i)) {
                is McpToolRunnerPanel -> { comp.openToolByName(toolName); return }
                is java.awt.Container -> findToolPanel(comp)
            }
        }
    }
}
