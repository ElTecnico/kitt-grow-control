package com.example.kittgrowcontrol.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

interface DataRepository {
    val data: Flow<List<Crop>>
    fun addCrop(crop: Crop)
    fun updateCrop(crop: Crop)
    fun deleteCrop(cropId: Long)
    fun logWatering(cropId: Long, log: WateringLog)
    fun logEnvironment(cropId: Long, temp: Double, humidity: Double)
    fun changeState(cropId: Long, newState: String, date: String, quantity: Int)
    fun harvestCrop(cropId: Long, date: String, quantity: Int, pesoFresco: Double)
    fun updateHarvestState(cropId: Long, subEstado: String, yieldGrams: Double, ratingStars: Int, notes: String, date: String)
    fun exportJson(): String
    fun importJson(jsonString: String): Boolean
}

class DefaultDataRepository(private val context: Context) : DataRepository {
    private val fileName = "kitt_cultivos.json"
    private val file = File(context.filesDir, fileName)
    private val _crops = MutableStateFlow<List<Crop>>(emptyList())
    override val data: Flow<List<Crop>> = _crops.asStateFlow()

    init {
        loadCrops()
    }

    private fun loadCrops() {
        try {
            if (file.exists()) {
                val jsonString = file.readText()
                val list = parseJsonString(jsonString)
                _crops.value = list
            } else {
                val defaultList = createDemoData()
                _crops.value = defaultList
                saveCrops(defaultList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _crops.value = emptyList()
        }
    }

    private fun saveCrops(list: List<Crop>) {
        try {
            val jsonArr = JSONArray()
            list.forEach { jsonArr.put(it.toJson()) }
            file.writeText(jsonArr.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseJsonString(jsonString: String): List<Crop> {
        val list = mutableListOf<Crop>()
        if (jsonString.isBlank()) return list
        val jsonArr = JSONArray(jsonString)
        for (i in 0 until jsonArr.length()) {
            list.add(Crop.fromJson(jsonArr.getJSONObject(i)))
        }
        return list
    }

    override fun addCrop(crop: Crop) {
        val currentList = _crops.value.toMutableList()
        currentList.add(crop)
        _crops.value = currentList
        saveCrops(currentList)
    }

    override fun updateCrop(crop: Crop) {
        val currentList = _crops.value.map { if (it.id == crop.id) crop else it }
        _crops.value = currentList
        saveCrops(currentList)
    }

    override fun deleteCrop(cropId: Long) {
        val currentList = _crops.value.filter { it.id != cropId }
        _crops.value = currentList
        saveCrops(currentList)
    }

    override fun logWatering(cropId: Long, log: WateringLog) {
        val currentList = _crops.value.map { crop ->
            if (crop.id == cropId) {
                crop.copy(riegos = crop.riegos + log)
            } else crop
        }
        _crops.value = currentList
        saveCrops(currentList)
    }

    override fun logEnvironment(cropId: Long, temp: Double, humidity: Double) {
        val vpd = calculateVpdValue(temp, humidity)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        val dateStr = dateFormat.format(java.util.Date())
        val log = EnvMeasurement(dateStr, temp, humidity, vpd)

        val currentList = _crops.value.map { crop ->
            if (crop.id == cropId) {
                crop.copy(mediciones = crop.mediciones + log)
            } else crop
        }
        _crops.value = currentList
        saveCrops(currentList)
    }

    override fun changeState(cropId: Long, newState: String, date: String, quantity: Int) {
        val currentList = _crops.value.toMutableList()
        val originalIndex = currentList.indexOfFirst { it.id == cropId }
        if (originalIndex == -1) return

        val original = currentList[originalIndex]
        if (quantity >= original.cantidad) {
            // Move whole batch
            val newHist = original.historial + CycleEvent(newState, date)
            currentList[originalIndex] = original.copy(
                estadoActual = newState,
                historial = newHist
            )
        } else {
            // Split batch: decrement original quantity
            currentList[originalIndex] = original.copy(
                cantidad = original.cantidad - quantity
            )
            // Create new child batch
            val childId = System.currentTimeMillis()
            val childHist = original.historial + CycleEvent(newState, date)
            val child = original.copy(
                id = childId,
                nombre = "${original.nombre} ($newState)",
                cantidad = quantity,
                estadoActual = newState,
                historial = childHist
            )
            currentList.add(child)
        }

        _crops.value = currentList
        saveCrops(currentList)
    }

    override fun harvestCrop(cropId: Long, date: String, quantity: Int, pesoFresco: Double) {
        val currentList = _crops.value.toMutableList()
        val originalIndex = currentList.indexOfFirst { it.id == cropId }
        if (originalIndex == -1) return

        val original = currentList[originalIndex]
        val harvest = HarvestRecord(fecha = date, rendimiento = 0.0, calidad = 5, notas = "", subEstado = "Secado", pesoFresco = pesoFresco)

        if (quantity >= original.cantidad) {
            // Move whole batch
            val newHist = original.historial + CycleEvent("Cosechado", date)
            currentList[originalIndex] = original.copy(
                estadoActual = "Cosechado",
                historial = newHist,
                cosecha = harvest
            )
        } else {
            // Split batch: decrement original quantity
            currentList[originalIndex] = original.copy(
                cantidad = original.cantidad - quantity
            )
            // Create new harvested batch
            val childId = System.currentTimeMillis()
            val childHist = original.historial + CycleEvent("Cosechado", date)
            val child = original.copy(
                id = childId,
                nombre = "${original.nombre} (Cosecha)",
                cantidad = quantity,
                estadoActual = "Cosechado",
                historial = childHist,
                cosecha = harvest
            )
            currentList.add(child)
        }

        _crops.value = currentList
        saveCrops(currentList)
    }

    override fun updateHarvestState(cropId: Long, subEstado: String, yieldGrams: Double, ratingStars: Int, notes: String, date: String) {
        val currentList = _crops.value.map { crop ->
            if (crop.id == cropId && crop.cosecha != null) {
                val updatedCosecha = crop.cosecha.copy(
                    subEstado = subEstado,
                    rendimiento = if (subEstado == "Curado") yieldGrams else crop.cosecha.rendimiento,
                    calidad = if (subEstado == "Curado") ratingStars else crop.cosecha.calidad,
                    notas = if (subEstado == "Curado") notes else crop.cosecha.notas,
                    fechaCurado = if (subEstado == "Curado") date else crop.cosecha.fechaCurado,
                    fechaListo = if (subEstado == "Listo") date else crop.cosecha.fechaListo
                )
                val newHist = crop.historial + CycleEvent(subEstado, date)
                crop.copy(
                    cosecha = updatedCosecha,
                    historial = newHist
                )
            } else crop
        }
        _crops.value = currentList
        saveCrops(currentList)
    }

    override fun exportJson(): String {
        val jsonArr = JSONArray()
        _crops.value.forEach { jsonArr.put(it.toJson()) }
        return jsonArr.toString()
    }

    override fun importJson(jsonString: String): Boolean {
        return try {
            val list = parseJsonString(jsonString)
            _crops.value = list
            saveCrops(list)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun calculateVpdValue(temp: Double, rh: Double): Double {
        val svp = 0.61078 * Math.exp((17.27 * temp) / (temp + 237.3))
        val vpd = svp * (1.0 - (rh / 100.0))
        return Math.round(vpd * 100.0) / 100.0
    }

    private fun createDemoData(): List<Crop> {
        fun getPastDate(daysAgo: Int): String {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
            return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
        }

        return listOf(
            Crop(
                id = 1688200000001L,
                nombre = "Amnesia Haze Auto",
                cantidad = 4,
                tipo = "Automática",
                tipoGen = "Feminizada",
                origen = "Semilla",
                estadoActual = "Floración",
                fechaIngreso = getPastDate(35),
                tVegetativo = 4,
                tFloracion = 9,
                observaciones = "Fenotipo sativa marcado. Responde muy bien a LST leve en ramas bajas.",
                historial = listOf(
                    CycleEvent("Clonador", getPastDate(35)),
                    CycleEvent("Vegetativo", getPastDate(25)),
                    CycleEvent("Floración", getPastDate(5))
                ),
                riegos = listOf(
                    WateringLog(getPastDate(5), 2.0, 6.2, 1.2, "Base Floración A+B, Carbohidratos", "Primer riego en floración. Transición normal."),
                    WateringLog(getPastDate(2), 2.0, 6.3, 1.4, "Base Floración A+B, Estimulante Flora", "Aumento de EC. Puntas sin signos de sobrefertilización.")
                ),
                mediciones = listOf(
                    EnvMeasurement(getPastDate(1) + " 14:00", 24.5, 52.0, 1.45)
                )
            ),
            Crop(
                id = 1688200000002L,
                nombre = "Super Lemon Haze",
                cantidad = 6,
                tipo = "Fotoperiódica",
                tipoGen = "Feminizada",
                origen = "Esqueje",
                estadoActual = "Vegetativo",
                fechaIngreso = getPastDate(15),
                tVegetativo = 5,
                tFloracion = 8,
                observaciones = "Esquejes cortados de la madre A. Enraizado rápido de 10 días.",
                historial = listOf(
                    CycleEvent("Clonador", getPastDate(15)),
                    CycleEvent("Vegetativo", getPastDate(5))
                ),
                riegos = listOf(
                    WateringLog(getPastDate(4), 1.5, 5.9, 0.8, "Enraizador, Base Veg", "Riego post-trasplante. Hojas bien erguidas.")
                ),
                mediciones = listOf(
                    EnvMeasurement(getPastDate(1) + " 14:00", 25.2, 65.0, 1.12)
                )
            ),
            Crop(
                id = 1688200000003L,
                nombre = "Gorilla Glue Clone",
                cantidad = 10,
                tipo = "Fotoperiódica Fast",
                tipoGen = "Regular",
                origen = "Esqueje",
                estadoActual = "Clonador",
                fechaIngreso = getPastDate(3),
                tVegetativo = 3,
                tFloracion = 8,
                observaciones = "Puestos en clonador aeropónico. Humedad constante y luz baja.",
                historial = listOf(
                    CycleEvent("Clonador", getPastDate(3))
                ),
                riegos = listOf(
                    WateringLog(getPastDate(3), 0.1, 5.6, 0.4, "Microdosis Enraizador", "Puesta inicial. Temperatura agua 21°C.")
                ),
                mediciones = listOf(
                    EnvMeasurement(getPastDate(1) + " 14:00", 23.8, 80.0, 0.59)
                )
            ),
            Crop(
                id = 1688200000004L,
                nombre = "Northern Lights",
                cantidad = 2,
                tipo = "Automática",
                tipoGen = "Feminizada",
                origen = "Semilla",
                estadoActual = "Cosechado",
                fechaIngreso = getPastDate(90),
                tVegetativo = 3,
                tFloracion = 8,
                observaciones = "Muy resinosa, cogollo central gordo y compacto.",
                historial = listOf(
                    CycleEvent("Clonador", getPastDate(90)),
                    CycleEvent("Vegetativo", getPastDate(80)),
                    CycleEvent("Floración", getPastDate(60)),
                    CycleEvent("Cosechado", getPastDate(10))
                ),
                riegos = emptyList(),
                mediciones = emptyList(),
                cosecha = HarvestRecord(getPastDate(10), 124.5, 4, "Excelente sabor terroso dulce y relajación intensa. Buena cantidad de resina.", subEstado = "Listo")
            ),
            Crop(
                id = 1688200000005L,
                nombre = "OG Kush Esquejes",
                cantidad = 5,
                tipo = "Fotoperiódica",
                tipoGen = "Feminizada",
                origen = "Esqueje",
                estadoActual = "Cosechado",
                fechaIngreso = getPastDate(120),
                tVegetativo = 4,
                tFloracion = 9,
                observaciones = "Esquejes seleccionados. Gran vigor en crecimiento.",
                historial = listOf(
                    CycleEvent("Clonador", getPastDate(120)),
                    CycleEvent("Vegetativo", getPastDate(110)),
                    CycleEvent("Floración", getPastDate(80)),
                    CycleEvent("Cosechado", getPastDate(20))
                ),
                riegos = emptyList(),
                mediciones = emptyList(),
                cosecha = HarvestRecord(getPastDate(20), 285.0, 5, "Calidad de copa. Cogollos duros como rocas con un aroma a pino y combustible exquisito.", subEstado = "Listo")
            )
        )
    }
}
