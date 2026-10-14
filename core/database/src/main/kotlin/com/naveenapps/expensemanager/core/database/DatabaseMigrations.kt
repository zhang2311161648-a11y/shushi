package com.naveenapps.expensemanager.core.database

import androidx.room.migration.Migration

import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `bookkeeping_draft_batch` (`id` TEXT NOT NULL, `ledgerId` TEXT NOT NULL, `payload` TEXT NOT NULL, `state` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookkeeping_draft_batch_ledgerId` ON `bookkeeping_draft_batch` (`ledgerId`)")
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `account` ADD COLUMN `sequence` INTEGER NOT NULL DEFAULT 0")
    }
}

internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // DROP TABLE triggers ON DELETE CASCADE even during the Room migration transaction.
        // Snapshot relation rows before rebuilding their parent and restore their original IDs.
        db.execSQL("CREATE TEMP TABLE `budget_account_backup_3_4` AS SELECT * FROM `budget_account_relation`")
        db.execSQL("CREATE TEMP TABLE `budget_category_backup_3_4` AS SELECT * FROM `budget_category_relation`")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `budget_new` (" +
                "`id` TEXT NOT NULL, " +
                "`selected_month` TEXT NOT NULL, " +
                "`amount` REAL NOT NULL, " +
                "`all_accounts_selected` INTEGER NOT NULL, " +
                "`all_categories_selected` INTEGER NOT NULL, " +
                "`created_on` INTEGER NOT NULL, " +
                "`updated_on` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "INSERT INTO `budget_new` " +
                "(`id`, `selected_month`, `amount`, `all_accounts_selected`, `all_categories_selected`, `created_on`, `updated_on`) " +
                "SELECT `id`, `selected_month`, `amount`, `all_accounts_selected`, `all_categories_selected`, `created_on`, `updated_on` " +
                "FROM `budget`"
        )
        db.execSQL("DROP TABLE `budget`")
        db.execSQL("ALTER TABLE `budget_new` RENAME TO `budget`")
        db.execSQL("INSERT OR IGNORE INTO `budget_account_relation` (`id`, `budget_id`, `account_id`, `created_on`, `updated_on`) " +
            "SELECT `id`, `budget_id`, `account_id`, `created_on`, `updated_on` FROM `budget_account_backup_3_4`")
        db.execSQL("INSERT OR IGNORE INTO `budget_category_relation` (`id`, `budget_id`, `category_id`, `created_on`, `updated_on`) " +
            "SELECT `id`, `budget_id`, `category_id`, `created_on`, `updated_on` FROM `budget_category_backup_3_4`")
        db.execSQL("DROP TABLE `budget_account_backup_3_4`")
        db.execSQL("DROP TABLE `budget_category_backup_3_4`")
    }
}

/**
 * Adds a nullable `default_category_key` column used to mark which rows are the app's
 * built-in seeded categories (as opposed to user-created ones), so their display name can be
 * localized via string resources instead of the raw stored `name`.
 *
 * For existing installs, the 11 categories seeded by `PreloadDatabaseInitializer.BASE_CATEGORY_LIST`
 * always got the deterministic ids "1".."11" on first launch (that list is only ever inserted into
 * an empty table). We backfill the key for those ids, but only when `name` still matches the
 * original seeded English name — if a user has since renamed one of these categories, we leave
 * `default_category_key` null so their custom name is preserved instead of being overwritten by a
 * translation the user never asked for.
 */
internal val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `category` ADD COLUMN `default_category_key` TEXT DEFAULT NULL")

        val defaults = listOf(
            Triple("1", "Clothing", "clothing"),
            Triple("2", "Entertainment", "entertainment"),
            Triple("3", "Food", "food"),
            Triple("4", "Health", "health"),
            Triple("5", "Leisure", "leisure"),
            Triple("6", "Shopping", "shopping"),
            Triple("7", "Transportation", "transportation"),
            Triple("8", "Utilities", "utilities"),
            Triple("9", "Salary", "salary"),
            Triple("10", "Gift", "gift"),
            Triple("11", "Coupons", "coupons"),
        )

        defaults.forEach { (id, originalName, key) ->
            db.execSQL(
                "UPDATE `category` SET `default_category_key` = ? " +
                    "WHERE `id` = ? AND `name` = ?",
                arrayOf(key, id, originalName),
            )
        }
    }
}

/**
 * Adds a `period_type` column to `budget` so a budget can cover either a single month or an
 * entire year (see `core.model.BudgetPeriod`). `BudgetPeriod.MONTHLY` is ordinal 0, matching the
 * column's `DEFAULT 0`, so every budget that existed before this migration — all of which were
 * implicitly monthly — is correctly backfilled without needing a separate UPDATE statement.
 */
internal val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `budget` ADD COLUMN `period_type` INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Adds a nullable `custom_image_path` column to both `category` and `account`, holding the
 * absolute path to a user-picked/captured photo copied into app-private storage (see
 * `ImageStorageRepository`). Defaults to NULL for every existing row, which correctly means "no
 * custom photo — keep showing the icon/color" for every row that existed before this migration.
 *
 * Both columns are added in this single migration (rather than one migration per table) because
 * this app version has not been published yet — no installed build has ever run with only the
 * `category` column present, so there's no real-world database to preserve a separate step for.
 */
internal val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `category` ADD COLUMN `custom_image_path` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `account` ADD COLUMN `custom_image_path` TEXT DEFAULT NULL")
    }
}

/**
 * Adds the `transaction_attachment` table — one or more receipt/proof-of-payment photos per
 * transaction. Unlike Category/Account's single `custom_image_path` column, a transaction can
 * have any number of attachments, so this is a child table (same one-to-many shape as
 * `budget_category_relation`/`budget_account_relation`) rather than a column, with
 * `ON DELETE CASCADE` so deleting a transaction automatically drops its attachment rows. The
 * actual image files on disk are cleaned up separately by `ImageStorageRepository`, since Room
 * only owns the row, not the file.
 */
internal val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `transaction_attachment` (" +
                "`id` TEXT NOT NULL, " +
                "`transaction_id` TEXT NOT NULL, " +
                "`image_path` TEXT NOT NULL, " +
                "`created_on` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`transaction_id`) REFERENCES `transaction`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
    }
}

/** Adds the standalone asset ledger without changing any transaction or budget tables. */
internal val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `asset` (" +
                "`id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`amount` REAL NOT NULL, " +
                "`note` TEXT NOT NULL, " +
                "`created_on` INTEGER NOT NULL, " +
                "`updated_on` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
    }
}

