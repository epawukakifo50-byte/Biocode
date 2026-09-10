package com.biocode.engine

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.biocode.app.R

/**
 * 5 ЦВЕТОВЫХ ТЕМ BIOCODE
 */
enum class BiocodeThemeMode(
    val title: String,
    val subtitle: String,
    val codeIndex: String
) {
    NOGUCHI_GREEN("NOGUCHI MOSS", "Био-лайм и хвойный саге", "01/05"),
    CYBER_PINK("CYBER PINK", "Неоновый пинк и графит", "02/05"),
    CRIMSON_NAVY("CRIMSON NAVY", "Алый кримсон и бездонный нейви", "03/05"),
    FIG_CHARTREUSE("FIG CHARTREUSE", "Инжирный баклажан и шартрез", "04/05"),
    WOOD_CYAN("NORDIC WOOD", "Березовый шпон, графит и золото", "05/05");

    fun next(): BiocodeThemeMode {
        val values = entries
        val nextIndex = (ordinal + 1) % values.size
        return values[nextIndex]
    }
}

/**
 * НАБОР ТОКЕНОВ ТЕМЫ
 */
data class BiocodeColors(
    val noguchiCream: Color,
    val bioLime: Color,
    val pineTeal: Color,
    val darkMoss: Color,
    val deepPineBg: Color,
    val spruceDeck: Color,
    val deckBorder: Color,
    val deckBorderSubtle: Color,
    val noguchiBorder: Color,
    val limeBorder: Color,
    val pillActiveBg: Color,
    val pillInactiveBg: Color,
    val pillCreamBg: Color,
    val pillDarkBg: Color,
    val macroProtein: Color,
    val macroFat: Color,
    val lipidAmber: Color,
    val macroCarbs: Color,
    val macroCarbsLight: Color,
    val macroWater: Color,
    val macroCalories: Color
)

