package za.co.woolworths.financial.services.android.contracts

interface ICommonView<T> : IResponseListener<T> {
    fun showProgress() {}
    fun hideProgress() {}
}