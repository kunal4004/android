package za.co.woolworths.financial.services.android.contracts

interface IWTodayInterface {
    fun onShowProductListing(categoryId: String, categoryName: String)
    fun onAddIngredientsToShoppingList(ingredients: String)
    fun onShowProductDetail(productId: String, skuId: String)
    fun sendEventToFirebase(eventName: String, parameter: String)
}