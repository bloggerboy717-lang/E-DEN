package com.example.ui.orb

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class PlanetTheme(
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val hasRings: Boolean,
    val ringColor: Color = glowColor,
    val description: String = ""
)

object PlanetPresets {
    val Saturn = PlanetTheme(
        name = "Saturn",
        primaryColor = SaturnGold,
        secondaryColor = SaturnBrown,
        glowColor = SaturnGlow,
        hasRings = true,
        ringColor = SaturnGold,
        description = "Majestic golden rings and atmospheric amber bands"
    )

    val Earth = PlanetTheme(
        name = "Earth",
        primaryColor = EarthBlue,
        secondaryColor = EarthGreen,
        glowColor = EarthGlow,
        hasRings = false,
        ringColor = EarthGlow,
        description = "Azure oceans with emerald continental contours"
    )

    val Mars = PlanetTheme(
        name = "Mars",
        primaryColor = MarsRed,
        secondaryColor = MarsRust,
        glowColor = MarsGlow,
        hasRings = false,
        ringColor = MarsGlow,
        description = "Fiery red dunes and iron-rich crimson atmosphere"
    )

    val Jupiter = PlanetTheme(
        name = "Jupiter",
        primaryColor = JupiterOchre,
        secondaryColor = JupiterCream,
        glowColor = JupiterGlow,
        hasRings = true,
        ringColor = JupiterCream,
        description = "Grand swirling gas bands and colossal storms"
    )

    val Neptune = PlanetTheme(
        name = "Neptune",
        primaryColor = NeptuneBlue,
        secondaryColor = NeptuneCyan,
        glowColor = NeptuneGlow,
        hasRings = true,
        ringColor = NeptuneCyan,
        description = "Deep icy cobalt with crystalline atmospheric ring"
    )

    val Nebula = PlanetTheme(
        name = "Nebula",
        primaryColor = NebulaPurple,
        secondaryColor = NebulaPink,
        glowColor = NebulaGlow,
        hasRings = true,
        ringColor = NebulaPink,
        description = "Interstellar stardust with iridescent violet aura"
    )

    val Sun = PlanetTheme(
        name = "Sun",
        primaryColor = SunGold,
        secondaryColor = SunOrange,
        glowColor = SunGlow,
        hasRings = false,
        ringColor = SunGlow,
        description = "Solar incandescent flare with shimmering corona"
    )

    val Moon = PlanetTheme(
        name = "Moon",
        primaryColor = MoonSlate,
        secondaryColor = MoonSilver,
        glowColor = MoonGlow,
        hasRings = false,
        ringColor = MoonGlow,
        description = "Cratered silvery slate with quiet moonlight glow"
    )

    val all = listOf(Saturn, Earth, Mars, Jupiter, Neptune, Nebula, Sun, Moon)

    fun getByName(name: String): PlanetTheme {
        return all.find { it.name.equals(name, ignoreCase = true) } ?: Saturn
    }
}
