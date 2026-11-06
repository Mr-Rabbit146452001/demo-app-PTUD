package com.example.nhoms8

import com.example.nhoms8.ExpenseCategory
import com.example.nhoms8.data.SpendTogetherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test kiểm thử Dữ liệu và Tính toán tài chính thực tế Nhóm 08 (SpendTogetherRepositoryTest)
 * Thực hiện bởi Kỹ sư Kiểm thử Tự động (qa-automation-tester) - Nhóm 08
 */
class SpendTogetherRepositoryTest {

    @Test
    fun testRealTeamMembersInitialized() {
        val repository = SpendTogetherRepository()
        val members = repository.members.value

        assertEquals("Phải có 4 thành viên chính thức của Nhóm 08", 4, members.size)

        val leader = members.find { it.isLeader }
        assertNotNull("Nhóm 08 phải có trưởng nhóm", leader)
        assertEquals("Lê Văn Hùng", leader!!.name)
        assertEquals("VH", leader.initials)

        val memberNames = members.map { it.name }
        assertTrue(memberNames.contains("Lê Văn Hùng"))
        assertTrue(memberNames.contains("Lý Việt Linh"))
        assertTrue(memberNames.contains("Nguyễn Phan Tấn Đạt"))
        assertTrue(memberNames.contains("Lê Trung Bình"))
    }

    @Test
    fun testDynamicBudgetCalculationsFromRealTransactions() {
        val repository = SpendTogetherRepository()
        val budget = repository.budget.value

        val txList = repository.transactions.value
        val expectedDeposited = txList.filter { it.isIncome }.sumOf { it.amount }
        val expectedSpent = txList.filter { !it.isIncome }.sumOf { it.amount }
        val expectedBalance = expectedDeposited - expectedSpent

        assertEquals("Tổng nạp quỹ phải khớp tổng giao dịch thu", expectedDeposited, budget.depositedAmount)
        assertEquals("Tổng chi phải khớp tổng giao dịch chi", expectedSpent, budget.spentAmount)
        assertEquals("Số dư quỹ phải bằng nạp trừ chi", expectedBalance, budget.totalBalance)
        assertTrue("Số dư quỹ phải lớn hơn 0", budget.totalBalance > 0L)
    }

    @Test
    fun testAddExpenseDynamicallyUpdatesBudgetAndDebts() {
        val repository = SpendTogetherRepository()
        val initialBalance = repository.budget.value.totalBalance
        val initialTxCount = repository.transactions.value.size
        val initialDebtsCount = repository.debts.value.size

        val expenseAmount = 400000L
        repository.addExpenseAndSplit(
            title = "In tài liệu đồ án tốt nghiệp",
            amount = expenseAmount,
            category = ExpenseCategory.OTHER,
            note = "In màu 4 quyển báo cáo",
            payerId = "1",
            selectedMemberIds = listOf("1", "2", "3", "4")
        )

        val updatedBudget = repository.budget.value
        assertEquals("Số dư quỹ phải giảm đúng số tiền chi", initialBalance - expenseAmount, updatedBudget.totalBalance)
        assertEquals("Số lượng giao dịch phải tăng 1", initialTxCount + 1, repository.transactions.value.size)
        assertTrue("Số lượng khoản nợ phải tăng sau khi chia cho 3 thành viên còn lại", repository.debts.value.size >= initialDebtsCount + 3)
    }

    @Test
    fun testAddRealMember() {
        val repository = SpendTogetherRepository()
        val initialSize = repository.members.value.size

        repository.addMember(name = "Phạm Hoàng Nam", phoneNumber = "0977 123 456", isLeader = false)

        val updatedMembers = repository.members.value
        assertEquals(initialSize + 1, updatedMembers.size)

        val newMember = updatedMembers.last()
        assertEquals("Phạm Hoàng Nam", newMember.name)
        assertEquals("0977 123 456", newMember.phoneNumber)
        assertFalse(newMember.isLeader)
    }
}
