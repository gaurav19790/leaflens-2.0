package com.example.ui

import android.content.Context

object PointWallet {
    const val SCAN_COST = 10
    const val CHAT_COST = 2
    const val STARTER_POINTS = 30

    private const val PREFS_NAME = "leaflens_prefs"
    private const val POINTS_KEY = "points_balance"
    private const val SEEDED_KEY = "points_seeded"

    fun balance(context: Context): Int {
        val prefs = prefs(context)
        seedIfNeeded(context)
        return prefs.getInt(POINTS_KEY, 0)
    }

    fun add(context: Context, points: Int): Int {
        val next = (balance(context) + points).coerceAtLeast(0)
        prefs(context).edit().putInt(POINTS_KEY, next).apply()
        return next
    }

    fun spend(context: Context, points: Int): Boolean {
        val current = balance(context)
        if (current < points) return false
        prefs(context).edit().putInt(POINTS_KEY, current - points).apply()
        return true
    }

    private fun seedIfNeeded(context: Context) {
        val prefs = prefs(context)
        if (!prefs.getBoolean(SEEDED_KEY, false)) {
            prefs.edit()
                .putInt(POINTS_KEY, STARTER_POINTS)
                .putBoolean(SEEDED_KEY, true)
                .apply()
        }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
