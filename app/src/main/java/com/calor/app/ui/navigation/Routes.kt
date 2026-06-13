package com.calor.app.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val FRIDGE = "fridge"
    const val DISHES = "dishes"
    const val CATALOG = "catalog"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
    const val ADD_PRODUCT = "add_product"
    const val EDIT_PRODUCT = "edit_product/{productId}"
    const val CREATE_DISH = "create_dish"
    const val DISH_DETAIL = "dish_detail/{dishId}"
    const val ADD_FRIDGE = "add_fridge"

    fun editProduct(id: Long) = "edit_product/$id"
    fun dishDetail(id: Long) = "dish_detail/$id"
}
