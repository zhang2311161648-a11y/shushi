package com.naveenapps.expensemanager.core.domain.usecase.categorysuggestion

/**
 * Built-in rules live in one local registry so they are easy to review and extend. The keys
 * refer to stable built-in category keys, never to hard-coded database IDs.
 */
data class CategoryKeywordRule(
    val categoryKey: String,
    val categoryNameAliases: List<String>,
    val keywords: List<String>,
)

object CategoryKeywordRegistry {

    val rules: List<CategoryKeywordRule> = listOf(
        rule("food", listOf("餐饮", "food"), "麦当劳", "肯德基", "必胜客", "汉堡", "火锅", "烧烤", "食堂", "早餐", "午饭", "午餐", "晚饭", "晚餐", "外卖", "饭店", "餐厅"),
        rule("beverages", listOf("奶茶咖啡", "coffee", "beverages"), "瑞幸", "星巴克", "喜茶", "奈雪", "茶百道", "霸王茶姬", "咖啡", "奶茶"),
        rule("shopping", listOf("购物", "shopping"), "淘宝", "京东", "拼多多", "商场", "优衣库", "衣服", "鞋", "包", "购物", "山姆"),
        rule("transportation", listOf("交通", "transportation"), "滴滴", "高德打车", "出租车", "地铁", "公交", "高铁", "火车", "机票", "打车", "共享单车"),
        rule("medical", listOf("医疗", "health", "medical"), "医院", "挂号", "药店", "药", "检查", "体检", "门诊"),
        rule("learning", listOf("学习", "learning"), "教材", "课本", "书", "打印", "网课", "课程", "考试", "报名费", "文具"),
        rule("sports", listOf("运动", "sports"), "健身房", "健身", "羽毛球", "篮球", "游泳", "跑步", "球拍"),
        rule("travel", listOf("旅游", "travel"), "酒店", "民宿", "景区", "旅游", "旅行", "门票"),
        rule("dormitory", listOf("宿舍", "dormitory"), "宿舍", "宿舍费", "水费", "电费"),
        rule("daily_needs", listOf("日用品", "daily needs", "daily_needs"), "纸巾", "洗衣液", "牙膏", "洗发水", "沐浴露", "清洁用品"),
        rule("beauty", listOf("美容", "beauty"), "护肤", "化妆", "理发", "剪头发", "面膜"),
        rule("entertainment", listOf("娱乐", "entertainment"), "电影", "电影院", "游戏", "ktv", "会员", "演唱会"),
        rule("gifts", listOf("送礼", "gift", "gifts"), "礼物", "生日礼物", "红包"),
        rule("snacks", listOf("零食", "snacks"), "薯片", "饼干", "巧克力", "零食"),
        rule("living_expense", listOf("生活费", "living expense", "living_expense"), "生活费", "家里给", "生活补贴"),
        rule("part_time", listOf("兼职", "part time", "part_time"), "兼职", "工资", "薪资"),
        rule("gift_income", listOf("礼金", "gift income", "gift_income"), "礼金", "收到红包"),
        rule("internet", listOf("网络", "internet"), "网络收入", "稿费"),
    )

    private fun rule(
        categoryKey: String,
        aliases: List<String>,
        vararg keywords: String,
    ) = CategoryKeywordRule(categoryKey, aliases, keywords.toList())
}
