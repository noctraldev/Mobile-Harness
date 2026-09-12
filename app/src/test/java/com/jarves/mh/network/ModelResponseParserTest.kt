package com.jarves.mh.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import com.jarves.mh.model.ProviderProtocol

class ProviderApiClientAuthTest {
    @Test
    fun anthropicCompatibleProtocolsUseApiKeyHeaderNotBearer() {
        val headers = ProviderApiClient().requestHeaders("key", ProviderProtocol.ANTHROPIC_GATEWAY)

        assertEquals("key", headers["x-api-key"])
        assertEquals("2023-06-01", headers["anthropic-version"])
        assertFalse(headers.containsKey("Authorization"))
    }

    @Test
    fun openAiCompatibleProtocolsUseBearerHeader() {
        val headers = ProviderApiClient().requestHeaders("key", ProviderProtocol.OPENAI_CHAT)

        assertEquals("Bearer key", headers["Authorization"])
        assertFalse(headers.containsKey("x-api-key"))
    }
}

class ModelResponseParserTest {
    @Test
    fun parsesOpenAiStyleDataList() {
        val models = ModelResponseParser.parse(
            """{"data":[{"id":"model-b"},{"id":"model-a","display_name":"Model A"}]}""",
        )

        assertEquals(listOf("model-a", "model-b"), models.map { it.id })
        assertEquals("Model A", models.first().displayName)
    }

    @Test
    fun parsesModelsAndStringArrays() {
        assertEquals(
            listOf("alpha"),
            ModelResponseParser.parse("""{"models":[{"name":"alpha"}]}""").map { it.id },
        )
        assertEquals(
            listOf("alpha", "beta"),
            ModelResponseParser.parse("""["beta","alpha"]""").map { it.id },
        )
    }

    @Test
    fun malformedResponseReturnsEmptyList() {
        assertEquals(emptyList<DiscoveredModel>(), ModelResponseParser.parse("not json"))
    }
}