fun getBiocodeThemeColors(mode: BiocodeThemeMode): BiocodeColors {
    return when (mode) {
        BiocodeThemeMode.NOGUCHI_GREEN -> BiocodeColors(
            noguchiCream = Color(0xFFFFFEEF),
            bioLime = Color(0xFF9FD700),
            pineTeal = Color(0xFF446158),
            darkMoss = Color(0xFF272C1A),
            deepPineBg = Color(0xFF061512),
            spruceDeck = Color(0xFF0A2620),
            deckBorder = Color(0xFF1E5448),
            deckBorderSubtle = Color(0xFF133B32),
            noguchiBorder = Color(0xFF3B4D36),
            limeBorder = Color(0xFF86B502),
            pillActiveBg = Color(0xFF123830),
            pillInactiveBg = Color(0xFF071916),
            pillCreamBg = Color(0xFFFFFEEF),
            pillDarkBg = Color(0xFF1C2213),
            macroProtein = Color(0xFF9FD700),
            macroFat = Color(0xFFE5B842),
            lipidAmber = Color(0xFFE5B842),
            macroCarbs = Color(0xFF446158),
            macroCarbsLight = Color(0xFF729E91),
            macroWater = Color(0xFF4AE3B5),
            macroCalories = Color(0xFFFFFEEF)
        )
        BiocodeThemeMode.CYBER_PINK -> BiocodeColors(
            // Референс 1: #ff096c, #f2088a, #2a3843, #4f6172, #192731
            noguchiCream = Color(0xFFFFFEEF),
            bioLime = Color(0xFFFF096C),             // Неоновый пинк
            pineTeal = Color(0xFF2A3843),            // Сланцево-синий графит
            darkMoss = Color(0xFF192731),            // Глубокий ночной уголь
            deepPineBg = Color(0xFF0E171E),
            spruceDeck = Color(0xFF1F2E3A),
            deckBorder = Color(0xFF3F5568),
            deckBorderSubtle = Color(0xFF2B3A47),
            noguchiBorder = Color(0xFF394D5E),
            limeBorder = Color(0xFFFF2B82),
            pillActiveBg = Color(0xFF321A28),
            pillInactiveBg = Color(0xFF14191F),
            pillCreamBg = Color(0xFFFFFEEF),
            pillDarkBg = Color(0xFF19222B),
            macroProtein = Color(0xFFFF096C),        // Белки: Неоновый пинк
            macroFat = Color(0xFFF2088A),            // Жиры: Маджента
            lipidAmber = Color(0xFFF2088A),
            macroCarbs = Color(0xFF4F6172),          // Углеводы: Холодный сланец
            macroCarbsLight = Color(0xFF7991A6),
            macroWater = Color(0xFF00E5FF),
            macroCalories = Color(0xFFFFFEEF)
        )
        BiocodeThemeMode.CRIMSON_NAVY -> BiocodeColors(
            // Референс 2: #e6223a, #690722, #621d43, #644765, #374365, #062a48, #03213d
            noguchiCream = Color(0xFFFFF8FA),
            bioLime = Color(0xFFE6223A),             // Алый кримсон
            pineTeal = Color(0xFF690722),            // Винный бордо
            darkMoss = Color(0xFF062A48),            // Полночный синий
            deepPineBg = Color(0xFF03213D),          // Бездонный нейви
            spruceDeck = Color(0xFF133658),
            deckBorder = Color(0xFF8B1234),
            deckBorderSubtle = Color(0xFF2C4A6F),
            noguchiBorder = Color(0xFF1C4269),
            limeBorder = Color(0xFFFF3B53),
            pillActiveBg = Color(0xFF3A1220),
            pillInactiveBg = Color(0xFF071B2E),
            pillCreamBg = Color(0xFFFFF8FA),
            pillDarkBg = Color(0xFF092036),
            macroProtein = Color(0xFFE6223A),        // Белки: Алый кримсон
            macroFat = Color(0xFFE07A5F),            // Жиры: Теракота/коралл
            lipidAmber = Color(0xFFE07A5F),
            macroCarbs = Color(0xFF374365),          // Углеводы: Индиго сланец
            macroCarbsLight = Color(0xFF644765),
            macroWater = Color(0xFF4CC9F0),
            macroCalories = Color(0xFFFFF8FA)
        )
        BiocodeThemeMode.FIG_CHARTREUSE -> BiocodeColors(
            // Референс 3: #b6c648, #dfe6b0, #543a55, #260729, #a091a3
            noguchiCream = Color(0xFFDFE6B0),        // Нежный фисташковый крем
            bioLime = Color(0xFFB6C648),             // Кислотный шартрез
            pineTeal = Color(0xFF543A55),            // Инжирный баклажан
            darkMoss = Color(0xFF260729),            // Глубокая ежевика
            deepPineBg = Color(0xFF170419),
            spruceDeck = Color(0xFF38143C),
            deckBorder = Color(0xFF724F75),
            deckBorderSubtle = Color(0xFF451E4B),
            noguchiBorder = Color(0xFF4C2153),
            limeBorder = Color(0xFFCBE042),
            pillActiveBg = Color(0xFF332036),
            pillInactiveBg = Color(0xFF1A0A1D),
            pillCreamBg = Color(0xFFDFE6B0),
            pillDarkBg = Color(0xFF230B27),
            macroProtein = Color(0xFFB6C648),        // Белки: Шартрез
            macroFat = Color(0xFFE3A857),            // Жиры: Теплый янтарь
            lipidAmber = Color(0xFFE3A857),
            macroCarbs = Color(0xFFA091A3),          // Углеводы: Лавандовый сланец
            macroCarbsLight = Color(0xFFC7BAC9),
            macroWater = Color(0xFF64DFDF),
            macroCalories = Color(0xFFDFE6B0)
        )
        BiocodeThemeMode.WOOD_CYAN -> BiocodeColors(
            // Референс 4 «на нюансе»: #f9e7d2 (Young Wood), #121819 (Charcoal), #039692 (Cyan), #f7c800 (Yellow Gold)
            noguchiCream = Color(0xFFF9E7D2),        // Теплый березовый шпон (Young Wood)
            bioLime = Color(0xFFE5B842),             // Благородное теплое золото (Yellow Gold)
            pineTeal = Color(0xFF1B292B),            // Сдержанный сланцево-нефтяной графит (без ядовитого циана!)
            darkMoss = Color(0xFF101617),            // Глубокий угольный обсидиан (Charcoal)
            deepPineBg = Color(0xFF0A0F10),
            spruceDeck = Color(0xFF152022),          // Темный графитовый сланец
            deckBorder = Color(0xFF1B4849),          // Тонкая окантовка патинированного циана
            deckBorderSubtle = Color(0xFF122C2D),
            noguchiBorder = Color(0xFF203537),
            limeBorder = Color(0xFFF2C95C),          // Золотистая окантовка
            pillActiveBg = Color(0xFF1D3537),
            pillInactiveBg = Color(0xFF0E1516),
            pillCreamBg = Color(0xFFF9E7D2),
            pillDarkBg = Color(0xFF131D1F),
            macroProtein = Color(0xFFE5B842),        // Белки: Золото
            macroFat = Color(0xFF039692),            // Жиры: Благородный циан
            lipidAmber = Color(0xFFE5A030),
            macroCarbs = Color(0xFFD6C5B3),          // Углеводы: Текстурированный шпон
            macroCarbsLight = Color(0xFFEAE0D5),
            macroWater = Color(0xFF039692),
            macroCalories = Color(0xFFF9E7D2)
        )
    }
}

