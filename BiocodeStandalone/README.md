# 🧬 BIOCODE Standalone Engine (Модульный движок интерфейса)

Готовая автономная библиотека для сборки интерфейсов в философии **BIOCODE**.
Включает двухслойную архитектуру аппаратного слияния (MetaBall на базе `RenderEffect`), биометрические капсулы с перетяжкой по Золотому сечению, точечно-матричные LED-дисплеи, орбитальные порты и интерактивную кнопку «Жидкий выброс».

**В этой папке НЕТ кода приложения NutriBalance** — только чистый, переносимый UI-движок на Jetpack Compose без лишних зависимостей.

---

## 📁 Структура файлов в папке `src/`

| Файл | Назначение |
|---|---|
| [`BiocodeGrid.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeGrid.kt) | **Глобальная модульная сетка**: дискретная сетка (GridCell, BiocodeGridSpec) и полимино-сборка модульных контейнеров (L-Shape, U-Shape, Rect). |
| [`BiocodeFilletPath.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeFilletPath.kt) | **Алгоритм сопряжений**: точная генерация векторных контуров с выпуклыми (+90°) и **вогнутыми галтелями (concave fillets -90°)** для бенто-карточек с 100% резкостью 1px обводок. |
| [`BiocodeFluidBridge.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeFluidBridge.kt) | **Касательные флюидные мосты**: расчет касательных дуг натяжения между узлами сетки и цепочки соединенных пилюль (`BiocodeLinkedPillRow`). |
| [`BiocodeBentoContainer.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeBentoContainer.kt) | **Модульные бенто-карточки**: контейнеры `BiocodeBentoCard` с угловыми перекрестиями, бейджами телеметрии и флюидный навбар `BiocodeFluidNavBar`. |
| [`NutritionDiaryModel.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/NutritionDiaryModel.kt) | **Домен дневника питания**: расчет КБЖУ, баланс воды, хронология приемов пищи, гликемический статус. |
| [`BiocodeNutritionWidgets.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeNutritionWidgets.kt) | **Бенто-виджеты питания**: модуль энергобаланса с LED-матрицей, L-образный модуль БЖУ, трекер гидратации и строки приемов пищи. |
| [`BiocodeNutritionDashboard.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeNutritionDashboard.kt) | **Главный интерактивный экран**: полноценный экран дневника питания Biocode с живым реактивным состоянием. |
| [`GooeyEffect.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/GooeyEffect.kt) | **Аппаратное ядро слияния**: `Modifier.gooeyBackground()` на базе `RenderEffect.createChainEffect` (Gaussian Blur + ColorMatrix threshold). Сплавляет модули на GPU с сохранением их оригинальных RGB-цветов. |
| [`BiocodeEngine.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeEngine.kt) | **Двухслойный хост**: Слой 1 (силуэты под слияние) + Слой 2 (четкий контент поверх без размытия). |
| [`BiocodeShapes.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeShapes.kt) | **Геометрия Biocode**: `BiocodeWaistPill` (капсулы с перетяжкой 0.618), `BiocodeDockingPort` (орбитальные кольца с вырезом 90°), `BiocodeFluidDropletButton` (кнопка-капля со squish & stretch физикой). |
| [`BiocodeDotMatrix.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeDotMatrix.kt) | **LED-матрицы 5x7**: растровые светодиодные дисплеи `BiocodeDotMatrixText` для цифр и телеметрии. |
| [`BiocodeBlueprint.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeBlueprint.kt) | **Инженерная сетка**: чертежный координатный фон `BiocodeBlueprintCanvas` с перекрестиями и диагоналями. |
| [`BiocodeIcons.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeIcons.kt) | **Векторные пиктограммы**: 4-лучевая звезда `VectorDopamineStar`, биометрический щит `VectorBiocodeShield`, молния `VectorBiocodeBolt`. |
| [`BiocodeTheme.kt`](file:///d:/Repositories/BIOCODE/BiocodeStandalone/src/BiocodeTheme.kt) | **Цветовые токены BIOCODE**: палитра Noguchi Green (#fffeef, #9fd700, #446158, #272c1a), БЖУ-токены, DeepPine, SpruceDeck. |

---

## 🚀 Как подключить в новый проект

1. Скопируйте папку `BiocodeStandalone/src` в свой проект (например, в `app/src/main/java/com/biocode/engine/`).
2. Убедитесь, что в `build.gradle.kts (app)` включен Compose:
   ```kotlin
   buildFeatures {
       compose = true
   }
   ```
3. Все файлы используют стандартные зависимости AndroidX Compose Foundation, Material3 и Graphics. Никаких сторонних библиотек не требуется!

---

## 💻 Примеры использования (Copy & Paste)

### 1. Фоновая чертежная сетка
```kotlin
Box(modifier = Modifier.fillMaxSize().background(BiocodePalette.DeepPineBg)) {
    // Инженерная координатная сетка
    BiocodeBlueprintCanvas(modifier = Modifier.fillMaxSize())
    
    // Ваш контент...
}
```

### 2. Кнопка «Жидкий выброс» с физикой натяжения
```kotlin
// Размещается внутри навигационного дока
BiocodeFluidDropletButton(
    onClick = { /* действие */ },
    dropletColor = BiocodePalette.SpruceDeck,
    accentColor = BiocodePalette.AccentMint
) {
    VectorDopamineStar(modifier = Modifier.size(20.dp), tint = BiocodePalette.AccentMint)
}
```

### 3. Био-капсула с перетяжкой по Золотому сечению (0.618)
```kotlin
BiocodeWaistPill(
    height = 52.dp,
    backgroundColor = BiocodePalette.SpruceDeck,
    borderColor = BiocodePalette.DeckBorder,
    nodeContent = {
        // Круглый узел слева
        VectorBiocodeBolt(modifier = Modifier.size(22.dp), tint = BiocodePalette.AccentMint)
    },
    bodyContent = {
        // Тело капсулы справа
        Text("СЕНСОРНЫЙ МОДУЛЬ", style = BiocodeTypography.TabLabel, color = Color.White)
    }
)
```

### 4. Точечно-матричный LED-дисплей калорий / очков
```kotlin
BiocodeDotMatrixText(
    text = "3260 KCAL",
    dotSize = 2.5.dp,
    activeColor = BiocodePalette.AccentMint,
    inactiveColor = Color(0xFF0C2921)
)
```

### 5. Сплавление нескольких модулей в единый метаболл
```kotlin
BiocodeGooeyHost(
    modifier = Modifier.fillMaxWidth().height(200.dp),
    silhouettes = {
        // Модуль 1 (левый)
        Box(modifier = Modifier.size(100.dp).background(BiocodePalette.SpruceDeck, RoundedCornerShape(24.dp)))
        // Модуль 2 (накладывается или стоит с зазором <16dp)
        Box(modifier = Modifier.offset(x = 80.dp, y = 20.dp).size(80.dp).background(BiocodePalette.SpruceDeck, CircleShape))
        // Они физически сплавятся шейдером в один орган!
    },
    content = {
        // Четкий текст и кнопки поверх
    }
)
### 6. L-образный бенто-контейнер с вогнутой галтелью (Concave Fillet)
```kotlin
// Модуль с вырезом под соседний виджет (как на референсах 3 и 4)
val lShape = BiocodeLShape(
    notchWidthDp = 130.dp,
    notchHeightDp = 110.dp,
    notchCorner = CornerPosition.BOTTOM_RIGHT,
    convexRadiusDp = 24.dp,
    concaveRadiusDp = 20.dp
)

BiocodeBentoCard(
    modifier = Modifier.fillMaxWidth().height(230.dp),
    title = "Биохимический профиль БЖУ",
    badgeText = "НОРМА",
    badgeColor = BiocodePalette.BioLime,
    backgroundColor = BiocodePalette.PineTeal,
    borderColor = BiocodePalette.DeckBorder,
    shape = lShape
) {
    // Контент карточки БЖУ...
}
```

### 7. Цепочка связанных пилюль с органическими перетяжками (Референс 1)
```kotlin
BiocodeLinkedPillRow(
    height = 48.dp,
    segmentWeights = listOf(1.0f, 2.2f, 1.8f),
    backgroundColor = BiocodePalette.DarkMoss,
    borderColor = BiocodePalette.NoguchiBorder,
    slot1 = { Text("#01", color = BiocodePalette.BioLime) },
    slot2 = { Text("Овсянка с изолятом", color = BiocodePalette.NoguchiCream) },
    slot3 = { Text("540 KCAL", color = BiocodePalette.BioLime) }
)
```

### 8. Полный интерактивный дашборд дневника питания
```kotlin
@Composable
fun NutritionScreen() {
    BiocodeNutritionDashboard()
}
```