/** Adds a separate wellbeing-helper table. It has no foreign keys into transactions or budgets. */
internal val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `anxiety_relief_item` (" +
                "`id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`image_uri` TEXT, " +
                "`price_cents` INTEGER NOT NULL, " +
                "`purchase_date` TEXT NOT NULL, " +
                "`target_daily_cost_cents` INTEGER NOT NULL, " +
                "`note` TEXT NOT NULL, " +
                "`created_at` INTEGER NOT NULL, " +
                "`updated_at` INTEGER NOT NULL, " +
                "`is_archived` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))"
        )
    }
}

/** Adds the independent monthly savings ledger without touching transactions or assets. */
internal val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `monthly_saving` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`year` INTEGER NOT NULL, " +
                "`month` INTEGER NOT NULL, " +
                "`amount_cents` INTEGER NOT NULL, " +
                "`note` TEXT, " +
                "`created_at` INTEGER NOT NULL, " +
                "`updated_at` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_monthly_saving_year_month` " +
                "ON `monthly_saving` (`year`, `month`)"
        )
    }
}

/** Adds exact cents balances and the historical post-transaction balance snapshot. */
internal val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `account` ADD COLUMN `initial_balance_cents` INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE `account` ADD COLUMN `current_balance_cents` INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "UPDATE `account` SET " +
                "`current_balance_cents` = CAST(ROUND(`amount` * 100.0) AS INTEGER), " +
                "`initial_balance_cents` = CAST(ROUND(`amount` * 100.0) AS INTEGER) - " +
                "COALESCE((SELECT CAST(ROUND(SUM(CASE " +
                "WHEN t.`type` = 0 AND t.`from_account_id` = `account`.`id` THEN t.`amount` " +
                "WHEN t.`type` = 1 AND t.`from_account_id` = `account`.`id` THEN -t.`amount` " +
                "WHEN t.`type` = 2 AND t.`from_account_id` = `account`.`id` THEN -t.`amount` " +
                "WHEN t.`type` = 2 AND t.`to_account_id` = `account`.`id` THEN t.`amount` " +
                "ELSE 0.0 END) * 100.0) AS INTEGER) FROM `transaction` t " +
                "WHERE t.`from_account_id` = `account`.`id` OR t.`to_account_id` = `account`.`id`), 0)",
        )
        db.execSQL(
            "ALTER TABLE `transaction` ADD COLUMN `balance_after_transaction_cents` INTEGER DEFAULT NULL",
        )
    }
}

