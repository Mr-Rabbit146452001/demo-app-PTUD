package com.example.nhoms8.core.navigation

import android.net.Uri
import com.example.nhoms8.core.designsystem.SpendlyTab

/**
 * Định nghĩa toàn bộ đường dẫn (routes) và hàm tạo route có tham số cho Spendly Navigation Compose.
 */
object SpendlyRoutes {
    const val ARG_EMAIL_OR_PHONE = "emailOrPhone"
    const val ARG_GROUP_ID = "groupId"
    const val ARG_EXPENSE_ID = "expenseId"

    // Onboarding & Auth routes
    const val WELCOME = "welcome"
    const val ONBOARDING_1 = "onboarding_1"
    const val ONBOARDING_2 = "onboarding_2"
    const val ONBOARDING_3 = "onboarding_3"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD_BASE = "forgot_password"
    const val FORGOT_PASSWORD = "$FORGOT_PASSWORD_BASE?prefill={$ARG_EMAIL_OR_PHONE}"

    // Top-level 5-tab hub routes
    const val HOME = "home"
    const val PROFILE = "profile"
    const val GROUPS = "groups"
    const val NOTIFICATIONS = "notifications"

    // Multi-group routes
    const val CREATE_GROUP = "create_group"
    const val JOIN_GROUP = "join_group"
    const val GROUP_DETAIL = "group_detail/{$ARG_GROUP_ID}"

    // Transaction & Split V2 routes
    const val ADD_EDIT_EXPENSE = "add_edit_expense"
    const val SPLIT_BILL_V2 = "split_bill_v2"
    const val ADD_INCOME = "add_income/{$ARG_GROUP_ID}"
    const val EXPENSE_DETAIL = "expense_detail/{$ARG_GROUP_ID}/{$ARG_EXPENSE_ID}"

    // AI Chatbot route
    const val CHATBOT = "chatbot"

    // Legacy & full-view routes kept operational
    const val HISTORY = "history"
    const val REPORTS = "reports"
    const val SETTLEMENT = "settlement"
    const val MEMBERS = "members"
    const val EDIT_MEMBER = "edit_member"

    val topLevelTabRoutes: Set<String> = setOf(HOME, PROFILE, GROUPS, NOTIFICATIONS)

    fun forgotPassword(emailOrPhone: String = ""): String {
        val trimmed = emailOrPhone.trim()
        return if (trimmed.isEmpty()) {
            FORGOT_PASSWORD_BASE
        } else {
            "$FORGOT_PASSWORD_BASE?prefill=${Uri.encode(trimmed)}"
        }
    }

    fun groupDetail(groupId: String): String = "group_detail/${Uri.encode(groupId)}"

    fun addIncome(groupId: String): String = "add_income/${Uri.encode(groupId)}"

    fun expenseDetail(groupId: String, expenseId: String): String =
        "expense_detail/${Uri.encode(groupId)}/${Uri.encode(expenseId)}"

    fun tabForRoute(route: String?): SpendlyTab? = when (route) {
        HOME -> SpendlyTab.HOME
        PROFILE -> SpendlyTab.PROFILE
        GROUPS -> SpendlyTab.GROUPS
        NOTIFICATIONS -> SpendlyTab.NOTIFICATIONS
        else -> null
    }
}
