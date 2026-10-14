package com.naveenapps.expensemanager.initializer

import android.content.Context
import androidx.startup.Initializer
import androidx.room.withTransaction
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.domain.usecase.account.AddAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.AddCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.util.Calendar
import java.util.Date

val BASE_CATEGORY_LIST = listOf(
    category("1", "餐饮", CategoryType.EXPENSE, "restaurant", "#FF7043", "food"),
    category("2", "购物", CategoryType.EXPENSE, "shopping_cart", "#EC407A", "shopping"),
    category("3", "宿舍", CategoryType.EXPENSE, "apartment", "#5C6BC0", "dormitory"),
    category("4", "零食", CategoryType.EXPENSE, "emoji_food_beverage", "#FFA726", "snacks"),
    category("5", "交通", CategoryType.EXPENSE, "directions_bus", "#26A69A", "transportation"),
    category("6", "旅游", CategoryType.EXPENSE, "flight", "#29B6F6", "travel"),
    category("7", "医疗", CategoryType.EXPENSE, "medication", "#EF5350", "medical"),
    category("8", "日用品", CategoryType.EXPENSE, "shopping_cart", "#9CCC65", "daily_needs"),
    category("9", "送礼", CategoryType.EXPENSE, "featured_seasonal_and_gifts", "#AB47BC", "gifts"),
    category("10", "奶茶咖啡", CategoryType.EXPENSE, "emoji_food_beverage", "#8D6E63", "beverages"),
    category("11", "娱乐", CategoryType.EXPENSE, "sports_esports", "#7E57C2", "entertainment"),
    category("12", "美容", CategoryType.EXPENSE, "diversity", "#F06292", "beauty"),
    category("13", "学习", CategoryType.EXPENSE, "laptop_chromebook", "#42A5F5", "learning"),
    category("14", "运动", CategoryType.EXPENSE, "fitness_center", "#66BB6A", "sports"),
    category("15", "生活费", CategoryType.INCOME, "account_balance_wallet", "#43A047", "living_expense"),
    category("16", "兼职", CategoryType.INCOME, "payments", "#2E7D32", "part_time"),
    category("17", "礼金", CategoryType.INCOME, "redeem", "#1B5E20", "gift_income"),
    category("18", "网络", CategoryType.INCOME, "laptop_chromebook", "#00897B", "internet"),
)

private fun category(
    id: String,
    name: String,
    type: CategoryType,
    iconName: String,
    backgroundColor: String,
    defaultCategoryKey: String,
) = Category(
    id = id,
    name = name,
    type = type,
    storedIcon = StoredIcon(name = iconName, backgroundColor = backgroundColor),
    createdOn = Date(),
    updatedOn = Date(),
    defaultCategoryKey = defaultCategoryKey,
)

val BASE_ACCOUNT_LIST = listOf(
    Account(
        "1",
        "Cash",
        AccountType.REGULAR,
        storedIcon = StoredIcon(
            name = "savings",
            backgroundColor = "#4CAF50",
        ),
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    ),
    Account(
        "2",
        "Card-xxx",
        AccountType.CREDIT,
        storedIcon = StoredIcon(
            name = "credit_card",
            backgroundColor = "#4CAF50",
        ),
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    ),
    Account(
        "3",
        "Bank Account",
        AccountType.REGULAR,
        storedIcon = StoredIcon(
            name = "account_balance",
            backgroundColor = "#4CAF50",
        ),
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    ),
)

class PreloadDatabaseInitializer : Initializer<Unit> {

    override fun create(context: Context) {

        val getPreloadStatusUseCase: GetPreloadStatusUseCase =  GlobalContext.get().get()
        val setPreloadStatusUseCase: SetPreloadStatusUseCase =  GlobalContext.get().get()
        val addAccountUseCase: AddAccountUseCase =  GlobalContext.get().get()
        val addCategoryUseCase: AddCategoryUseCase =  GlobalContext.get().get()
        val database: ExpenseManagerDatabase = GlobalContext.get().get()

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            val isPreloaded = getPreloadStatusUseCase.invoke()

            if (isPreloaded.not()) {
                // A missing preferences flag does not mean the user's database is empty.
                // REPLACE on seeded IDs would delete old accounts and cascade into history.
                database.withTransaction {
                    val hasExistingData = hasExistingUserData(database.openHelper.writableDatabase)
                    if (!hasExistingData) {
                        BASE_CATEGORY_LIST.forEach { value ->
                            val result = addCategoryUseCase.invoke(value)
                            check(result is Resource.Success && result.data) { "Unable to preload category" }
                        }
                        BASE_ACCOUNT_LIST.forEach { value ->
                            val result = addAccountUseCase.invoke(value)
                            check(result is Resource.Success && result.data) { "Unable to preload account" }
                        }
                    }
                }

                setPreloadStatusUseCase.invoke(true)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(
            KoinInitializer::class.java,
            WorkManagerInitializer::class.java
        )
    }
}

internal fun hasExistingUserData(database: androidx.sqlite.db.SupportSQLiteDatabase): Boolean =
    database.query(
        "SELECT EXISTS (SELECT 1 FROM `account` UNION ALL SELECT 1 FROM `category` " +
            "UNION ALL SELECT 1 FROM `transaction` UNION ALL SELECT 1 FROM `budget` " +
            "UNION ALL SELECT 1 FROM `asset` UNION ALL SELECT 1 FROM `anxiety_relief_item` " +
            "UNION ALL SELECT 1 FROM `monthly_saving` UNION ALL SELECT 1 FROM `salary_cycle` " +
            "UNION ALL SELECT 1 FROM `category_suggestion_rule`)",
    ).use { cursor -> cursor.moveToFirst() && cursor.getInt(0) != 0 }
