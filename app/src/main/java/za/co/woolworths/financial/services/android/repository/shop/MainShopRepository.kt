package za.co.woolworths.financial.services.android.repository.shop

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.delay
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.FirebaseManager
import java.io.IOException

class MainShopRepository : ShopRepository {

    override suspend fun fetchDashCategories(): Resource<DashCategories> {
        return try {
            delay(5000L)
            val responseString = "{\n" +
                    "\t\"productCatalogues\": [{\n" +
                    "\t\t\t\"name\": \"Banner Carousel\",\n" +
                    "\t\t\t\"headerText\": \"Shop by Preference\",\n" +
                    "\t\t\t\"banners\": [{\n" +
                    "\t\t\t\t\t\"displayName\": \"Vegan\",\n" +
                    "\t\t\t\t\t\"navigationState\": \"N-8uulckZeros7t\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ0m7VJgL9Qhv7LXs9R5Jg0PY0Bid6DOX1PTA&usqp=CAU\",\n" +
                    "\t\t\t\t\t\"filterContent\": false\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"displayName\": \"Fruits\",\n" +
                    "\t\t\t\t\t\"navigationState\": \"N-8uulckZ17zinix\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQygmaZNUtzIAjs0shE0CTVJV4Yk3Kk5ReZhA&usqp=CAU\",\n" +
                    "\t\t\t\t\t\"filterContent\": false\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"displayName\": \"Carb Clever\",\n" +
                    "\t\t\t\t\t\"navigationState\": \"N-8uulckZ17zinix\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTvwwBrtzYov1JhRO23Zr3H17csw_RFKK0frw&usqp=CAU\",\n" +
                    "\t\t\t\t\t\"filterContent\": false\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"displayName\": \"Indian\",\n" +
                    "\t\t\t\t\t\"navigationState\": \"N-8uulckZ17zinix\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRCo9a2fEzKYKywRIUp9pi3wlY6NTFicEaN0Q&usqp=CAU\",\n" +
                    "\t\t\t\t\t\"filterContent\": false\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t]\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"name\": \"Banner Grid\",\n" +
                    "\t\t\t\"headerText\": \"Shop by Promotions\",\n" +
                    "\t\t\t\"banners\": [{\n" +
                    "\t\t\t\t\t\"displayName\": \"\",\n" +
                    "\t\t\t\t\t\"navigationState\": \"N-8uulckZeros7t\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvY29udGVudC9sYW5kaW5nX3BhZ2VzL0Zvb2RfMjAyMC9Gb29kX0Rlc2t0b3BfV2VlazI2X0Jhbm5lcjA2LmpwZyJ9.jpg?&q=75\",\n" +
                    "\t\t\t\t\t\"filterContent\": false\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"displayName\": \"\",\n" +
                    "\t\t\t\t\t\"navigationState\": \"N-8uulckZ17zinix\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvY29udGVudC9sYW5kaW5nX3BhZ2VzL0Zvb2RfMjAyMS9ERF9EZXNrdG9wXzRVUHNtYWxsX0Jhbm5lcjAxLmpwZyJ9.jpg?&q=75\",\n" +
                    "\t\t\t\t\t\"filterContent\": false\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"displayName\": \"\",\n" +
                    "\t\t\t\t\t\"navigationState\": \"N-8uulckZeros7t\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvY29udGVudC9sYW5kaW5nX3BhZ2VzL0Zvb2RfMjAyMC9Gb29kX0Rlc2t0b3BfV2VlazI2X0Jhbm5lcjA4LmpwZyJ9.jpg?&q=75\",\n" +
                    "\t\t\t\t\t\"filterContent\": false\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"displayName\": \"\",\n" +
                    "\t\t\t\t\t\"navigationState\": \"N-8uulckZ17zinix\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvY29udGVudC9sYW5kaW5nX3BhZ2VzL0Zvb2RfMjAyMC9Gb29kX0Rlc2t0b3BfV2VlazI2X0Jhbm5lcjA5LmpwZyJ9.jpg?&q=75\",\n" +
                    "\t\t\t\t\t\"filterContent\": false\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t]\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"name\": \"Product Carousel\",\n" +
                    "\t\t\t\"headerText\": \"Trending\",\n" +
                    "\t\t\t\"products\": [{\n" +
                    "\t\t\t\t\t\"isRnREnabled\": false,\n" +
                    "\t\t\t\t\t\"productId\": \"20160074\",\n" +
                    "\t\t\t\t\t\"productName\": \"Basil Pesto 125 g\",\n" +
                    "\t\t\t\t\t\"externalImageRef\": \"https://assets.woolworthsstatic.co.za/Basil-Pesto-125-g-20160074.jpg?V=qp8S&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA0LTIxLzIwMTYwMDc0X2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Basil-Pesto-125-g-20160074.jpg?V=qp8S&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA0LTIxLzIwMTYwMDc0X2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"imagePath\": \"https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2021-04-21/20160074_hero.jpg\",\n" +
                    "\t\t\t\t\t\"isLiquor\": false,\n" +
                    "\t\t\t\t\t\"price\": \"56.99\",\n" +
                    "\t\t\t\t\t\"productType\": \"foodProducts\",\n" +
                    "\t\t\t\t\t\"sku\": \"20160074\",\n" +
                    "\t\t\t\t\t\"promotions\": [],\n" +
                    "\t\t\t\t\t\"brandHeaderDescription\": \"\"\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"isRnREnabled\": false,\n" +
                    "\t\t\t\t\t\"productId\": \"6009189863189\",\n" +
                    "\t\t\t\t\t\"productName\": \"Baby Marrow Spaghetti 300 g\",\n" +
                    "\t\t\t\t\t\"externalImageRef\": \"https://assets.woolworthsstatic.co.za/Baby-Marrow-Spaghetti-300-g-6009189863189.jpg?V=g2@z&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTAyLTI1LzYwMDkxODk4NjMxODlfaGVyby5qcGcifQ&\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Baby-Marrow-Spaghetti-300-g-6009189863189.jpg?V=g2@z&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTAyLTI1LzYwMDkxODk4NjMxODlfaGVyby5qcGcifQ&\",\n" +
                    "\t\t\t\t\t\"imagePath\": \"https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2020-02-25/6009189863189_hero.jpg\",\n" +
                    "\t\t\t\t\t\"isLiquor\": false,\n" +
                    "\t\t\t\t\t\"price\": \"29.99\",\n" +
                    "\t\t\t\t\t\"productType\": \"foodProducts\",\n" +
                    "\t\t\t\t\t\"sku\": \"6009189863189\",\n" +
                    "\t\t\t\t\t\"promotions\": [],\n" +
                    "\t\t\t\t\t\"promotionImages\": {\n" +
                    "\t\t\t\t\t\t\"vitality\": \"https://www-win-qa.woolworths.co.za/images/flashes/vitality_new_new.png\"\n" +
                    "\t\t\t\t\t},\n" +
                    "\t\t\t\t\t\"brandHeaderDescription\": \"\"\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"isRnREnabled\": false,\n" +
                    "\t\t\t\t\t\"productId\": \"6009184396286\",\n" +
                    "\t\t\t\t\t\"productName\": \"Fresh Sweet & Sour Stir-Fry Sauce 100 g\",\n" +
                    "\t\t\t\t\t\"externalImageRef\": \"https://assets.woolworthsstatic.co.za/Fresh-Sweet-Sour-Stir-Fry-Sauce-100-g-6009184396286.jpg?V=v6CW&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTAyLTIzLzYwMDkxODQzOTYyODZfaGVyby5qcGcifQ&\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Fresh-Sweet-Sour-Stir-Fry-Sauce-100-g-6009184396286.jpg?V=v6CW&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTAyLTIzLzYwMDkxODQzOTYyODZfaGVyby5qcGcifQ&\",\n" +
                    "\t\t\t\t\t\"imagePath\": \"https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2021-02-23/6009184396286_hero.jpg\",\n" +
                    "\t\t\t\t\t\"isLiquor\": false,\n" +
                    "\t\t\t\t\t\"price\": \"19.99\",\n" +
                    "\t\t\t\t\t\"productType\": \"foodProducts\",\n" +
                    "\t\t\t\t\t\"sku\": \"6009184396286\",\n" +
                    "\t\t\t\t\t\"promotions\": [],\n" +
                    "\t\t\t\t\t\"brandHeaderDescription\": \"\"\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"isRnREnabled\": false,\n" +
                    "\t\t\t\t\t\"productId\": \"6009184979663\",\n" +
                    "\t\t\t\t\t\"productName\": \"Cauliflower Couscous 380 g\",\n" +
                    "\t\t\t\t\t\"externalImageRef\": \"https://assets.woolworthsstatic.co.za/Cauliflower-Couscous-380-g-6009184979663.jpg?V=VhPv&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTAyLTI1LzYwMDkxODQ5Nzk2NjNfaGVyby5qcGcifQ&\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Cauliflower-Couscous-380-g-6009184979663.jpg?V=VhPv&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIwLTAyLTI1LzYwMDkxODQ5Nzk2NjNfaGVyby5qcGcifQ&\",\n" +
                    "\t\t\t\t\t\"imagePath\": \"https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2020-02-25/6009184979663_hero.jpg\",\n" +
                    "\t\t\t\t\t\"isLiquor\": false,\n" +
                    "\t\t\t\t\t\"price\": \"32.00\",\n" +
                    "\t\t\t\t\t\"productType\": \"foodProducts\",\n" +
                    "\t\t\t\t\t\"sku\": \"6009184979663\",\n" +
                    "\t\t\t\t\t\"promotions\": [],\n" +
                    "\t\t\t\t\t\"promotionImages\": {\n" +
                    "\t\t\t\t\t\t\"vitality\": \"https://www-win-qa.woolworths.co.za/images/flashes/vitality_new_new.png\"\n" +
                    "\t\t\t\t\t},\n" +
                    "\t\t\t\t\t\"brandHeaderDescription\": \"\"\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"isRnREnabled\": false,\n" +
                    "\t\t\t\t\t\"productId\": \"20040000\",\n" +
                    "\t\t\t\t\t\"productName\": \"Fresh Pour Over Cheese Sauce 200 g\",\n" +
                    "\t\t\t\t\t\"externalImageRef\": \"https://assets.woolworthsstatic.co.za/Fresh-Pour-Over-Cheese-Sauce-200-g-20040000.jpg?V=Hvjk&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTE4LzIwMDQwMDAwX2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Fresh-Pour-Over-Cheese-Sauce-200-g-20040000.jpg?V=Hvjk&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTE4LzIwMDQwMDAwX2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"imagePath\": \"https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2021-06-18/20040000_hero.jpg\",\n" +
                    "\t\t\t\t\t\"isLiquor\": false,\n" +
                    "\t\t\t\t\t\"price\": \"38.99\",\n" +
                    "\t\t\t\t\t\"productType\": \"foodProducts\",\n" +
                    "\t\t\t\t\t\"sku\": \"20040000\",\n" +
                    "\t\t\t\t\t\"promotions\": [],\n" +
                    "\t\t\t\t\t\"brandHeaderDescription\": \"\"\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"isRnREnabled\": true,\n" +
                    "\t\t\t\t\t\"averageRating\": \"4.0\",\n" +
                    "\t\t\t\t\t\"reviewCount\": \"1\",\n" +
                    "\t\t\t\t\t\"productId\": \"20120825\",\n" +
                    "\t\t\t\t\t\"productName\": \"Fresh Spinach & Ricotta Ravioli 250 g\",\n" +
                    "\t\t\t\t\t\"externalImageRef\": \"https://assets.woolworthsstatic.co.za/Fresh-Spinach-Ricotta-Ravioli-250-g-20120825.jpg?V=Beun&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA1LTA2LzIwMTIwODI1X2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Fresh-Spinach-Ricotta-Ravioli-250-g-20120825.jpg?V=Beun&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA1LTA2LzIwMTIwODI1X2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"imagePath\": \"https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2021-05-06/20120825_hero.jpg\",\n" +
                    "\t\t\t\t\t\"isLiquor\": false,\n" +
                    "\t\t\t\t\t\"price\": \"74.99\",\n" +
                    "\t\t\t\t\t\"productType\": \"foodProducts\",\n" +
                    "\t\t\t\t\t\"sku\": \"20120825\",\n" +
                    "\t\t\t\t\t\"promotions\": [],\n" +
                    "\t\t\t\t\t\"brandHeaderDescription\": \"\"\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"isRnREnabled\": false,\n" +
                    "\t\t\t\t\t\"productId\": \"20094621\",\n" +
                    "\t\t\t\t\t\"productName\": \"Fresh Napoletana Sauce 200 g\",\n" +
                    "\t\t\t\t\t\"externalImageRef\": \"https://assets.woolworthsstatic.co.za/Fresh-Napoletana-Sauce-200-g-20094621.jpg?V=gkTB&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTAyLTAxLzIwMDk0NjIxX2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Fresh-Napoletana-Sauce-200-g-20094621.jpg?V=gkTB&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTAyLTAxLzIwMDk0NjIxX2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"imagePath\": \"https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2021-02-01/20094621_hero.jpg\",\n" +
                    "\t\t\t\t\t\"isLiquor\": false,\n" +
                    "\t\t\t\t\t\"price\": \"38.99\",\n" +
                    "\t\t\t\t\t\"productType\": \"foodProducts\",\n" +
                    "\t\t\t\t\t\"sku\": \"20094621\",\n" +
                    "\t\t\t\t\t\"promotions\": [],\n" +
                    "\t\t\t\t\t\"brandHeaderDescription\": \"\"\n" +
                    "\t\t\t\t},\n" +
                    "\t\t\t\t{\n" +
                    "\t\t\t\t\t\"isRnREnabled\": false,\n" +
                    "\t\t\t\t\t\"productId\": \"20102142\",\n" +
                    "\t\t\t\t\t\"productName\": \"Fresh Pour Over Cheese Sauce 400 g\",\n" +
                    "\t\t\t\t\t\"externalImageRef\": \"https://assets.woolworthsstatic.co.za/Fresh-Pour-Over-Cheese-Sauce-400-g-20102142.jpg?V=dq95&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTE4LzIwMTAyMTQyX2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"externalImageRefV2\": \"https://assets.woolworthsstatic.co.za/Fresh-Pour-Over-Cheese-Sauce-400-g-20102142.jpg?V=dq95&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTE4LzIwMTAyMTQyX2hlcm8uanBnIn0&\",\n" +
                    "\t\t\t\t\t\"imagePath\": \"https://www-win-qa.woolworths.co.za/images/elasticera/products/hero/2021-06-18/20102142_hero.jpg\",\n" +
                    "\t\t\t\t\t\"isLiquor\": false,\n" +
                    "\t\t\t\t\t\"price\": \"58.99\",\n" +
                    "\t\t\t\t\t\"productType\": \"foodProducts\",\n" +
                    "\t\t\t\t\t\"sku\": \"20102142\",\n" +
                    "\t\t\t\t\t\"promotions\": [],\n" +
                    "\t\t\t\t\t\"brandHeaderDescription\": \"\"\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t]\n" +
                    "\t\t}\n" +
                    "\t],\n" +
                    "\t\"response\": {\n" +
                    "\t\t\"code\": \"-1\",\n" +
                    "\t\t\"desc\": \"Success\"\n" +
                    "\t},\n" +
                    "\t\"httpCode\": 200\n" +
                    "}"
            val result = Gson().fromJson(responseString, DashCategories::class.java)

            return Resource.success(result)

            /*val response = OneAppService.getDashCategory()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("An unknown error occured", null)
            } else {
                Resource.error("An unknown error occured", null)
            }*/
        } catch (e: IOException) {
            Log.e("EXCEPTION", "EXCEPTION:", e)
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }

    override suspend fun fetchOnDemandCategories(): Resource<RootCategories> {
        return try {
            delay(2000L)
            val responseString = "{\n" +
                    "\t\"onDemandCategories\": [{\n" +
                    "\t\t\t\"categoryName\": \"Meat, Poultry & Fish\",\n" +
                    "\t\t\t\"categoryId\": \"0\",\n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 18,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/y23a7p9j\"\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"categoryName\": \"Wine & Bubbles\",\n" +
                    "\t\t\t\"categoryId\": \"33\",  \n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 20,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/yckw854h\"\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"categoryName\": \"Food Cupboard\",\n" +
                    "\t\t\t\"categoryId\": \"33\",\n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 20,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/5dnwbjh8\"\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"categoryName\": \"Bread Bakery & Desserts\",\n" +
                    "\t\t\t\"categoryId\": \"33\",\n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 20,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/3jtnwrcd\"\n" +
                    "\t\t},\n" +
                    "\t\t{\n" +
                    "\t\t\t\"categoryName\": \"Flowers & Plants\",\n" +
                    "\t\t\t\"categoryId\": \"25\",\n" +
                    "\t\t\t\"hasChildren\": false,\n" +
                    "\t\t\t\"dimValId\": \"N-r0vkur\",\n" +
                    "\t\t\t\"megaMenuSortPriority\": 19,\n" +
                    "\t\t\t\"navigationType\": \"onDemand\",\n" +
                    "\t\t\t\"liquorStoreIds\": [],\n" +
                    "\t\t\t\"imgUrl\": \"https://tinyurl.com/2p9xw7ku\"\n" +
                    "\t\t}\n" +
                    "\t],\n" +
                    "\t\"dash\": {\n" +
                    "\t\t\"imgUrl\": \"https://s3-eu-west-1.amazonaws.com/wfs-oneapp-images-qa/category-images/woolies-dash.png\",\n" +
                    "\t\t\"categoryName\": \"WOOLIES DASH\",\n" +
                    "\t\t\"categoryId\": \"WOOLIES DASH\",\n" +
                    "\t\t\"dashBreakoutLink\": \"https://play.google.com/store/apps/details?id=za.co.woolworths.dash\",\n" +
                    "\t\t\"bannerText\": \"Same Day Delivery. Download Our App.\"\n" +
                    "\t},\n" +
                    "\t\"response\": {\n" +
                    "\t\t\"code\": \"-1\",\n" +
                    "\t\t\"desc\": \"Success\",\n" +
                    "\t\t\"version\": 288994731\n" +
                    "\t},\n" +
                    "\t\"httpCode\": 200\n" +
                    "}"
            val result = Gson().fromJson(responseString, RootCategories::class.java)

            return Resource.success(result)

            /*val response = OneAppService.getDashCategory()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("An unknown error occured", null)
            } else {
                Resource.error("An unknown error occured", null)
            }*/
        } catch (e: IOException) {
            Log.e("EXCEPTION", "EXCEPTION:", e)
            FirebaseManager.logException(e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }
}