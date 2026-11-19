package com.example.nhoms8.data

import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.repository.BudgetRepository
import com.example.nhoms8.domain.repository.GroupRepository
import com.example.nhoms8.domain.repository.SettlementRepository
import com.example.nhoms8.domain.repository.TransactionRepository
import com.example.nhoms8.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Bộ định vị dịch vụ dùng chung toàn ứng dụng (SpendlyServiceLocator).
 * Khởi tạo lười (lazy) an toàn đa luồng cho Auth, Cloud Firestore, Gemini AI
 * và trạng thái nhóm/quỹ đang được chọn.
 */
object SpendlyServiceLocator {

    val authRepository: FirebaseAuthRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        FirebaseAuthRepository()
    }

    val spendlyRepository: FirestoreSpendlyRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        FirestoreSpendlyRepository()
    }

    val geminiChatRepository: GeminiChatRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        GeminiChatRepository()
    }

    val userRepository: UserRepository
        get() = spendlyRepository

    val groupRepository: GroupRepository
        get() = spendlyRepository

    val transactionRepository: TransactionRepository
        get() = spendlyRepository

    val settlementRepository: SettlementRepository
        get() = spendlyRepository

    val budgetRepository: BudgetRepository
        get() = spendlyRepository

    val activeGroupId: MutableStateFlow<String?> by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MutableStateFlow(null)
    }

    /**
     * Cập nhật ID nhóm/quỹ đang hoạt động trên toàn ứng dụng.
     */
    fun setActiveGroup(groupId: String?) {
        activeGroupId.value = groupId?.takeIf { it.isNotBlank() }
    }
}
