package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature.screen

object LazyColumnKeyConstant {

    var index = 0;

    const val SectionDivider = "SectionDivider"
    const val SignInList = "SignInList"
    const val OfferSection = "OfferSection"
    const val OfferSectionSpacerBottom = "OfferSectionSpacerBottom"
    const val OfferSectionCarousel = "OfferSectionCarousel"
    const val AccountProductCardsGroupApplicationStatus = "AccountProductCardsGroupApplicationStatus"
    const val AccountProductCardsGroupElseBlock = "AccountProductCardsGroupElseBlock"
    const val NoC2IdNorProductView = "NoC2IdNorProductView"
    const val LinkYourWooliesCardUI = "LinkYourWooliesCardUI"
    const val ProductHeaderView = "ProductHeaderView"
    const val SpacerHeight8dp = "SpacerHeight8dp"
    const val OfferLazyListRowSnapSpacerWidth24dp = "OfferLazyListRowSnapSpacerWidth24dp"

    fun getDynamicKey(key : String): String {
        index++
        return "$key$index"
    }
}