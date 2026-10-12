package com.example.kittgrowcontrol.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.kittgrowcontrol.data.*
import com.example.kittgrowcontrol.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Dynamic composable color getters mapping from MaterialTheme to prevent renaming inside UI code
val CyberBg: Color @Composable get() = CyberThemeColors.bg
val CyberCardBg: Color @Composable get() = CyberThemeColors.cardBg
val CyberNeonGreen: Color @Composable get() = CyberThemeColors.primary
val CyberDarkText: Color @Composable get() = CyberThemeColors.onPrimary
val CyberNeonViolet: Color @Composable get() = CyberThemeColors.secondary
val CyberNeonCyan: Color @Composable get() = CyberThemeColors.tertiary
val CyberNeonRed: Color @Composable get() = CyberThemeColors.error
val CyberTextPrimary: Color @Composable get() = CyberThemeColors.textPrimary
val CyberTextSecondary: Color @Composable get() = CyberThemeColors.textSecondary
val CyberBorder: Color @Composable get() = CyberThemeColors.border
val CyberAmber: Color @Composable get() = CyberThemeColors.amber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  isDarkMode: Boolean,
  onToggleDarkMode: () -> Unit,
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current.applicationContext
  val viewModel: MainScreenViewModel = viewModel {
    MainScreenViewModel(DefaultDataRepository(context))
  }
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  when (state) {
    MainScreenUiState.Loading -> {
      Box(modifier = Modifier.fillMaxSize().background(CyberBg), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CyberNeonGreen)
      }
    }
    is MainScreenUiState.Success -> {
      val crops = (state as MainScreenUiState.Success).data
      MainAppContent(
        isDarkMode = isDarkMode,
        onToggleDarkMode = onToggleDarkMode,
        crops = crops,
        onAddCrop = { viewModel.addCrop(it) },
        onUpdateCrop = { viewModel.updateCrop(it) },
        onDeleteCrop = { viewModel.deleteCrop(it) },
        onLogWatering = { id, log -> viewModel.logWatering(id, log) },
        onLogEnvironment = { id, t, h -> viewModel.logEnvironment(id, t, h) },
        onChangeState = { id, st, d, q -> viewModel.changeState(id, st, d, q) },
        onHarvestCrop = { id, d, q, pf -> viewModel.harvestCrop(id, d, q, pf) },
        onUpdateHarvestState = { id, subSt, g, s, n, d -> viewModel.updateHarvestState(id, subSt, g, s, n, d) },
        onExport = { viewModel.exportJson() },
        onImport = { viewModel.importJson(it) }
      )
    }
    is MainScreenUiState.Error -> {
      Box(modifier = Modifier.fillMaxSize().background(CyberBg).padding(16.dp), contentAlignment = Alignment.Center) {
        Text(
          text = "Error cargando banco de memoria K.I.T.T.:\n${(state as MainScreenUiState.Error).throwable.message}",
          color = CyberNeonRed,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
fun MainAppContent(
  isDarkMode: Boolean,
  onToggleDarkMode: () -> Unit,
  crops: List<Crop>,
  onAddCrop: (Crop) -> Unit,
  onUpdateCrop: (Crop) -> Unit,
  onDeleteCrop: (Long) -> Unit,
  onLogWatering: (Long, WateringLog) -> Unit,
  onLogEnvironment: (Long, Double, Double) -> Unit,
  onChangeState: (Long, String, String, Int) -> Unit,
  onHarvestCrop: (Long, String, Int, Double) -> Unit,
  onUpdateHarvestState: (Long, String, Double, Int, String, String) -> Unit,
  onExport: () -> String,
  onImport: (String) -> Boolean
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabTitles = listOf("🌱 Monitoreo", "➕ Ingresar Lote", "🪓 Cosechas", "⚙️ Herramientas")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBg)
      .statusBarsPadding()
      .navigationBarsPadding()
  ) {
    // Header Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(CyberBg)
        .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Spacer(modifier = Modifier.width(48.dp)) // Balance element
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "K.I.T.T. CULTIVATION MONITOR",
          color = CyberNeonGreen,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "SISTEMA OPERATIVO DE GESTIÓN MODULAR",
          color = CyberTextSecondary,
          fontSize = 9.sp,
          fontWeight = FontWeight.Medium,
          letterSpacing = 0.5.sp
        )
      }
      IconButton(onClick = onToggleDarkMode) {
        Text(
          text = if (isDarkMode) "☀️" else "🌙",
          fontSize = 20.sp
        )
      }
    }
    Divider(color = CyberBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

    // Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = CyberBg,
      contentColor = CyberNeonGreen,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = CyberNeonGreen
        )
      },
      divider = { Divider(color = CyberBorder) }
    ) {
      tabTitles.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              fontSize = 12.sp,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedTab == index) CyberNeonGreen else CyberTextSecondary
            )
          }
        )
      }
    }

    // Tab content
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
    ) {
      when (selectedTab) {
        0 -> MonitoreoTab(
          crops = crops.filter { it.estadoActual != "Cosechado" },
          onUpdateCrop = onUpdateCrop,
          onDeleteCrop = onDeleteCrop,
          onLogWatering = onLogWatering,
          onLogEnvironment = onLogEnvironment,
          onChangeState = onChangeState,
          onHarvestCrop = onHarvestCrop
        )
        1 -> IngresarLoteTab(
          onAddCrop = onAddCrop,
          onSuccess = { selectedTab = 0 }
        )
        2 -> CosechasTab(
          crops = crops.filter { it.estadoActual == "Cosechado" },
          onUpdateHarvestState = onUpdateHarvestState,
          onDeleteCrop = onDeleteCrop
        )
        3 -> HerramientasTab(
          onExport = onExport,
          onImport = onImport
        )
      }
    }
  }
}