/** Adds the device-local learned category rules and stable keys for the current built-ins. */
internal val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `category_suggestion_rule` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`pattern` TEXT NOT NULL, " +
                "`normalized_pattern` TEXT NOT NULL, " +
                "`category_id` TEXT NOT NULL, " +
                "`match_type` INTEGER NOT NULL, " +
                "`source` INTEGER NOT NULL, " +
                "`usage_count` INTEGER NOT NULL, " +
                "`created_at` INTEGER NOT NULL, " +
                "`updated_at` INTEGER NOT NULL, " +
                "`last_used_at` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS " +
                "`index_category_suggestion_rule_normalized_pattern_category_id_match_type_source` " +
                "ON `category_suggestion_rule` (`normalized_pattern`, `category_id`, `match_type`, `source`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_category_suggestion_rule_category_id` " +
                "ON `category_suggestion_rule` (`category_id`)"
        )

        listOf(
            Triple("1", "餐饮", "food"),
            Triple("2", "购物", "shopping"),
            Triple("3", "宿舍", "dormitory"),
            Triple("4", "零食", "snacks"),
            Triple("5", "交通", "transportation"),
            Triple("6", "旅游", "travel"),
            Triple("7", "医疗", "medical"),
            Triple("8", "日用品", "daily_needs"),
            Triple("9", "送礼", "gifts"),
            Triple("10", "奶茶咖啡", "beverages"),
            Triple("11", "娱乐", "entertainment"),
            Triple("12", "美容", "beauty"),
            Triple("13", "学习", "learning"),
            Triple("14", "运动", "sports"),
            Triple("15", "生活费", "living_expense"),
            Triple("16", "兼职", "part_time"),
            Triple("17", "礼金", "gift_income"),
            Triple("18", "网络", "internet"),
        ).forEach { (id, name, key) ->
            db.execSQL(
                "UPDATE `category` SET `default_category_key` = ? " +
                    "WHERE `default_category_key` IS NULL AND `id` = ? AND `name` = ?",
                arrayOf(key, id, name),
            )
        }
    }
}

/** Adds user-defined salary-cycle anchors without touching transaction history. */
internal val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `salary_cycle` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`start_date` TEXT NOT NULL, " +
                "`note` TEXT, " +
                "`created_at` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_salary_cycle_start_date` " +
                "ON `salary_cycle` (`start_date`)"
        )
    }
}

/**
 * Stores the exact accounting-period snapshot for budgets and savings.
 *
 * Existing monthly savings are backfilled as calendar-month records before the old composite
 * uniqueness index is replaced. No transaction rows are touched by this migration.
 */
