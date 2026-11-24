package com.creatorsclub.engine.ai.matcher

import com.creatorsclub.engine.ai.registry.FeatureCatalog
import com.creatorsclub.engine.ai.registry.RegisteredTool

data class AiAssistantResponse(
    val title: String,
    val explanationText: String,
    val matchedTool: RegisteredTool?,
    val suggestedActionTitle: String?,
    val suggestedStudioId: String?
)

object IntentMatcher {

    /**
     * Universal Level A intent parser.
     * Evaluates query against keywords in English, Hindi, and Hinglish.
     * 100% offline, zero overhead, instantaneous.
     */
    fun matchQuery(query: String): AiAssistantResponse {
        val cleanQuery = query.lowercase().trim()

        // Match against catalog
        val matched = FeatureCatalog.allTools.find { tool ->
            tool.keywords.any { keyword -> cleanQuery.contains(keyword) }
        }

        return if (matched != null) {
            val explanation = buildString {
                appendLine("I can help you with that! Here is the recommended workflow:")
                matched.workflowSteps.forEach { step ->
                    appendLine(step)
                }
            }
            AiAssistantResponse(
                title = matched.nameEn,
                explanationText = explanation.trim(),
                matchedTool = matched,
                suggestedActionTitle = "Open ${matched.nameEn.split(" ").take(2).joinToString(" ")}",
                suggestedStudioId = matched.targetStudioId
            )
        } else {
            AiAssistantResponse(
                title = "Kreavilo Creative Assistant",
                explanationText = "I can guide you across any creative task! Try asking:\n• \"How can I make a product advertisement?\"\n• \"Mujhe video mein subtitles lagane hain\"\n• \"Remove background from my photo\"\n• \"How to draw rangoli or mandala?\"",
                matchedTool = null,
                suggestedActionTitle = "Explore Studios",
                suggestedStudioId = "studios"
            )
        }
    }
}