// ---------------------------------------------------------
// TAB 1: MONITOREO
// ---------------------------------------------------------
@Composable
fun MonitoreoTab(
  crops: List<Crop>,
  onUpdateCrop: (Crop) -> Unit,
  onDeleteCrop: (Long) -> Unit,
  onLogWatering: (Long, WateringLog) -> Unit,
  onLogEnvironment: (Long, Double, Double) -> Unit,
  onChangeState: (Long, String, String, Int) -> Unit,
  onHarvestCrop: (Long, String, Int, Double) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedStageFilter by remember { mutableStateOf("Todos") }
  var sortBy by remember { mutableStateOf("Ingreso") } // Ingreso, Nombre, Plantas

  val filteredCrops = crops.filter { crop ->
    val matchesSearch = crop.nombre.contains(searchQuery, ignoreCase = true)
    val matchesStage = selectedStageFilter == "Todos" || crop.estadoActual == selectedStageFilter
    matchesSearch && matchesStage
  }.sortedWith { a, b ->
    when (sortBy) {
      "Nombre" -> a.nombre.compareTo(b.nombre)
      "Plantas" -> b.cantidad.compareTo(a.cantidad)
      else -> b.id.compareTo(a.id) // Default: newest first
    }
  }

  // Dashboard Metrics
  val activeCropsCount = crops.size
  val totalPlants = crops.sumOf { it.cantidad }
  val inFlowerPlants = crops.filter { it.estadoActual == "Floración" }.sumOf { it.cantidad }
  
  // Calculate next projected harvest
  val nextHarvest = crops.mapNotNull { crop ->
    val proj = calculateProjections(crop)
    if (proj.harvestDate != "N/A") {
      try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateObj = sdf.parse(proj.harvestDate)
        if (dateObj != null && dateObj.time >= Date().time) dateObj else null
      } catch (e: Exception) {
        null
      }
    } else null
  }.minOrNull()
  
  val nextHarvestStr = if (nextHarvest != null) {
    SimpleDateFormat("dd/MM", Locale.getDefault()).format(nextHarvest)
  } else "N/A"

  Column(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
    Spacer(modifier = Modifier.height(10.dp))
    
    // KPI Cards Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      KpiCard("Lotes", activeCropsCount.toString(), Modifier.weight(1f))
      KpiCard("Plantas", totalPlants.toString(), Modifier.weight(1f))
      KpiCard("En Flora", inFlowerPlants.toString(), Modifier.weight(1f))
      KpiCard("Próx Cos.", nextHarvestStr, Modifier.weight(1.1f), isSpecial = nextHarvestStr != "N/A")
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Search and filters
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = { Text("Buscar variedad...", color = CyberTextSecondary) },
      modifier = Modifier.fillMaxWidth(),
      textStyle = TextStyle(color = CyberTextPrimary, fontSize = 14.sp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyberNeonGreen,
        unfocusedBorderColor = CyberBorder,
        focusedTextColor = CyberTextPrimary,
        unfocusedTextColor = CyberTextPrimary
      ),
      shape = RoundedCornerShape(4.dp),
      singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Filter stage + Sort buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Stage Filter buttons
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("Todos", "Clonador", "Veg", "Flora").forEach { label ->
          val stageVal = when (label) {
            "Veg" -> "Vegetativo"
            "Flora" -> "Floración"
            else -> label
          }
          val isSelected = selectedStageFilter == stageVal
          Box(
            modifier = Modifier
              .background(if (isSelected) CyberNeonGreen else Color.Transparent, RoundedCornerShape(4.dp))
              .border(1.dp, if (isSelected) CyberNeonGreen else CyberBorder, RoundedCornerShape(4.dp))
              .clickable { selectedStageFilter = stageVal }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) CyberDarkText else CyberTextSecondary
            )
          }
        }
      }

      // Sort Indicator Button
      Box(
        modifier = Modifier
          .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
          .clickable {
            sortBy = when (sortBy) {
              "Ingreso" -> "Nombre"
              "Nombre" -> "Plantas"
              else -> "Ingreso"
            }
          }
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = "Ord: $sortBy",
          fontSize = 11.sp,
          color = CyberNeonGreen,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Active crop list
    if (filteredCrops.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("No hay lotes en los bancos de memoria.", color = CyberTextSecondary, fontSize = 14.sp)
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
      ) {
        items(filteredCrops, key = { it.id }) { crop ->
          CropCard(
            crop = crop,
            onUpdateCrop = onUpdateCrop,
            onDeleteCrop = onDeleteCrop,
            onLogWatering = onLogWatering,
            onLogEnvironment = onLogEnvironment,
            onChangeState = onChangeState,
            onHarvestCrop = onHarvestCrop
          )
        }
      }
    }
  }
}

@Composable
fun KpiCard(label: String, value: String, modifier: Modifier = Modifier, isSpecial: Boolean = false) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(4.dp),
    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
    border = BorderStroke(1.dp, if (isSpecial) CyberAmber else CyberBorder)
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label.uppercase(),
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = CyberTextSecondary,
        letterSpacing = 0.5.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        fontSize = 15.sp,
        fontWeight = FontWeight.Black,
        color = if (isSpecial) CyberAmber else CyberNeonGreen
      )
    }
  }
}

