package com.biocode.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biocode.engine.BiocodeBentoCard
import com.biocode.engine.BiocodeBlueprintCanvas
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.DailyNutritionState
import com.biocode.engine.LucidePlus
import com.biocode.engine.LucideSparkles
import com.biocode.engine.MealEntry
import com.biocode.engine.MealType

data class MealBlueprint(
    val name: String,
    val calories: Int,
    val protein: Float,
    val fat: Float,
    val carbs: Float,
    val tag: String,
    val description: String
)

/**
 * 🥗 2. ЭКРАН «ЧТО ПОЕСТЬ» (MEAL BUILDER)
 */
@Composable
fun MealBuilderScreen(
    state: DailyNutritionState,
    onAddMeal: (MealEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val blueprints = listOf(
        MealBlueprint("Лосось на пару и киноа со спаржей", 540, 44f, 16f, 52f, "АНАБОЛИЧЕСКИЙ ЛАНЧ", "Омега-3 и медленные углеводы для стабильного гликемического профиля."),
        MealBlueprint("Стейк индейки с печеным бататом", 480, 48f, 8f, 54f, "ВОССТАНОВИТЕЛЬНЫЙ УЖИН", "Максимальная концентрация аминокислот без избыточных липидов."),
        MealBlueprint("Матча-боул с чиа и сывороточным протеином", 310, 32f, 7f, 28f, "ЭНЕРГО-ПЕРЕКУС", "Антиоксиданты зеленого чая и легкий гидролизат белка."),
        MealBlueprint("Омлет из 4 белков с авокадо и томатами", 360, 28f, 18f, 14f, "УТРЕННИЙ КЕТО-СТАРТ", "Здоровые мононенасыщенные жиры и чистый альбумин.")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BiocodePalette.DarkMoss)
    ) {
        BiocodeBlueprintCanvas(
            modifier = Modifier.fillMaxSize(),
            gridPitchDp = 24.dp,
            lineColor = BiocodePalette.PineTeal.copy(alpha = 0.25f),
            crossColor = BiocodePalette.BioLime.copy(alpha = 0.20f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // КРУПНЫЙ ЗАГОЛОВОК BIOCODE ШРИФТОМ LIQUIDASI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "BIOCODE",
                    style = BiocodeTypography.HeaderBrand.copy(fontSize = 38.sp),
                    color = BiocodePalette.NoguchiCream
                )
            }

            Text(
                text = "БИО-КОНСТРУКТОР // МАКРО-ПОДБОР",
                style = BiocodeTypography.TelemetryLabel,
                color = BiocodePalette.BioLime,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                blueprints.forEach { bp ->
                    BiocodeBentoCard(
                        title = bp.tag,
                        badgeText = "${bp.calories} KCAL",
                        badgeColor = BiocodePalette.BioLime,
                        backgroundColor = BiocodePalette.PineTeal,
                        borderColor = BiocodePalette.DeckBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = bp.name,
                                style = BiocodeTypography.TabLabel.copy(fontSize = 13.sp),
                                color = BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bp.description,
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                color = BiocodePalette.NoguchiCream.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Б: ${bp.protein.toInt()}г", color = BiocodePalette.BioLime, style = BiocodeTypography.TelemetryLabel, fontWeight = FontWeight.Bold)
                                    Text("Ж: ${bp.fat.toInt()}г", color = BiocodePalette.MacroFat, style = BiocodeTypography.TelemetryLabel, fontWeight = FontWeight.Bold)
                                    Text("У: ${bp.carbs.toInt()}г", color = BiocodePalette.NoguchiCream, style = BiocodeTypography.TelemetryLabel, fontWeight = FontWeight.Bold)
                                }

                                // Кнопка быстрого добавления в дневник
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(BiocodePalette.BioLime)
                                        .clickable {
                                            onAddMeal(
                                                MealEntry(
                                                    id = System.currentTimeMillis().toString(),
                                                    name = bp.name,
                                                    type = MealType.LUNCH,
                                                    calories = bp.calories,
                                                    protein = bp.protein,
                                                    fat = bp.fat,
                                                    carbs = bp.carbs,
                                                    time = "13:00"
                                                )
                                            )
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        LucidePlus(modifier = Modifier.size(12.dp), tint = BiocodePalette.DarkMoss)
                                        Text(
                                            text = "В ДНЕВНИК",
                                            style = BiocodeTypography.TelemetryLabel,
                                            color = BiocodePalette.DarkMoss,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(86.dp))
            }
        }
    }
}