internal val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `budget` ADD COLUMN `accounting_period_start_date` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `budget` ADD COLUMN `accounting_period_end_date` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `monthly_saving` ADD COLUMN `period_key` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `monthly_saving` ADD COLUMN `period_start_date` TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE `monthly_saving` ADD COLUMN `period_end_date` TEXT DEFAULT NULL")
        db.execSQL(
            "UPDATE `monthly_saving` SET " +
                "`period_key` = 'CALENDAR_MONTH:' || printf('%04d-%02d-01', `year`, `month`), " +
                "`period_start_date` = printf('%04d-%02d-01', `year`, `month`), " +
                "`period_end_date` = date(printf('%04d-%02d-01', `year`, `month`), '+1 month') " +
                "WHERE `period_key` IS NULL",
        )
        db.execSQL("DROP INDEX IF EXISTS `index_monthly_saving_year_month`")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_monthly_saving_period_key` " +
                "ON `monthly_saving` (`period_key`)",
        )
    }
}

/** Adds immutable original/base currency and exchange-rate snapshots to every transaction. */
internal val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `original_amount_cents` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `original_currency_code` TEXT NOT NULL DEFAULT 'CNY'")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `original_currency_symbol` TEXT NOT NULL DEFAULT '¥'")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `original_currency_name` TEXT NOT NULL DEFAULT 'Chinese Yuan'")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `original_currency_position` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `base_amount_cents` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `base_currency_code` TEXT NOT NULL DEFAULT 'CNY'")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `base_currency_symbol` TEXT NOT NULL DEFAULT '¥'")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `base_currency_name` TEXT NOT NULL DEFAULT 'Chinese Yuan'")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `base_currency_position` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `exchange_rate_micros` INTEGER NOT NULL DEFAULT 1000000")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `exchange_rate_source` TEXT NOT NULL DEFAULT 'legacy'")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `exchange_rate_updated_at` INTEGER DEFAULT NULL")
        db.execSQL(
            "UPDATE `transaction` SET " +
                "`original_amount_cents` = CAST(ROUND(`amount` * 100.0) AS INTEGER), " +
                "`base_amount_cents` = CAST(ROUND(`amount` * 100.0) AS INTEGER)",
        )
    }
}

/** Introduces independent user ledgers and assigns all pre-existing rows to the default ledger. */
internal val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `ledger` (" +
                "`id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`created_on` INTEGER NOT NULL, " +
                "`updated_on` INTEGER NOT NULL, " +
                "`is_archived` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))",
        )
        val now = System.currentTimeMillis()
        db.execSQL(
            "INSERT OR IGNORE INTO `ledger` (id, name, created_on, updated_on, is_archived) " +
                "VALUES (?, ?, ?, ?, 0)",
            arrayOf("default", "我的账本", now, now),
        )

        db.execSQL("ALTER TABLE `category` ADD COLUMN `ledger_id` TEXT NOT NULL DEFAULT 'default'")
        db.execSQL("ALTER TABLE `account` ADD COLUMN `ledger_id` TEXT NOT NULL DEFAULT 'default'")
        db.execSQL("ALTER TABLE `transaction` ADD COLUMN `ledger_id` TEXT NOT NULL DEFAULT 'default'")
        db.execSQL("ALTER TABLE `budget` ADD COLUMN `ledger_id` TEXT NOT NULL DEFAULT 'default'")
        db.execSQL("ALTER TABLE `budget_category_relation` ADD COLUMN `ledger_id` TEXT NOT NULL DEFAULT 'default'")
        db.execSQL("ALTER TABLE `budget_account_relation` ADD COLUMN `ledger_id` TEXT NOT NULL DEFAULT 'default'")
        db.execSQL("ALTER TABLE `category_suggestion_rule` ADD COLUMN `ledger_id` TEXT NOT NULL DEFAULT 'default'")

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_ledger_is_archived` ON `ledger` (`is_archived`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_category_ledger_id` ON `category` (`ledger_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_account_ledger_id` ON `account` (`ledger_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_transaction_ledger_id_created_on` ON `transaction` (`ledger_id`, `created_on`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_ledger_id` ON `budget` (`ledger_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_category_relation_ledger_id` ON `budget_category_relation` (`ledger_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_account_relation_ledger_id` ON `budget_account_relation` (`ledger_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_category_suggestion_rule_ledger_id` ON `category_suggestion_rule` (`ledger_id`)")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `anxiety_relief_item` ADD COLUMN `category` TEXT NOT NULL DEFAULT '未分类'")
        db.execSQL("ALTER TABLE `anxiety_relief_item` ADD COLUMN `is_sold` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `anxiety_relief_item` ADD COLUMN `cost_by_use` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `anxiety_relief_item` ADD COLUMN `use_count` INTEGER NOT NULL DEFAULT 0")
    }
}
