package za.co.woolworths.financial.services.android.contracts

interface ICommonView<T> : RequestListener<T> {
    fun showProgress() {}
    fun hideProgress() {}
}