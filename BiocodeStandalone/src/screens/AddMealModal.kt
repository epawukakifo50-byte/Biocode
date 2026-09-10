package com.biocode.app.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.biocode.ai.GeminiNutritionService
import com.biocode.engine.BiocodePalette
import com.biocode.engine.BiocodeTypography
import com.biocode.engine.LucideCamera
import com.biocode.engine.LucideCheck
import com.biocode.engine.LucideImage
import com.biocode.engine.LucideKey
import com.biocode.engine.LucidePlus
import com.biocode.engine.LucideSparkles
import com.biocode.engine.LucideTrash2
import com.biocode.engine.LucideX
import com.biocode.engine.MealEntry
import com.biocode.engine.MealType
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AddMealModal(
    onDismiss: () -> Unit,
    onSaveMeal: (MealEntry) -> Unit,
    initialPhotoLaunch: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Поля ввода
    var mealName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var weightGramsStr by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MealType.LUNCH) }

    // Фото и ИИ состояние
    var photoUriStr by remember { mutableStateOf<String?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }
    var aiStatusBanner by remember { mutableStateOf<String?>(null) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    // Лаунчер галереи (Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUriStr = uri.toString()
            aiStatusBanner = null
        }
    }

    // Лаунчер камеры (TakePicture)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            photoUriStr = tempCameraUri.toString()
            aiStatusBanner = null
        }
    }

    fun launchCamera() {
        try {
            val photoFile = File(context.cacheDir, "camera_meal_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("AddMealModal", "Error launching camera: ${e.message}")
        }
    }

    // Если модал открыт по кнопке PHOTO — сразу предлагаем камеру
    LaunchedEffect(Unit) {
        if (initialPhotoLaunch) {
            launchCamera()
        }
    }

    // Битмап предпросмотра
    val previewBitmap = remember(photoUriStr) {
        if (!photoUriStr.isNullOrBlank()) {
            try {
                val uri = Uri.parse(photoUriStr)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Пульсирующая анимация для кнопки ИИ
    val infiniteTransition = rememberInfiniteTransition(label = "ai_pulse")
    val aiGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_glow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp))
                .background(BiocodePalette.DarkMoss)
                .border(1.5.dp, BiocodePalette.PineTeal, RoundedCornerShape(26.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. ЗАГОЛОВОК С КНОПКОЙ КЛЮЧА И ЗАКРЫТИЕМ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BiocodePalette.PineTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            LucideSparkles(modifier = Modifier.size(16.dp), tint = BiocodePalette.BioLime)
                        }
                        Column {
                            Text(
                                text = "НОВЫЙ ПРИЕМ ПИЩИ",
                                style = BiocodeTypography.TabLabel,
                                color = BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "БИО-ЛОГИРОВАНИЕ & ИИ-РАСЧЕТ",
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                                color = BiocodePalette.BioLime
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Кнопка настроек API-ключа
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BiocodePalette.SpruceDeck)
                                .border(1.dp, BiocodePalette.DeckBorder.copy(alpha = 0.5f), CircleShape)
                                .clickable { showApiKeyDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            LucideKey(modifier = Modifier.size(15.dp), tint = BiocodePalette.BioLime)
                        }

                        // Закрыть
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BiocodePalette.SpruceDeck)
                                .border(1.dp, BiocodePalette.DeckBorder.copy(alpha = 0.5f), CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            LucideX(modifier = Modifier.size(16.dp), tint = BiocodePalette.NoguchiCream)
                        }
                    }
                }

                // 2. ВЫБОР ТИПА ПРИЕМА ПИЩИ (УТРЕННИЙ, ДНЕВНОЙ, ВЕЧЕРНИЙ, ПЕРЕКУС)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf(
                        MealType.BREAKFAST to "ЗАВТРАК",
                        MealType.LUNCH to "ОБЕД",
                        MealType.DINNER to "УЖИН",
                        MealType.SNACK to "СНЕК"
                    )

                    types.forEach { (type, label) ->
                        val isSel = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) BiocodePalette.BioLime else BiocodePalette.PineTeal.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    if (isSel) BiocodePalette.BioLime else BiocodePalette.DeckBorder.copy(alpha = 0.4f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedType = type
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                color = if (isSel) BiocodePalette.DarkMoss else BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. БЛОК ФОТОФИКСАЦИИ (КАМЕРА / ГАЛЕРЕЯ / ПРЕВЬЮ)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BiocodePalette.SpruceDeck)
                        .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    if (previewBitmap != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, BiocodePalette.BioLime.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            ) {
                                Image(
                                    bitmap = previewBitmap.asImageBitmap(),
                                    contentDescription = "Превью блюда",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BiocodePalette.DarkMoss.copy(alpha = 0.85f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ФОТО ПРИКРЕПЛЕНО",
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 7.5.sp),
                                        color = BiocodePalette.BioLime
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BiocodePalette.PineTeal)
                                        .clickable { launchCamera() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("ПЕРЕСНЯТЬ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp), color = BiocodePalette.NoguchiCream)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BiocodePalette.LipidAmber.copy(alpha = 0.2f))
                                        .border(1.dp, BiocodePalette.LipidAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .clickable { photoUriStr = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("УДАЛИТЬ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp), color = BiocodePalette.LipidAmber)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Кнопка Камера
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BiocodePalette.PineTeal)
                                    .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(10.dp))
                                    .clickable { launchCamera() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LucideCamera(modifier = Modifier.size(16.dp), tint = BiocodePalette.BioLime)
                                    Text(
                                        text = "КАМЕРА",
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                        color = BiocodePalette.NoguchiCream,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Кнопка Галерея
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BiocodePalette.PineTeal)
                                    .border(1.dp, BiocodePalette.DeckBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LucideImage(modifier = Modifier.size(16.dp), tint = BiocodePalette.BioLime)
                                    Text(
                                        text = "ГАЛЕРЕЯ",
                                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 9.sp),
                                        color = BiocodePalette.NoguchiCream,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. НАЗВАНИЕ БЛЮДА
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Название блюда", style = BiocodeTypography.TelemetryLabel) },
                    placeholder = { Text("Например: Боул с форелью и киноа", color = BiocodePalette.NoguchiCream.copy(alpha = 0.3f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiocodePalette.BioLime,
                        unfocusedBorderColor = BiocodePalette.DeckBorder,
                        focusedTextColor = BiocodePalette.NoguchiCream,
                        unfocusedTextColor = BiocodePalette.NoguchiCream,
                        focusedLabelColor = BiocodePalette.BioLime,
                        unfocusedLabelColor = BiocodePalette.NoguchiCream.copy(alpha = 0.6f),
                        cursorColor = BiocodePalette.BioLime
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. ОПИСАНИЕ И ИНГРЕДИЕНТЫ
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание и состав", style = BiocodeTypography.TelemetryLabel) },
                    placeholder = { Text("Ингредиенты, соусы, способ готовки...", color = BiocodePalette.NoguchiCream.copy(alpha = 0.3f)) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BiocodePalette.BioLime,
                        unfocusedBorderColor = BiocodePalette.DeckBorder,
                        focusedTextColor = BiocodePalette.NoguchiCream,
                        unfocusedTextColor = BiocodePalette.NoguchiCream,
                        focusedLabelColor = BiocodePalette.BioLime,
                        unfocusedLabelColor = BiocodePalette.NoguchiCream.copy(alpha = 0.6f),
                        cursorColor = BiocodePalette.BioLime
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. ПРИМЕРНАЯ ГРАММОВКА + ПРЕСЕТЫ
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = weightGramsStr,
                            onValueChange = { weightGramsStr = it.filter { c -> c.isDigit() } },
                            label = { Text("Примерный вес (г)", style = BiocodeTypography.TelemetryLabel) },
                            placeholder = { Text("300", color = BiocodePalette.NoguchiCream.copy(alpha = 0.3f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BiocodePalette.BioLime,
                                unfocusedBorderColor = BiocodePalette.DeckBorder,
                                focusedTextColor = BiocodePalette.NoguchiCream,
                                unfocusedTextColor = BiocodePalette.NoguchiCream,
                                focusedLabelColor = BiocodePalette.BioLime,
                                cursorColor = BiocodePalette.BioLime
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Быстрые чипсы веса
                        listOf("150г", "250г", "350г", "500г").forEach { chip ->
                            val chipVal = chip.replace("г", "")
                            val isSel = weightGramsStr == chipVal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) BiocodePalette.BioLime else BiocodePalette.SpruceDeck)
                                    .border(1.dp, if (isSel) BiocodePalette.BioLime else BiocodePalette.DeckBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        weightGramsStr = chipVal
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chip,
                                    style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp),
                                    color = if (isSel) BiocodePalette.DarkMoss else BiocodePalette.NoguchiCream,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 7. АКЦЕНТНАЯ КНОПКА «✨ РАССЧИТАТЬ С ИИ»
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BiocodePalette.BioLime.copy(alpha = if (isAiLoading) 0.6f else aiGlowAlpha))
                        .border(1.5.dp, BiocodePalette.BioLime, RoundedCornerShape(24.dp))
                        .clickable(enabled = !isAiLoading) {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            coroutineScope.launch {
                                isAiLoading = true
                                aiStatusBanner = "ИИ-СКАНИРОВАНИЕ БИО-СОСТАВА..."
                                val res = GeminiNutritionService.analyzeDish(
                                    context = context,
                                    rawName = mealName,
                                    rawDescription = description,
                                    rawWeightGrams = weightGramsStr.toIntOrNull(),
                                    photoUriStr = photoUriStr
                                )
                                isAiLoading = false
                                mealName = res.name
                                if (description.isBlank() || res.isRealAi) {
                                    description = res.description
                                }
                                weightGramsStr = res.weightGrams.toString()
                                caloriesStr = res.calories.toString()
                                proteinStr = res.protein.toInt().toString()
                                fatStr = res.fat.toInt().toString()
                                carbsStr = res.carbs.toInt().toString()
                                aiStatusBanner = res.statusMessage
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BiocodePalette.DarkMoss,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "АНАЛИЗ БИО-МАТРИЦЫ...",
                                style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            LucideSparkles(modifier = Modifier.size(17.dp), tint = BiocodePalette.DarkMoss)
                            Text(
                                text = "РАССЧИТАТЬ С ПОМОЩЬЮ ИИ",
                                style = BiocodeTypography.TabLabel.copy(fontSize = 11.sp),
                                color = BiocodePalette.DarkMoss,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Статус-бейдж ИИ
                AnimatedVisibility(
                    visible = aiStatusBanner != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BiocodePalette.SpruceDeck)
                            .border(1.dp, BiocodePalette.BioLime.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ ${aiStatusBanner ?: ""}",
                            style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.sp),
                            color = BiocodePalette.BioLime
                        )
                    }
                }

                // 8. РЕДАКТИРУЕМЫЕ ПОЛЯ КБЖУ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = caloriesStr,
                        onValueChange = { caloriesStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Ккал", style = BiocodeTypography.TelemetryLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.BioLime,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.NoguchiCream,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.BioLime,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.weight(1.1f)
                    )

                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Белки", style = BiocodeTypography.TelemetryLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.BioLime,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.BioLime,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.BioLime,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Жиры", style = BiocodeTypography.TelemetryLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.MacroFat,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.MacroFat,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.MacroFat,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Углеводы", style = BiocodeTypography.TelemetryLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.NoguchiCream,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.NoguchiCream,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.NoguchiCream,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 9. КНОПКА «ДОБАВИТЬ В ДНЕВНИК»
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BiocodePalette.BioLime)
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            val cals = caloriesStr.toIntOrNull() ?: 250
                            val prot = proteinStr.toFloatOrNull() ?: 18f
                            val fat = fatStr.toFloatOrNull() ?: 8f
                            val carb = carbsStr.toFloatOrNull() ?: 24f
                            val grams = weightGramsStr.toIntOrNull()
                            val title = mealName.ifBlank { "Прием пищи" }
                            val currentTimeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

                            val entry = MealEntry(
                                id = System.currentTimeMillis().toString(),
                                name = title,
                                type = selectedType,
                                calories = cals,
                                protein = prot,
                                fat = fat,
                                carbs = carb,
                                time = currentTimeStr,
                                description = description,
                                weightGrams = grams,
                                photoUri = photoUriStr
                            )
                            onSaveMeal(entry)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LucidePlus(modifier = Modifier.size(16.dp), tint = BiocodePalette.DarkMoss)
                        Text(
                            text = "ДОБАВИТЬ В ДНЕВНИК",
                            style = BiocodeTypography.TabLabel,
                            color = BiocodePalette.DarkMoss,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // ДИАЛОГ НАСТРОЙКИ API-КЛЮЧА
    if (showApiKeyDialog) {
        var keyInput by remember { mutableStateOf(GeminiNutritionService.getApiKey(context)) }
        Dialog(onDismissRequest = { showApiKeyDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(BiocodePalette.DarkMoss)
                    .border(1.5.dp, BiocodePalette.BioLime, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LucideKey(modifier = Modifier.size(18.dp), tint = BiocodePalette.BioLime)
                            Text(
                                text = "GEMINI API КЛЮЧ",
                                style = BiocodeTypography.TabLabel,
                                color = BiocodePalette.NoguchiCream,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BiocodePalette.SpruceDeck)
                                .clickable { showApiKeyDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            LucideX(modifier = Modifier.size(14.dp), tint = BiocodePalette.NoguchiCream)
                        }
                    }

                    Text(
                        text = "Для работы реальной нейросети Gemini Vision вставьте бесплатный ключ из Google AI Studio (aistudio.google.com). Если ключ не указан — работает локальный биохимический движок.",
                        style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp, lineHeight = 12.sp),
                        color = BiocodePalette.NoguchiCream.copy(alpha = 0.8f)
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Google AI Studio Key", style = BiocodeTypography.TelemetryLabel) },
                        placeholder = { Text("AIzaSy...", color = BiocodePalette.NoguchiCream.copy(alpha = 0.3f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BiocodePalette.BioLime,
                            unfocusedBorderColor = BiocodePalette.DeckBorder,
                            focusedTextColor = BiocodePalette.NoguchiCream,
                            unfocusedTextColor = BiocodePalette.NoguchiCream,
                            focusedLabelColor = BiocodePalette.BioLime,
                            cursorColor = BiocodePalette.BioLime
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(BiocodePalette.BioLime)
                                .clickable {
                                    GeminiNutritionService.saveApiKey(context, keyInput)
                                    showApiKeyDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("СОХРАНИТЬ КЛЮЧ", style = BiocodeTypography.TabLabel.copy(fontSize = 9.sp), color = BiocodePalette.DarkMoss, fontWeight = FontWeight.Bold)
                        }

                        if (keyInput.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(BiocodePalette.LipidAmber.copy(alpha = 0.2f))
                                    .border(1.dp, BiocodePalette.LipidAmber, RoundedCornerShape(20.dp))
                                    .clickable {
                                        GeminiNutritionService.saveApiKey(context, "")
                                        keyInput = ""
                                        showApiKeyDialog = false
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("СБРОСИТЬ", style = BiocodeTypography.TelemetryLabel.copy(fontSize = 8.5.sp), color = BiocodePalette.LipidAmber)
                            }
                        }
                    }
                }
            }
        }
    }
}