/**
 * ОФИЦИАЛЬНАЯ РЕАКТИВНАЯ ЦВЕТОВАЯ ПАЛИТРА BIOCODE
 * Делегирует ко всем свойствам текущей анимируемой палитры.
 */
object BiocodePalette {
    var current: BiocodeColors by mutableStateOf(getBiocodeThemeColors(BiocodeThemeMode.NOGUCHI_GREEN))

    val NoguchiCream: Color get() = current.noguchiCream
    val BioLime: Color get() = current.bioLime
    val PineTeal: Color get() = current.pineTeal
    val DarkMoss: Color get() = current.darkMoss
    val DeepPineBg: Color get() = current.deepPineBg
    val SpruceDeck: Color get() = current.spruceDeck
    val DeckBorder: Color get() = current.deckBorder
    val DeckBorderSubtle: Color get() = current.deckBorderSubtle
    val NoguchiBorder: Color get() = current.noguchiBorder
    val LimeBorder: Color get() = current.limeBorder
    val PillActiveBg: Color get() = current.pillActiveBg
    val PillInactiveBg: Color get() = current.pillInactiveBg
    val PillCreamBg: Color get() = current.pillCreamBg
    val PillDarkBg: Color get() = current.pillDarkBg
    val MacroProtein: Color get() = current.macroProtein
    val MacroFat: Color get() = current.macroFat
    val LipidAmber: Color get() = current.lipidAmber
    val MacroCarbs: Color get() = current.macroCarbs
    val MacroCarbsLight: Color get() = current.macroCarbsLight
    val MacroWater: Color get() = current.macroWater
    val MacroCalories: Color get() = current.macroCalories

    // Дополнительные свойства для обратной совместимости
    val WarmSandCard: Color get() = current.spruceDeck
    val WarmSandBorder: Color get() = current.deckBorder
    val AccentMint: Color get() = current.macroWater
    val AccentPeach: Color get() = current.lipidAmber
    val AccentCopper: Color get() = current.lipidAmber
    val SafeGreen: Color get() = current.bioLime
}

/**
 * CompositionLocal для динамического доступа к палитре Biocode
 */
val LocalBiocodeColors = compositionLocalOf { BiocodePalette.current }

val LiquidasiFont = FontFamily(Font(R.font.liquidasi))

/**
 * БАЗОВАЯ ТИПОГРАФИКА BIOCODE
 */
object BiocodeTypography {
    val HeaderBrand: TextStyle
        get() = TextStyle(
            fontFamily = LiquidasiFont,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            letterSpacing = 2.5.sp,
            color = BiocodePalette.NoguchiCream
        )

    val MonospaceTitle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp
    )

    val BentoHeader = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.8.sp
    )

    val TabLabel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    )

    val TelemetryLabel = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        letterSpacing = 1.5.sp
    )

    val ValueNumber = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )
}

