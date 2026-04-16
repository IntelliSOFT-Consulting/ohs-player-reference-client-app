package dev.ohs.player.reference.client.app.configuration


import dev.ohs.player.reference.client.app.models.AppConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.test.*

class ConfigurationTest {

    private lateinit var configManager: ConfigurationManager
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setup() {
        configManager = ConfigurationManager()
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun testLoadValidConfiguration() = runTest {
        // Load the actual config
        configManager.loadConfiguration()

        val config = configManager.config.value
        assertNotNull(config, "Config should not be null")

        // Verify all fields
        assertEquals("ohs-reference-app", config.appId)
        assertEquals("application", config.configType)
        assertEquals("OHS Player", config.appTitle)
        assertEquals(100, config.remoteSyncPageSize)
        assertEquals(listOf("en", "sw", "fr"), config.languages)
        assertEquals(30, config.syncInterval)

        // Verify nested config
        assertTrue(config.loginConfig.showLogo)
        assertTrue(config.loginConfig.enablePin)
    }

    @Test
    fun testConfigurationDefaults() = runTest {
        // Test default values when config fails to load
        val defaultConfig = configManager.getDefaultConfig()

        assertEquals("ohs-reference-app", defaultConfig.appId)
        assertEquals("OHS Player", defaultConfig.appTitle)
        assertEquals(listOf("en", "sw", "fr"), defaultConfig.languages)
    }

    @Test
    fun testConvenienceMethods() = runTest {
        configManager.loadConfiguration()

        assertEquals("OHS Player", configManager.getAppTitle())
        assertEquals(listOf("en", "sw", "fr"), configManager.getLanguages())
        assertEquals(30, configManager.getSyncInterval())
        assertTrue(configManager.shouldShowLogo())
        assertTrue(configManager.isPinEnabled())
        assertEquals(100, configManager.getRemoteSyncPageSize())
    }

    @Test
    fun testJsonParsing() {
        val jsonString = """
            {
                "appId": "test-app",
                "configType": "test",
                "appTitle": "Test App",
                "remoteSyncPageSize": 50,
                "languages": ["en", "es"],
                "syncInterval": 60,
                "loginConfig": {
                    "showLogo": false,
                    "enablePin": false
                }
            }
        """.trimIndent()

        val config = json.decodeFromString<AppConfig>(jsonString)

        assertEquals("test-app", config.appId)
        assertEquals("Test App", config.appTitle)
        assertEquals(listOf("en", "es"), config.languages)
        assertFalse(config.loginConfig.showLogo)
        assertFalse(config.loginConfig.enablePin)
    }

    @Test
    fun testConfigStateFlow() = runTest {
        // Test that StateFlow updates correctly
        val emissions = mutableListOf<AppConfig?>()

        // Start collecting in a background job
        val job = launch {
            configManager.config.collect { value ->
                emissions.add(value)
            }
        }

        // Give a moment for collection to start
        delay(50)

        // Initial emission should be null
        assertEquals(1, emissions.size)
        assertNull(emissions[0])

        // Load configuration
        configManager.loadConfiguration()

        // Wait for the update
        delay(100)

        // Should have second emission with the config
        assertEquals(2, emissions.size)
        assertNotNull(emissions[1])
        assertEquals("OHS Player", emissions[1]?.appTitle)

        job.cancel()
    }
}