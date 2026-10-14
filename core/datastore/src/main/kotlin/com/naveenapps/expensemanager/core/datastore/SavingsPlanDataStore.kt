package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.SavingsCheckIn
import com.naveenapps.expensemanager.core.model.SavingsPlan
import com.naveenapps.expensemanager.core.model.SavingsPlanType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class SavingsPlanDataStore(private val dataStore: DataStore<Preferences>) {

    val plans: Flow<List<SavingsPlan>> = dataStore.data.map { prefs ->
        val jsonString = prefs[KEY_SAVINGS_PLANS]
        parsePlans(jsonString)
    }

    suspend fun savePlan(plan: SavingsPlan) {
        dataStore.edit { prefs ->
            val currentList = parsePlans(prefs[KEY_SAVINGS_PLANS]).toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == plan.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = plan
            } else {
                currentList.add(0, plan)
            }
            prefs[KEY_SAVINGS_PLANS] = serializePlans(currentList)
        }
    }

    suspend fun deposit(planId: String, amountCents: Long, date: String = java.time.LocalDate.now().toString()) {
        require(amountCents > 0L)
        java.time.LocalDate.parse(date)
        dataStore.edit { prefs ->
            val currentList = parsePlans(prefs[KEY_SAVINGS_PLANS]).toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == planId }
            if (existingIndex >= 0) {
                val current = currentList[existingIndex]
                currentList[existingIndex] = current.copy(
                    savedAmountCents = Math.addExact(current.savedAmountCents, amountCents),
                    checkIns = current.checkIns + SavingsCheckIn(date = date, amountCents = amountCents),
                )
                prefs[KEY_SAVINGS_PLANS] = serializePlans(currentList)
            }
        }
    }

    suspend fun deletePlan(planId: String) {
        dataStore.edit { prefs ->
            val currentList = parsePlans(prefs[KEY_SAVINGS_PLANS]).filterNot { it.id == planId }
            prefs[KEY_SAVINGS_PLANS] = serializePlans(currentList)
        }
    }

    private fun parsePlans(jsonString: String?): List<SavingsPlan> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<SavingsPlan>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SavingsPlan(
                        id = obj.optString("id"),
                        name = obj.optString("name", "未命名计划"),
                        type = SavingsPlanType.fromName(obj.optString("type")),
                        targetAmountCents = obj.optLong("targetAmountCents", 100000L),
                        savedAmountCents = obj.optLong("savedAmountCents", 0L),
                        startDate = obj.optString("startDate"),
                        endDate = obj.optString("endDate"),
                        frequency = obj.optString("frequency", "每周"),
                        note = obj.optString("note", ""),
                        checkIns = obj.optJSONArray("checkIns")?.let { entries ->
                            (0 until entries.length()).map { index ->
                                val entry = entries.getJSONObject(index)
                                SavingsCheckIn(id = entry.getString("id"), date = entry.getString("date"),
                                    amountCents = entry.getLong("amountCents"), createdAt = entry.optLong("createdAt"))
                            }
                        }.orEmpty(),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    private fun serializePlans(plans: List<SavingsPlan>): String {
        val array = JSONArray()
        for (p in plans) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("type", p.type.name)
            obj.put("targetAmountCents", p.targetAmountCents)
            obj.put("savedAmountCents", p.savedAmountCents)
            obj.put("startDate", p.startDate)
            obj.put("endDate", p.endDate)
            obj.put("frequency", p.frequency)
            obj.put("note", p.note)
            obj.put("createdAt", p.createdAt)
            obj.put("checkIns", JSONArray().apply {
                p.checkIns.forEach { checkIn ->
                    put(JSONObject().apply {
                        put("id", checkIn.id); put("date", checkIn.date)
                        put("amountCents", checkIn.amountCents); put("createdAt", checkIn.createdAt)
                    })
                }
            })
            array.put(obj)
        }
        return array.toString()
    }

    companion object {
        private val KEY_SAVINGS_PLANS = stringPreferencesKey("jiangji_savings_plans_json")
    }
}