@Composable
fun CropCard(
  crop: Crop,
  onUpdateCrop: (Crop) -> Unit,
  onDeleteCrop: (Long) -> Unit,
  onLogWatering: (Long, WateringLog) -> Unit,
  onLogEnvironment: (Long, Double, Double) -> Unit,
  onChangeState: (Long, String, String, Int) -> Unit,
  onHarvestCrop: (Long, String, Int, Double) -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }
  val projections = calculateProjections(crop)
  val stageColor = when (crop.estadoActual) {
    "Clonador" -> CyberNeonCyan
    "Vegetativo" -> CyberNeonGreen
    "Floración" -> CyberNeonViolet
    else -> CyberTextSecondary
  }

  val context = LocalContext.current

  // Dialog states
  var showWateringDialog by remember { mutableStateOf(false) }
  var showEnvDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var showEditDialog by remember { mutableStateOf(false) }
  var showSplitDialog by remember { mutableStateOf(false) }
  var targetStateForSplit by remember { mutableStateOf("") } // "Vegetativo", "Floración", "Cosechado"

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { isExpanded = !isExpanded },
    shape = RoundedCornerShape(4.dp),
    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
    border = BorderStroke(1.dp, if (isExpanded) CyberNeonGreen else CyberBorder)
  ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      // Left stage neon accent bar
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(IntrinsicSize.Min)
          .fillMaxHeight()
          .background(stageColor)
      )

      Column(modifier = Modifier.padding(12.dp).weight(1f)) {
        // Card Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${crop.nombre} (${crop.cantidad} uds)",
            color = CyberTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
          )
          // Stage Badge
          Box(
            modifier = Modifier
              .background(stageColor.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
              .border(0.5.dp, stageColor, RoundedCornerShape(2.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = crop.estadoActual.uppercase(),
              color = stageColor,
              fontSize = 9.sp,
              fontWeight = FontWeight.Black
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Details Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "Tipo: ${crop.tipo}", fontSize = 11.sp, color = CyberTextSecondary)
          Text(text = "Genética: ${crop.tipoGen}", fontSize = 11.sp, color = CyberTextSecondary)
          Text(text = "Origen: ${crop.origen}", fontSize = 11.sp, color = CyberTextSecondary)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "Ingreso: ${formatDateStr(crop.fechaIngreso)}", fontSize = 11.sp, color = CyberTextSecondary)

        Spacer(modifier = Modifier.height(10.dp))

        // Projected Timeline Info
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(CyberBg, RoundedCornerShape(2.dp))
            .border(0.5.dp, CyberBorder, RoundedCornerShape(2.dp))
            .padding(8.dp)
        ) {
          Column {
            Text(
              text = "CRONOGRAMA TEÓRICO PROYECTADO",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = CyberTextSecondary,
              letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Column {
                Text("🌱 Vege", fontSize = 9.sp, color = CyberTextSecondary)
                Text(projections.vegeDate, fontSize = 10.sp, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
              }
              Column {
                Text("🌿 Flora", fontSize = 9.sp, color = CyberTextSecondary)
                Text(projections.floraDate, fontSize = 10.sp, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
              }
              Column {
                Text("🪓 Cosecha Est.", fontSize = 9.sp, color = CyberTextSecondary)
                Text(projections.harvestDate, fontSize = 10.sp, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = projections.progress,
              modifier = Modifier.fillMaxWidth().height(4.dp),
              color = stageColor,
              trackColor = CyberBorder
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = projections.progressText,
              fontSize = 9.sp,
              color = stageColor,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Expandable Content
        AnimatedVisibility(visible = isExpanded) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = CyberBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // General Info Header / Edit Batch Button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("DETALLES OPERATIVOS DEL LOTE", fontSize = 9.sp, color = CyberTextSecondary, fontWeight = FontWeight.Bold)
              Box(
                modifier = Modifier
                  .border(0.5.dp, CyberNeonGreen, RoundedCornerShape(2.dp))
                  .clickable { showEditDialog = true }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text("EDITAR LOTE", fontSize = 9.sp, color = CyberNeonGreen, fontWeight = FontWeight.Bold)
              }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Observations
            if (crop.observaciones.isNotBlank()) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(CyberBg)
                  .border(BorderStroke(0.5.dp, CyberBorder), RoundedCornerShape(2.dp))
                  .padding(8.dp)
              ) {
                Text("OBSERVACIONES DE LA UNIDAD:", fontSize = 9.sp, color = CyberTextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(crop.observaciones, fontSize = 11.sp, color = CyberTextPrimary)
              }
              Spacer(modifier = Modifier.height(10.dp))
            }

            // VPD & Environment Section
            val lastMed = crop.mediciones.lastOrNull()
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(CyberBg)
                .border(BorderStroke(0.5.dp, CyberBorder), RoundedCornerShape(2.dp))
                .padding(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("MONITOREO AMBIENTAL", fontSize = 9.sp, color = CyberTextSecondary, fontWeight = FontWeight.Bold)
                Box(
                  modifier = Modifier
                    .border(0.5.dp, CyberNeonGreen, RoundedCornerShape(2.dp))
                    .clickable { showEnvDialog = true }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text("REGISTRAR AMBIENTE", fontSize = 9.sp, color = CyberNeonGreen, fontWeight = FontWeight.Bold)
                }
              }
              Spacer(modifier = Modifier.height(6.dp))

              if (lastMed != null) {
                val vpdLevel = when {
                  lastMed.vpd < 0.5 -> "VPD Bajo (Riesgo Hongos)"
                  lastMed.vpd <= 1.2 && crop.estadoActual == "Vegetativo" -> "VPD Ideal (Vegetativo)"
                  lastMed.vpd >= 0.8 && lastMed.vpd <= 1.5 && crop.estadoActual == "Floración" -> "VPD Ideal (Flora)"
                  lastMed.vpd > 1.6 -> "VPD Alto (Estrés Hídrico)"
                  else -> "VPD Moderado"
                }
                val vpdColor = when {
                  vpdLevel.contains("Ideal") -> CyberNeonGreen
                  vpdLevel.contains("Alto") -> CyberNeonRed
                  else -> CyberAmber
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Último: ${lastMed.temp}°C / ${lastMed.humedad}% H.R.", fontSize = 11.sp, color = CyberTextPrimary)
                  Text("VPD: ${lastMed.vpd} kPa", fontSize = 11.sp, color = vpdColor, fontWeight = FontWeight.Bold)
                }
                Text(text = "Estado: $vpdLevel", fontSize = 10.sp, color = vpdColor)
              } else {
                Text("No hay mediciones ambientales registradas para este lote.", fontSize = 10.sp, color = CyberTextSecondary)
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Riegos / Nutrientes Section
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(CyberBg)
                .border(BorderStroke(0.5.dp, CyberBorder), RoundedCornerShape(2.dp))
                .padding(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("HISTORIAL DE RIEGOS (Últimos 3)", fontSize = 9.sp, color = CyberTextSecondary, fontWeight = FontWeight.Bold)
                Box(
                  modifier = Modifier
                    .border(0.5.dp, CyberNeonGreen, RoundedCornerShape(2.dp))
                    .clickable { showWateringDialog = true }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text("REGISTRAR RIEGO", fontSize = 9.sp, color = CyberNeonGreen, fontWeight = FontWeight.Bold)
                }
              }
              Spacer(modifier = Modifier.height(6.dp))

              val riegos = crop.riegos.takeLast(3).reversed()
              if (riegos.isEmpty()) {
                Text("No hay riegos registrados para este lote.", fontSize = 10.sp, color = CyberTextSecondary)
              } else {
                riegos.forEach { riego ->
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(bottom = 6.dp)
                      .border(0.5.dp, CyberBorder, RoundedCornerShape(2.dp))
                      .padding(6.dp)
                  ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                      Text("💧 ${riego.volumen}L | pH: ${riego.ph} | EC: ${riego.ec}", fontSize = 11.sp, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
                      Text(formatDateStr(riego.fecha), fontSize = 10.sp, color = CyberTextSecondary)
                    }
                    if (riego.fertilizantes.isNotBlank()) {
                      Text("Nutrientes: ${riego.fertilizantes}", fontSize = 10.sp, color = CyberTextSecondary)
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // History Section
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .background(CyberBg)
                .border(BorderStroke(0.5.dp, CyberBorder), RoundedCornerShape(2.dp))
                .padding(8.dp)
            ) {
              Text("HISTORIAL DE CICLOS REALES", fontSize = 9.sp, color = CyberTextSecondary, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(4.dp))
              crop.historial.forEach { event ->
                Text("▶ ${event.estado} - ${formatDateStr(event.fecha)}", fontSize = 10.sp, color = CyberTextPrimary)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions panel
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Move to Veg button
              Button(
                onClick = {
                  targetStateForSplit = "Vegetativo"
                  showSplitDialog = true
                },
                enabled = crop.estadoActual == "Clonador",
                colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, disabledContainerColor = CyberBorder),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.weight(1f).height(32.dp)
              ) {
                Text("A VEG", fontSize = 10.sp, color = if (crop.estadoActual == "Clonador") CyberDarkText else CyberTextSecondary)
              }

              // Move to Flora button
              Button(
                onClick = {
                  targetStateForSplit = "Floración"
                  showSplitDialog = true
                },
                enabled = crop.estadoActual == "Vegetativo",
                colors = ButtonDefaults.buttonColors(containerColor = CyberNeonViolet, disabledContainerColor = CyberBorder),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.weight(1f).height(32.dp)
              ) {
                Text("A FLORA", fontSize = 10.sp, color = if (crop.estadoActual == "Vegetativo") CyberDarkText else CyberTextSecondary)
              }

              // Harvest button
              Button(
                onClick = {
                  targetStateForSplit = "Cosechado"
                  showSplitDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.weight(1.1f).height(32.dp)
              ) {
                Text("COSECHAR", fontSize = 10.sp, color = CyberDarkText)
              }

              // Purgar button
              OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberNeonRed),
                border = BorderStroke(1.dp, CyberNeonRed),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.weight(0.9f).height(32.dp)
              ) {
                Text("PURGAR", fontSize = 10.sp, color = CyberNeonRed)
              }
            }
          }
        }
      }
    }
  }

  // --- DIALOGS ---

  // Log Watering Dialog
  if (showWateringDialog) {
    var volume by remember { mutableStateOf("") }
    var ph by remember { mutableStateOf("") }
    var ec by remember { mutableStateOf("") }
    var nutrients by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showWateringDialog = false },
      containerColor = CyberCardBg,
      title = { Text("REGISTRAR RIEGO EN LOTE", color = CyberNeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = volume,
            onValueChange = { volume = it },
            label = { Text("Volumen de agua (L)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
          OutlinedTextField(
            value = ph,
            onValueChange = { ph = it },
            label = { Text("pH") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
          OutlinedTextField(
            value = ec,
            onValueChange = { ec = it },
            label = { Text("EC (mS/cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
          OutlinedTextField(
            value = nutrients,
            onValueChange = { nutrients = it },
            label = { Text("Nutrientes / Fertilizantes") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas adicionales") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val volD = volume.toDoubleOrNull() ?: 1.0
            val phD = ph.toDoubleOrNull() ?: 6.0
            val ecD = ec.toDoubleOrNull() ?: 0.0
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val log = WateringLog(dateStr, volD, phD, ecD, nutrients, notes)
            onLogWatering(crop.id, log)
            showWateringDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, contentColor = CyberDarkText)
        ) {
          Text("REGISTRAR")
        }
      },
      dismissButton = {
        TextButton(onClick = { showWateringDialog = false }) {
          Text("CANCELAR", color = CyberTextSecondary)
        }
      }
    )
  }

  // Log Env Readings Dialog
  if (showEnvDialog) {
    var temp by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showEnvDialog = false },
      containerColor = CyberCardBg,
      title = { Text("REGISTRAR PARÁMETROS AMBIENTALES", color = CyberNeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = temp,
            onValueChange = { temp = it },
            label = { Text("Temperatura (°C)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
          OutlinedTextField(
            value = humidity,
            onValueChange = { humidity = it },
            label = { Text("Humedad Relativa (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val t = temp.toDoubleOrNull() ?: 24.0
            val h = humidity.toDoubleOrNull() ?: 60.0
            onLogEnvironment(crop.id, t, h)
            showEnvDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, contentColor = CyberDarkText)
        ) {
          Text("REGISTRAR")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEnvDialog = false }) {
          Text("CANCELAR", color = CyberTextSecondary)
        }
      }
    )
  }

  // Split / Partial State Transition Dialog
  if (showSplitDialog) {
    var qtyInput by remember { mutableStateOf(crop.cantidad.toString()) }
    val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    var dateInput by remember { mutableStateOf(todayStr) }
    var pesoFrescoInput by remember { mutableStateOf("") }

    val titleText = if (targetStateForSplit == "Cosechado") "COSECHAR PARTE DEL LOTE" else "TRASLADO PARCIAL DE ETAPA"
    val actionText = if (targetStateForSplit == "Cosechado") "COSECHAR" else "TRASLADAR"
    val accentColor = when (targetStateForSplit) {
      "Vegetativo" -> CyberNeonGreen
      "Floración" -> CyberNeonViolet
      else -> CyberAmber
    }

    AlertDialog(
      onDismissRequest = { showSplitDialog = false },
      containerColor = CyberCardBg,
      title = { Text(titleText, color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Lote: ${crop.nombre}\nCantidad actual: ${crop.cantidad} unidades", fontSize = 12.sp, color = CyberTextSecondary)
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = qtyInput,
            onValueChange = { qtyInput = it },
            label = { Text("Cantidad a mover / cosechar") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = CyberBorder)
          )
          OutlinedTextField(
            value = dateInput,
            onValueChange = { dateInput = it },
            label = { Text("Fecha del traslado (DD/MM/AAAA)") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = CyberBorder)
          )
          if (targetStateForSplit == "Cosechado") {
            OutlinedTextField(
              value = pesoFrescoInput,
              onValueChange = { pesoFrescoInput = it },
              label = { Text("Peso fresco obtenido (g húmedos)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, unfocusedBorderColor = CyberBorder)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val q = qtyInput.toIntOrNull()
            if (q == null || q <= 0 || q > crop.cantidad) {
              Toast.makeText(context, "Cantidad no válida", Toast.LENGTH_SHORT).show()
              return@Button
            }
            
            val dateParts = dateInput.split("/")
            val parsedDate = if (dateParts.size == 3) {
              "${dateParts[2].trim()}-${dateParts[1].trim()}-${dateParts[0].trim()}"
            } else {
              SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }

            if (targetStateForSplit == "Cosechado") {
              val pf = pesoFrescoInput.toDoubleOrNull() ?: 0.0
              onHarvestCrop(crop.id, parsedDate, q, pf)
            } else {
              onChangeState(crop.id, targetStateForSplit, parsedDate, q)
            }
            showSplitDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = CyberDarkText)
        ) {
          Text(actionText)
        }
      },
      dismissButton = {
        TextButton(onClick = { showSplitDialog = false }) {
          Text("CANCELAR", color = CyberTextSecondary)
        }
      }
    )
  }

  // Edit Crop Dialog
  if (showEditDialog) {
    var nameInput by remember { mutableStateOf(crop.nombre) }
    var qtyInput by remember { mutableStateOf(crop.cantidad.toString()) }
    val entryDateFormatted = try {
      val dbSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      val uiSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
      uiSdf.format(dbSdf.parse(crop.fechaIngreso)!!)
    } catch (e: Exception) {
      SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
    var dateInput by remember { mutableStateOf(entryDateFormatted) }
    
    var tipo by remember { mutableStateOf(crop.tipo) }
    val tipoOptions = listOf("Automática", "Fotoperiódica", "Fotoperiódica Fast")
    
    var tipoGen by remember { mutableStateOf(crop.tipoGen) }
    val genOptions = listOf("Feminizada", "Regular")
    
    var origen by remember { mutableStateOf(crop.origen) }
    val origenOptions = listOf("Semilla", "Esqueje")
    
    var tVegetativo by remember { mutableStateOf(crop.tVegetativo?.toString() ?: "") }
    var tFloracion by remember { mutableStateOf(crop.tFloracion?.toString() ?: "") }
    var observaciones by remember { mutableStateOf(crop.observaciones) }

    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      containerColor = CyberCardBg,
      title = { Text("EDITAR REGISTRO DE LOTE", color = CyberNeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Nombre / Identificador") },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
          
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = qtyInput,
              onValueChange = { qtyInput = it },
              label = { Text("Cantidad") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = dateInput,
              onValueChange = { dateInput = it },
              label = { Text("Fecha Ingreso") },
              placeholder = { Text("DD/MM/AAAA") },
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
              modifier = Modifier.weight(1.2f)
            )
          }

          SelectorField(label = "Tipo de Variedad", options = tipoOptions, selected = tipo, onSelect = { tipo = it })
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectorField(label = "Genética", options = genOptions, selected = tipoGen, onSelect = { tipoGen = it }, modifier = Modifier.weight(1f))
            SelectorField(label = "Origen", options = origenOptions, selected = origen, onSelect = { origen = it }, modifier = Modifier.weight(1f))
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = tVegetativo,
              onValueChange = { tVegetativo = it },
              label = { Text("Veg (Semanas)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = tFloracion,
              onValueChange = { tFloracion = it },
              label = { Text("Flora (Semanas)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
              modifier = Modifier.weight(1f)
            )
          }

          OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones / Notas") },
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (nameInput.isBlank()) {
              Toast.makeText(context, "Ingrese un nombre", Toast.LENGTH_SHORT).show()
              return@Button
            }
            val q = qtyInput.toIntOrNull() ?: crop.cantidad
            
            val dateParts = dateInput.split("/")
            val parsedDate = if (dateParts.size == 3) {
              "${dateParts[2].trim()}-${dateParts[1].trim()}-${dateParts[0].trim()}"
            } else {
              crop.fechaIngreso
            }
            
            val veg = tVegetativo.toIntOrNull()
            val flo = tFloracion.toIntOrNull()
            
            val updated = crop.copy(
              nombre = nameInput,
              cantidad = q,
              fechaIngreso = parsedDate,
              tipo = tipo,
              tipoGen = tipoGen,
              origen = origen,
              tVegetativo = veg,
              tFloracion = flo,
              observaciones = observaciones
            )
            onUpdateCrop(updated)
            showEditDialog = false
            Toast.makeText(context, "Lote actualizado en memoria central", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, contentColor = CyberDarkText)
        ) {
          Text("GUARDAR")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) {
          Text("CANCELAR", color = CyberTextSecondary)
        }
      }
    )
  }

  // Delete/Purge Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      containerColor = CyberCardBg,
      title = { Text("PURGAR LOTE DE MEMORIA", color = CyberNeonRed, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
      text = { Text("¿Está seguro de que desea purgar este lote? Esta acción borrará permanentemente sus registros de la unidad central.", color = CyberTextPrimary) },
      confirmButton = {
        Button(
          onClick = {
            onDeleteCrop(crop.id)
            showDeleteDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberNeonRed, contentColor = Color.White)
        ) {
          Text("ELIMINAR")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("CANCELAR", color = CyberTextSecondary)
        }
      }
    )
  }
}

// ---------------------------------------------------------
// TAB 2: INGRESAR LOTE
// ---------------------------------------------------------
@Composable
fun IngresarLoteTab(
  onAddCrop: (Crop) -> Unit,
  onSuccess: () -> Unit
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  var nombre by remember { mutableStateOf("") }
  var cantidad by remember { mutableStateOf("1") }
  
  val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
  var fechaIngreso by remember { mutableStateOf(today) }

  var tipo by remember { mutableStateOf("Automática") }
  val tipoOptions = listOf("Automática", "Fotoperiódica", "Fotoperiódica Fast")

  var tipoGen by remember { mutableStateOf("Feminizada") }
  val genOptions = listOf("Feminizada", "Regular")

  var origen by remember { mutableStateOf("Semilla") }
  val origenOptions = listOf("Semilla", "Esqueje")

  var estadoActual by remember { mutableStateOf("Vegetativo") }
  val estadoOptions = listOf("Clonador", "Vegetativo", "Floración")

  var tVegetativo by remember { mutableStateOf("4") }
  var tFloracion by remember { mutableStateOf("8") }
  var observaciones by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = "INGRESAR NUEVO LOTE AL SISTEMA",
      color = CyberNeonGreen,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold
    )

    OutlinedTextField(
      value = nombre,
      onValueChange = { nombre = it },
      label = { Text("Nombre de la Variedad / Identificador") },
      placeholder = { Text("Ej: Amnesia Kush 20/4") },
      colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
      modifier = Modifier.fillMaxWidth()
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = cantidad,
        onValueChange = { cantidad = it },
        label = { Text("Cantidad Uds") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = fechaIngreso,
        onValueChange = { fechaIngreso = it },
        label = { Text("Fecha Ingreso") },
        placeholder = { Text("DD/MM/AAAA") },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
        modifier = Modifier.weight(1.2f)
      )
    }

    // Custom dropdown selectors for Type, Genetics, Origin
    SelectorField(label = "Tipo de Variedad", options = tipoOptions, selected = tipo, onSelect = { tipo = it })
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      SelectorField(label = "Genética", options = genOptions, selected = tipoGen, onSelect = { tipoGen = it }, modifier = Modifier.weight(1f))
      SelectorField(label = "Origen", options = origenOptions, selected = origen, onSelect = { origen = it }, modifier = Modifier.weight(1f))
    }

    SelectorField(label = "Estado Inicial", options = estadoOptions, selected = estadoActual, onSelect = { estadoActual = it })

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = tVegetativo,
        onValueChange = { tVegetativo = it },
        label = { Text("Est. Veg (Semanas)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
        modifier = Modifier.weight(1f)
      )
      OutlinedTextField(
        value = tFloracion,
        onValueChange = { tFloracion = it },
        label = { Text("Est. Flora (Semanas)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
        modifier = Modifier.weight(1f)
      )
    }

    OutlinedTextField(
      value = observaciones,
      onValueChange = { observaciones = it },
      label = { Text("Observaciones / Notas Iniciales") },
      minLines = 3,
      colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
      onClick = {
        if (nombre.isBlank()) {
          Toast.makeText(context, "Por favor ingrese un nombre", Toast.LENGTH_SHORT).show()
          return@Button
        }
        val cantInt = cantidad.toIntOrNull() ?: 1
        val vegInt = tVegetativo.toIntOrNull()
        val floInt = tFloracion.toIntOrNull()

        val dateParts = fechaIngreso.split("/")
        val storedDate = if (dateParts.size == 3) {
          "${dateParts[2].trim()}-${dateParts[1].trim()}-${dateParts[0].trim()}"
        } else {
          SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        val newCrop = Crop(
          id = System.currentTimeMillis(),
          nombre = nombre,
          cantidad = cantInt,
          tipo = tipo,
          tipoGen = tipoGen,
          origen = origen,
          estadoActual = estadoActual,
          fechaIngreso = storedDate,
          tVegetativo = vegInt,
          tFloracion = floInt,
          observaciones = observaciones,
          historial = listOf(CycleEvent(estadoActual, storedDate)),
          riegos = emptyList(),
          mediciones = emptyList()
        )
        onAddCrop(newCrop)
        Toast.makeText(context, "Lote registrado en Memoria Central", Toast.LENGTH_SHORT).show()
        onSuccess()
      },
      colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, contentColor = CyberDarkText),
      shape = RoundedCornerShape(4.dp),
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Text("REGISTRAR EN MEMORIA CENTRAL", fontWeight = FontWeight.Bold)
    }
    
    Spacer(modifier = Modifier.height(20.dp))
  }
}

@Composable
fun SelectorField(
  label: String,
  options: List<String>,
  selected: String,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Text(text = label, fontSize = 10.sp, color = CyberTextSecondary, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
        .padding(2.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      options.forEach { opt ->
        val isSel = selected == opt
        Box(
          modifier = Modifier
            .weight(1f)
            .background(if (isSel) CyberNeonGreen else Color.Transparent, RoundedCornerShape(2.dp))
            .clickable { onSelect(opt) }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = opt,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSel) CyberDarkText else CyberTextPrimary
          )
        }
      }
    }
  }
}

// ---------------------------------------------------------
// TAB 3: COSECHAS HISTÓRICAS
// ---------------------------------------------------------
@Composable
fun CosechasTab(
  crops: List<Crop>,
  onUpdateHarvestState: (Long, String, Double, Int, String, String) -> Unit,
  onDeleteCrop: (Long) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp)
  ) {
    Text(
      text = "ARCHIVO HISTÓRICO DE COSECHAS",
      color = CyberNeonGreen,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(10.dp))

    if (crops.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("No hay registros de cosechas en memoria.", color = CyberTextSecondary, fontSize = 14.sp)
      }
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
      ) {
        items(crops, key = { it.id }) { crop ->
          val harvest = crop.cosecha
          if (harvest != null) {
            var showCuradoDialog by remember { mutableStateOf(false) }
            var showDeleteHarvestDialog by remember { mutableStateOf(false) }

            val borderAccentColor = when (harvest.subEstado) {
              "Secado" -> CyberNeonCyan
              "Curado" -> CyberAmber
              else -> CyberNeonGreen
            }

            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(4.dp),
              colors = CardDefaults.cardColors(containerColor = CyberCardBg),
              border = BorderStroke(1.dp, borderAccentColor)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                // Row 1: Title & Badge
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${crop.nombre} (${crop.cantidad} uds)",
                    color = CyberTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                  )
                  // Sub-State Badge
                  val (badgeText, badgeColor) = when (harvest.subEstado) {
                    "Secado" -> "SECADO (15D)" to CyberNeonCyan
                    "Curado" -> "CURADO (60D)" to CyberAmber
                    else -> "LISTO / CURADO" to CyberNeonGreen
                  }
                  Box(
                    modifier = Modifier
                      .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                      .border(0.5.dp, badgeColor, RoundedCornerShape(2.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = badgeText,
                      color = badgeColor,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Black
                    )
                  }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Cosechado el: ${formatDateStr(harvest.fecha)} | Origen: ${crop.origen}",
                  fontSize = 11.sp,
                  color = CyberTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Timer / Progress bar based on state
                when (harvest.subEstado) {
                  "Secado" -> {
                    val elapsedDays = try {
                      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                      val harvestDate = sdf.parse(harvest.fecha)
                      val diff = Date().time - harvestDate.time
                      (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                    } catch (e: Exception) { 0 }
                    val remainingDays = (15 - elapsedDays).coerceAtLeast(0)
                    val progress = (elapsedDays.toFloat() / 15f).coerceIn(0.0f, 1.0f)
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                      if (harvest.pesoFresco > 0.0) {
                        Text("Peso fresco cosechado: ${harvest.pesoFresco}g", fontSize = 11.sp, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                      }
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progreso Secado Ideal:", fontSize = 10.sp, color = CyberTextSecondary)
                        Text("Día $elapsedDays de 15 ($remainingDays d restantes)", fontSize = 10.sp, color = CyberNeonCyan, fontWeight = FontWeight.Bold)
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = CyberNeonCyan,
                        trackColor = CyberBorder
                      )
                      Spacer(modifier = Modifier.height(12.dp))
                      
                      // Button to transition to Curing
                      Button(
                        onClick = { showCuradoDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNeonCyan, contentColor = CyberDarkText),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                      ) {
                        Text("PASAR A CURADO (REGISTRAR PESO SECO)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                      }
                    }
                  }
                  "Curado" -> {
                    val startCuradoStr = harvest.fechaCurado ?: harvest.fecha
                    val elapsedDays = try {
                      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                      val curadoDate = sdf.parse(startCuradoStr)
                      val diff = Date().time - curadoDate.time
                      (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                    } catch (e: Exception) { 0 }
                    val remainingDays = (60 - elapsedDays).coerceAtLeast(0)
                    val progress = (elapsedDays.toFloat() / 60f).coerceIn(0.0f, 1.0f)

                    Column(modifier = Modifier.fillMaxWidth()) {
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progreso Curado Ideal:", fontSize = 10.sp, color = CyberTextSecondary)
                        Text("Día $elapsedDays de 60 ($remainingDays d restantes)", fontSize = 10.sp, color = CyberAmber, fontWeight = FontWeight.Bold)
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = CyberAmber,
                        trackColor = CyberBorder
                      )
                      Spacer(modifier = Modifier.height(8.dp))
                      
                      // Show yield and quality recorded
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        val frescoText = if (harvest.pesoFresco > 0.0) " (Fresco: ${harvest.pesoFresco}g)" else ""
                        Text("Rendimiento Seco: ${harvest.rendimiento}g$frescoText", fontSize = 11.sp, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
                        // Stars
                        Row {
                          (1..5).forEach { i ->
                            Text(
                              text = if (i <= harvest.calidad) "★" else "☆",
                              color = CyberAmber,
                              fontSize = 12.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }
                      }
                      if (harvest.pesoFresco > 0.0 && harvest.rendimiento > 0.0) {
                        val lossPct = ((harvest.pesoFresco - harvest.rendimiento) / harvest.pesoFresco * 100.0).coerceIn(0.0, 100.0)
                        val yieldPct = (harvest.rendimiento / harvest.pesoFresco * 100.0).coerceIn(0.0, 100.0)
                        val lossFormatted = String.format(Locale.US, "%.1f", lossPct)
                        val yieldFormatted = String.format(Locale.US, "%.1f", yieldPct)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "Humedad perdida: $lossFormatted% | Relación Seco/Fresco: $yieldFormatted%",
                          fontSize = 10.sp,
                          color = CyberNeonGreen,
                          fontWeight = FontWeight.Bold
                        )
                      }
                      if (harvest.notas.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Cata: ${harvest.notas}", fontSize = 10.sp, color = CyberTextSecondary)
                      }
                      
                      Spacer(modifier = Modifier.height(10.dp))
                      
                      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Button to modify/re-enter curation details
                        OutlinedButton(
                          onClick = { showCuradoDialog = true },
                          border = BorderStroke(0.5.dp, CyberBorder),
                          shape = RoundedCornerShape(2.dp),
                          modifier = Modifier.weight(1f).height(32.dp)
                        ) {
                          Text("CORREGIR PESO", fontSize = 9.sp, color = CyberTextSecondary)
                        }
                        
                        // Button to finish Curing
                        Button(
                          onClick = {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            onUpdateHarvestState(crop.id, "Listo", 0.0, 0, "", today)
                          },
                          colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, contentColor = CyberDarkText),
                          shape = RoundedCornerShape(2.dp),
                          modifier = Modifier.weight(1.2f).height(32.dp)
                        ) {
                          Text("FINALIZAR CURADO", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                  }
                  "Listo" -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        val frescoText = if (harvest.pesoFresco > 0.0) " (Fresco: ${harvest.pesoFresco}g)" else ""
                        Text("Rendimiento Seco Final: ${harvest.rendimiento}g$frescoText", fontSize = 12.sp, color = CyberNeonGreen, fontWeight = FontWeight.Bold)
                        // Stars
                        Row {
                          (1..5).forEach { i ->
                            Text(
                              text = if (i <= harvest.calidad) "★" else "☆",
                              color = CyberAmber,
                              fontSize = 13.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }
                      }
                      if (harvest.pesoFresco > 0.0 && harvest.rendimiento > 0.0) {
                        val lossPct = ((harvest.pesoFresco - harvest.rendimiento) / harvest.pesoFresco * 100.0).coerceIn(0.0, 100.0)
                        val yieldPct = (harvest.rendimiento / harvest.pesoFresco * 100.0).coerceIn(0.0, 100.0)
                        val lossFormatted = String.format(Locale.US, "%.1f", lossPct)
                        val yieldFormatted = String.format(Locale.US, "%.1f", yieldPct)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "Humedad perdida: $lossFormatted% | Relación Seco/Fresco: $yieldFormatted%",
                          fontSize = 10.sp,
                          color = CyberNeonCyan,
                          fontWeight = FontWeight.Bold
                        )
                      }
                      if (harvest.notas.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                          text = "Cata/Curado: ${harvest.notas}",
                          fontSize = 11.sp,
                          color = CyberTextPrimary
                        )
                      }
                      Spacer(modifier = Modifier.height(4.dp))
                      val curadoDoneDate = harvest.fechaListo ?: harvest.fechaCurado ?: harvest.fecha
                      Text("Ciclado Terminado el: ${formatDateStr(curadoDoneDate)}", fontSize = 9.sp, color = CyberTextSecondary)
                    }
                  }
                }
                
                // Action: Purgar (delete) harvest record
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = CyberBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                  TextButton(
                    onClick = { showDeleteHarvestDialog = true },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                  ) {
                    Text("ELIMINAR REGISTRO COSECHA", color = CyberNeonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            // Dialog: Enter Curing Details (Weight, Stars, Notes)
            if (showCuradoDialog) {
              var yield by remember { mutableStateOf(if (harvest.rendimiento > 0.0) harvest.rendimiento.toString() else "") }
              var rating by remember { mutableStateOf(harvest.calidad) }
              var notas by remember { mutableStateOf(harvest.notas) }

              AlertDialog(
                onDismissRequest = { showCuradoDialog = false },
                containerColor = CyberCardBg,
                title = { Text("INGRESAR PARÁMETROS DE CURADO", color = CyberAmber, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
                text = {
                  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                      value = yield,
                      onValueChange = { yield = it },
                      label = { Text("Peso seco total obtenido (gramos)") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                      colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberAmber, unfocusedBorderColor = CyberBorder)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Calidad de las flores:", fontSize = 11.sp, color = CyberTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      (1..5).forEach { stars ->
                        val isSel = rating == stars
                        Box(
                          modifier = Modifier
                            .size(32.dp)
                            .background(if (isSel) CyberAmber else CyberBorder, RoundedCornerShape(4.dp))
                            .clickable { rating = stars }
                            .padding(4.dp),
                          contentAlignment = Alignment.Center
                        ) {
                          Text(
                            text = stars.toString() + "★",
                            color = if (isSel) CyberDarkText else CyberTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }
                    }
                    OutlinedTextField(
                      value = notas,
                      onValueChange = { notas = it },
                      label = { Text("Notas de cata / curado") },
                      colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberAmber, unfocusedBorderColor = CyberBorder)
                    )
                  }
                },
                confirmButton = {
                  Button(
                    onClick = {
                      val y = yield.toDoubleOrNull() ?: 0.0
                      val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                      onUpdateHarvestState(crop.id, "Curado", y, rating, notas, today)
                      showCuradoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = CyberDarkText)
                  ) {
                    Text("REGISTRAR CURADO")
                  }
                },
                dismissButton = {
                  TextButton(onClick = { showCuradoDialog = false }) {
                    Text("CANCELAR", color = CyberTextSecondary)
                  }
                }
              )
            }

            // Dialog: Delete Harvest Record Confirmation
            if (showDeleteHarvestDialog) {
              AlertDialog(
                onDismissRequest = { showDeleteHarvestDialog = false },
                containerColor = CyberCardBg,
                title = { Text("ELIMINAR REGISTRO", color = CyberNeonRed, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = { Text("¿Está seguro de que desea eliminar permanentemente este registro de cosecha del archivo histórico?", color = CyberTextPrimary) },
                confirmButton = {
                  Button(
                    onClick = {
                      onDeleteCrop(crop.id)
                      showDeleteHarvestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNeonRed, contentColor = CyberDarkText)
                  ) {
                    Text("ELIMINAR")
                  }
                },
                dismissButton = {
                  TextButton(onClick = { showDeleteHarvestDialog = false }) {
                    Text("CANCELAR", color = CyberTextSecondary)
                  }
                }
              )
            }
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------
// TAB 4: HERRAMIENTAS & RESPALDOS
// ---------------------------------------------------------
@Composable
fun HerramientasTab(
  onExport: () -> String,
  onImport: (String) -> Boolean
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // VPD manual calculator states
  var vpdTemp by remember { mutableStateOf("") }
  var vpdHum by remember { mutableStateOf("") }
  var vpdResult by remember { mutableStateOf<Double?>(null) }

  // Dosage calculator states
  var dosageLiters by remember { mutableStateOf("") }
  var dosageStage by remember { mutableStateOf("Veg") } // Veg, Flora
  var showDosageResults by remember { mutableStateOf(false) }

  // Backup states
  var importText by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "HERRAMIENTAS E INSTRUMENTACIÓN DE CONTROL",
      color = CyberNeonGreen,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold
    )

    // 1. VPD Calculator Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(4.dp),
      colors = CardDefaults.cardColors(containerColor = CyberCardBg),
      border = BorderStroke(1.dp, CyberBorder)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("📟 CALCULADORA DE VPD INDEPENDIENTE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberNeonGreen)
        Text("Calcula el Déficit de Presión de Vapor en tiempo real.", fontSize = 10.sp, color = CyberTextSecondary)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = vpdTemp,
            onValueChange = { vpdTemp = it; vpdResult = null },
            label = { Text("Temp (°C)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = vpdHum,
            onValueChange = { vpdHum = it; vpdResult = null },
            label = { Text("Hum (%)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
            modifier = Modifier.weight(1f)
          )
        }

        Button(
          onClick = {
            val t = vpdTemp.toDoubleOrNull()
            val h = vpdHum.toDoubleOrNull()
            if (t != null && h != null) {
              val svp = 0.61078 * Math.exp((17.27 * t) / (t + 237.3))
              val vpd = svp * (1.0 - (h / 100.0))
              vpdResult = Math.round(vpd * 100.0) / 100.0
            } else {
              Toast.makeText(context, "Ingrese valores numéricos válidos", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, contentColor = CyberDarkText),
          shape = RoundedCornerShape(2.dp),
          modifier = Modifier.fillMaxWidth().height(36.dp)
        ) {
          Text("CALCULAR VPD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        vpdResult?.let { res ->
          val status = when {
            res < 0.4 -> "Muy Bajo (Transpiración ahogada, riesgo de hongos)"
            res <= 0.8 -> "Bajo (Perfecto para enraizado y plantas bebés)"
            res <= 1.2 -> "Ideal (Óptimo para etapa Vegetativa)"
            res <= 1.6 -> "Óptimo Flora (Ideal para desarrollo de cogollos)"
            else -> "Crítico (Estrés hídrico alto, estomas cerrándose)"
          }
          val col = when {
            status.contains("Ideal") || status.contains("Óptimo Flora") -> CyberNeonGreen
            status.contains("Crítico") || status.contains("Muy Bajo") -> CyberNeonRed
            else -> CyberAmber
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .border(0.5.dp, col, RoundedCornerShape(2.dp))
              .background(col.copy(alpha = 0.05f))
              .padding(8.dp)
          ) {
            Column {
              Text("VPD RESULTADO: $res kPa", color = col, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("Estado: $status", color = CyberTextPrimary, fontSize = 11.sp)
            }
          }
        }
      }
    }

    // 2. Dosage Calculator Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(4.dp),
      colors = CardDefaults.cardColors(containerColor = CyberCardBg),
      border = BorderStroke(1.dp, CyberBorder)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("🧪 DOSIFICADOR DE NUTRIENTES", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberNeonGreen)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = dosageLiters,
            onValueChange = { dosageLiters = it; showDosageResults = false },
            label = { Text("Litros de Agua", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
            modifier = Modifier.weight(1.2f)
          )

          // Toggle Veg / Flora Stage
          Row(
            modifier = Modifier
              .weight(1f)
              .border(1.dp, CyberBorder, RoundedCornerShape(4.dp))
              .padding(2.dp)
          ) {
            listOf("Veg", "Flora").forEach { st ->
              val isSel = dosageStage == st
              Box(
                modifier = Modifier
                  .weight(1f)
                  .background(if (isSel) CyberNeonGreen else Color.Transparent, RoundedCornerShape(2.dp))
                  .clickable { dosageStage = st; showDosageResults = false }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(st, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) CyberDarkText else CyberTextPrimary)
              }
            }
          }
        }

        Button(
          onClick = {
            if (dosageLiters.toDoubleOrNull() != null) {
              showDosageResults = true
            } else {
              Toast.makeText(context, "Ingrese litros válidos", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, contentColor = CyberDarkText),
          shape = RoundedCornerShape(2.dp),
          modifier = Modifier.fillMaxWidth().height(36.dp)
        ) {
          Text("CALCULAR DOSIS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        if (showDosageResults) {
          val l = dosageLiters.toDoubleOrNull() ?: 1.0
          val baseNutrient = if (dosageStage == "Veg") 2.5 * l else 3.5 * l
          val additive = if (dosageStage == "Veg") 1.0 * l else 2.0 * l
          val booster = if (dosageStage == "Veg") 0.0 else 1.5 * l

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(0.5.dp, CyberBorder, RoundedCornerShape(2.dp))
              .background(CyberBg)
              .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text("DOSIFICACIÓN SUGERIDA (${dosageStage}):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberTextSecondary)
            Text("• Base Nutriente: $baseNutrient ml", fontSize = 11.sp, color = CyberTextPrimary)
            Text("• Estimulador Radicular/Enzimas: $additive ml", fontSize = 11.sp, color = CyberTextPrimary)
            if (dosageStage == "Flora") {
              Text("• Booster / PK engorde: $booster ml", fontSize = 11.sp, color = CyberTextPrimary)
            }
            Text("• pH recomendado: ${if (dosageStage == "Veg") "5.8 - 6.0" else "6.2 - 6.5"}", fontSize = 10.sp, color = CyberNeonGreen)
          }
        }
      }
    }

    // 3. Backup and Restore
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(4.dp),
      colors = CardDefaults.cardColors(containerColor = CyberCardBg),
      border = BorderStroke(1.dp, CyberBorder)
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("💾 BANCOS DE MEMORIA: RESPALDO JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberNeonGreen)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              val json = onExport()
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("KITT_Backup", json)
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Copia de seguridad copiada al portapapeles!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen, contentColor = CyberDarkText),
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.weight(1f).height(36.dp)
          ) {
            Text("EXPORTAR COPIA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }

        Divider(color = CyberBorder, thickness = 0.5.dp)

        Text("Importar copia de seguridad:", fontSize = 10.sp, color = CyberTextSecondary)
        OutlinedTextField(
          value = importText,
          onValueChange = { importText = it },
          placeholder = { Text("Pegue el código JSON aquí...") },
          minLines = 3,
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberNeonGreen, unfocusedBorderColor = CyberBorder),
          modifier = Modifier.fillMaxWidth()
        )

        Button(
          onClick = {
            if (importText.isBlank()) {
              Toast.makeText(context, "El campo está vacío", Toast.LENGTH_SHORT).show()
              return@Button
            }
            val ok = onImport(importText)
            if (ok) {
              Toast.makeText(context, "Restauración exitosa del sistema K.I.T.T.", Toast.LENGTH_LONG).show()
              importText = ""
            } else {
              Toast.makeText(context, "ERROR: Código de respaldo corrupto", Toast.LENGTH_LONG).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyberNeonRed, contentColor = Color.White),
          shape = RoundedCornerShape(2.dp),
          modifier = Modifier.fillMaxWidth().height(36.dp)
        ) {
          Text("RESTAURAR BANCO DE MEMORIA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))
  }
}

// --- Date Math Helpers ---

fun addDays(dateStr: String, days: Int): String {
  return try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = sdf.parse(dateStr) ?: Date()
    val cal = Calendar.getInstance()
    cal.time = date
    cal.add(Calendar.DAY_OF_YEAR, days)
    sdf.format(cal.time)
  } catch (e: Exception) {
    "N/A"
  }
}

fun formatDateStr(dateStr: String): String {
  return try {
    if (dateStr == "N/A" || dateStr.isBlank()) return "N/A"
    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfOutput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = sdfInput.parse(dateStr)
    if (date != null) sdfOutput.format(date) else dateStr
  } catch (e: Exception) {
    dateStr
  }
}

data class Projections(
  val vegeDate: String,
  val floraDate: String,
  val harvestDate: String,
  val progress: Float,
  val progressText: String
)

fun calculateProjections(crop: Crop): Projections {
  val baseDate = crop.fechaIngreso
  val startState = crop.historial.firstOrNull()?.estado ?: crop.estadoActual
  
  val tVegDays = (crop.tVegetativo ?: 0) * 7
  val tFloDays = (crop.tFloracion ?: 0) * 7
  
  var vegeDate = "N/A"
  var floraDate = "N/A"
  var harvestDate = "N/A"
  
  if (startState == "Clonador") {
    vegeDate = addDays(baseDate, 12)
    if (tVegDays > 0) {
      floraDate = addDays(vegeDate, tVegDays)
      if (tFloDays > 0) {
        harvestDate = addDays(floraDate, tFloDays)
      }
    } else if (tFloDays > 0) {
      harvestDate = addDays(vegeDate, tFloDays)
    }
  } else if (startState == "Vegetativo") {
    vegeDate = baseDate
    if (tVegDays > 0) {
      floraDate = addDays(baseDate, tVegDays)
      if (tFloDays > 0) {
        harvestDate = addDays(floraDate, tFloDays)
      }
    } else if (tFloDays > 0) {
      harvestDate = addDays(baseDate, tFloDays)
    }
  } else if (startState == "Floración") {
    floraDate = baseDate
    if (tFloDays > 0) {
      harvestDate = addDays(baseDate, tFloDays)
    }
  }
  
  var progress = 0.0f
  var progressText = ""
  try {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startVal = sdf.parse(baseDate)?.time ?: 0L
    val nowVal = Date().time
    val elapsedDays = ((nowVal - startVal) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    
    when (crop.estadoActual) {
      "Clonador" -> {
        progress = (elapsedDays / 12.0f).coerceIn(0.0f, 1.0f) * 0.15f
        progressText = "Enraizando: $elapsedDays d / 12 d"
      }
      "Vegetativo" -> {
        val totalVegDays = if (tVegDays > 0) tVegDays else 28
        val vegStartEvent = crop.historial.find { it.estado == "Vegetativo" }
        val vegStartVal = vegStartEvent?.let { sdf.parse(it.fecha)?.time } ?: startVal
        val vegElapsed = ((nowVal - vegStartVal) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        
        progress = 0.15f + (vegElapsed / totalVegDays.toFloat()).coerceIn(0.0f, 1.0f) * 0.35f
        progressText = "Veg: $vegElapsed d / $totalVegDays d"
      }
      "Floración" -> {
        val totalFloDays = if (tFloDays > 0) tFloDays else 56
        val floStartEvent = crop.historial.find { it.estado == "Floración" }
        val floStartVal = floStartEvent?.let { sdf.parse(it.fecha)?.time } ?: startVal
        val floElapsed = ((nowVal - floStartVal) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        
        progress = 0.50f + (floElapsed / totalFloDays.toFloat()).coerceIn(0.0f, 1.0f) * 0.50f
        progressText = "Flora: $floElapsed d / $totalFloDays d"
      }
      "Cosechado" -> {
        progress = 1.0f
        progressText = "Cosechado!"
      }
    }
  } catch (e: Exception) {
    progress = 0.0f
    progressText = "Error cálculo"
  }

  return Projections(
    vegeDate = formatDateStr(vegeDate),
    floraDate = formatDateStr(floraDate),
    harvestDate = formatDateStr(harvestDate),
    progress = progress,
    progressText = progressText
  )
}
