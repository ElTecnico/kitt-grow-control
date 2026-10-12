package com.example.kittgrowcontrol.data

import org.json.JSONArray
import org.json.JSONObject

data class CycleEvent(
    val estado: String,
    val fecha: String
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("estado", estado)
            put("fecha", fecha)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): CycleEvent {
            return CycleEvent(
                estado = json.getString("estado"),
                fecha = json.getString("fecha")
            )
        }
    }
}

data class WateringLog(
    val fecha: String,
    val volumen: Double,
    val ph: Double,
    val ec: Double,
    val fertilizantes: String,
    val notas: String
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("fecha", fecha)
            put("volumen", volumen)
            put("ph", ph)
            put("ec", ec)
            put("fertilizantes", fertilizantes)
            put("notas", notas)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): WateringLog {
            return WateringLog(
                fecha = json.getString("fecha"),
                volumen = json.optDouble("volumen", 0.0),
                ph = json.optDouble("ph", 0.0),
                ec = json.optDouble("ec", 0.0),
                fertilizantes = json.optString("fertilizantes", ""),
                notas = json.optString("notas", "")
            )
        }
    }
}

data class EnvMeasurement(
    val fecha: String,
    val temp: Double,
    val humedad: Double,
    val vpd: Double
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("fecha", fecha)
            put("temp", temp)
            put("humedad", humedad)
            put("vpd", vpd)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): EnvMeasurement {
            return EnvMeasurement(
                fecha = json.getString("fecha"),
                temp = json.optDouble("temp", 0.0),
                humedad = json.optDouble("humedad", 0.0),
                vpd = json.optDouble("vpd", 0.0)
            )
        }
    }
}

data class HarvestRecord(
    val fecha: String,
    val rendimiento: Double,
    val calidad: Int, // Stars 1 to 5
    val notas: String,
    val subEstado: String = "Secado", // "Secado", "Curado", "Listo"
    val fechaCurado: String? = null,
    val fechaListo: String? = null,
    val pesoFresco: Double = 0.0
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("fecha", fecha)
            put("rendimiento", rendimiento)
            put("calidad", calidad)
            put("notas", notas)
            put("subEstado", subEstado)
            put("fechaCurado", fechaCurado ?: "")
            put("fechaListo", fechaListo ?: "")
            put("pesoFresco", pesoFresco)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): HarvestRecord {
            val subEstVal = json.optString("subEstado", "")
            val defaultSubEstado = if (json.optDouble("rendimiento", 0.0) > 0.0) "Listo" else "Secado"
            val subEstadoResolved = if (subEstVal.isBlank()) defaultSubEstado else subEstVal

            return HarvestRecord(
                fecha = json.getString("fecha"),
                rendimiento = json.optDouble("rendimiento", 0.0),
                calidad = json.optInt("calidad", 5),
                notas = json.optString("notas", ""),
                subEstado = subEstadoResolved,
                fechaCurado = json.optString("fechaCurado", "").let { if (it.isBlank()) null else it },
                fechaListo = json.optString("fechaListo", "").let { if (it.isBlank()) null else it },
                pesoFresco = json.optDouble("pesoFresco", 0.0)
            )
        }
    }
}

data class Crop(
    val id: Long,
    val nombre: String,
    val cantidad: Int,
    val tipo: String,         // Automática, Fotoperiódica, Fotoperiódica Fast
    val tipoGen: String,      // Feminizada, Regular
    val origen: String,       // Semilla, Esqueje
    val estadoActual: String,   // Clonador, Vegetativo, Floración
    val fechaIngreso: String,
    val tVegetativo: Int?,    // in weeks
    val tFloracion: Int?,     // in weeks
    val observaciones: String,
    val historial: List<CycleEvent>,
    val riegos: List<WateringLog>,
    val mediciones: List<EnvMeasurement>,
    val cosecha: HarvestRecord? = null
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("nombre", nombre)
            put("cantidad", cantidad)
            put("tipo", tipo)
            put("tipoGen", tipoGen)
            put("origen", origen)
            put("estadoActual", estadoActual)
            put("fechaIngreso", fechaIngreso)
            put("tVegetativo", tVegetativo ?: -1)
            put("tFloracion", tFloracion ?: -1)
            put("observaciones", observaciones)
            
            val histArr = JSONArray()
            historial.forEach { histArr.put(it.toJson()) }
            put("historial", histArr)
            
            val riegosArr = JSONArray()
            riegos.forEach { riegosArr.put(it.toJson()) }
            put("riegos", riegosArr)
            
            val medArr = JSONArray()
            mediciones.forEach { medArr.put(it.toJson()) }
            put("mediciones", medArr)
            
            cosecha?.let { put("cosecha", it.toJson()) }
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Crop {
            val tVegVal = json.optInt("tVegetativo", -1)
            val tFloVal = json.optInt("tFloracion", -1)
            
            val histList = mutableListOf<CycleEvent>()
            val histArr = json.optJSONArray("historial")
            if (histArr != null) {
                for (i in 0 until histArr.length()) {
                    histList.add(CycleEvent.fromJson(histArr.getJSONObject(i)))
                }
            }
            
            val riegosList = mutableListOf<WateringLog>()
            val riegosArr = json.optJSONArray("riegos")
            if (riegosArr != null) {
                for (i in 0 until riegosArr.length()) {
                    riegosList.add(WateringLog.fromJson(riegosArr.getJSONObject(i)))
                }
            }
            
            val medList = mutableListOf<EnvMeasurement>()
            val medArr = json.optJSONArray("mediciones")
            if (medArr != null) {
                for (i in 0 until medArr.length()) {
                    medList.add(EnvMeasurement.fromJson(medArr.getJSONObject(i)))
                }
            }
            
            val cosechObject = json.optJSONObject("cosecha")
            val cosechaRecord = if (cosechObject != null) HarvestRecord.fromJson(cosechObject) else null

            return Crop(
                id = json.getLong("id"),
                nombre = json.getString("nombre"),
                cantidad = json.optInt("cantidad", 1),
                tipo = json.optString("tipo", "Automática"),
                tipoGen = json.optString("tipoGen", "Feminizada"),
                origen = json.optString("origen", "Semilla"),
                estadoActual = json.optString("estadoActual", "Vegetativo"),
                fechaIngreso = json.getString("fechaIngreso"),
                tVegetativo = if (tVegVal == -1) null else tVegVal,
                tFloracion = if (tFloVal == -1) null else tFloVal,
                observaciones = json.optString("observaciones", ""),
                historial = histList,
                riegos = riegosList,
                mediciones = medList,
                cosecha = cosechaRecord
            )
        }
    }
}
