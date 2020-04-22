package za.co.woolworths.financial.services.android.contracts

interface IGenericAPILoaderView<T> : IResponseListener<T> {
    fun showProgress() {}
    fun hideProgress() {}
}