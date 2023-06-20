package za.co.woolworths.financial.services.android.enhancedSubstitution

import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.*
import za.co.woolworths.financial.services.android.util.KotlinUtils

class EnhanceSubstitutionHelperTest {
    companion object {
         const val STORE_ID = "473"
         const val PRODUCT_ID = "123456"
         const val SKU_ID = "6001009025692"
         const val SUBSTITUTION_ID = "20068905"
         const val COMMARCE_ITEM_ID = "ci1648299093"
         const val SEARCH_TYPE = "search"
         const val RESPONSE_TYPE = "details"
         const val DELIVERY_TYPE = "onDemand"
         const val DEVICE_TOKEN = ""
         const val SESSION_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSIsImtpZCI6ImEzck1VZ01Gdjl0UGNsTGE2eUYzekFrZnF1RSJ9.eyJpc3MiOiJodHRwczovL3N0c3FhLndvb2x3b3J0aHMuY28uemEvY3VzdG9tZXJpZCIsImF1ZCI6IldXT25lQXBwIiwiZXhwIjoxNjU1NDYzNzM4LCJuYmYiOjE2NDc2ODc3MzgsIm5vbmNlIjoiNmMxODIxMWQtMWFkZS00MTkwLWJhNWEtYmE4YTI3ODRhYTIwIiwiaWF0IjoxNjQ3Njg3NzM4LCJzaWQiOiJiNmQ0MmU4ZTc5MGMwNmNjMzk2MTE5MTRhODA2ZWM0YSIsInN1YiI6Ijc4ZWZjMDViLTliMGQtNGU2OC05ZjhlLTE0YmVhOTE2NTA0NSIsImF1dGhfdGltZSI6MTY0NzY4NzczOCwiaWRwIjoiaWRzcnYiLCJ1cGRhdGVkX2F0IjoiMTY0NzY4MDUzOCIsInByZWZlcnJlZF91c2VybmFtZSI6ImNkN2QzOThmMDM4ODRjNTNhZTQ0ZGFjYTg5MWE0ODhiIiwiZW1haWwiOiJjY3MxQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjoiZmFsc2UiLCJuYW1lIjoiU0hBVU4iLCJmYW1pbHlfbmFtZSI6IlNJTU9OUyIsIkF0Z0lkIjoiMzE0NzgwOTQxIiwiQXRnU2Vzc2lvbiI6IntcIkpTRVNTSU9OSURcIjpcIjZHaWgxNURaWlBQNC1JdlZBV2c2bFNiVlVCNml6OUZfd1VkeXJreUt1Y2ltbzVqS3g0VEMhMTY4MTc2ODY0NFwiLFwiX2R5blNlc3NDb25mXCI6XCI1MTE3NDI5NzI1MzYwMDkwMjc2XCJ9IiwiQzJJZCI6IjEzODkyODg3IiwiYW1yIjpbInBhc3N3b3JkIl19.HMA3CWHjDrIivUSYvzxHlmCD_TG27lajGA1imACD0N5fXiAGzYyiGX2ZJ3e7W5kReoJ809EGo8MhXLjzIgYXFZwWTM29GXiX1_bAgww0IsgeTdvNU5vTxrXLRuWlkwGiV63Z3otRftmZuuRZnwmr42zVLdZ_llvSxFaYBTrbhybvNWxcbyc6-t1dQiVG78DaRGvVglgHptiMRuD0G4zgL3LrypI6YGbtNrVoP0uhOTDd7Tu7c59ZTIlqn4iDa9GdvwZMntYZlkDUispjUHsbySDI9Cci_K5vz7Az4s50stoJV96JwjNYxZ1EpFqG19b_T7MGr63LPhhjsWLHCDbxug"


          fun prepareKiboProductRequest(): KiboProductRequest {
              val product = Product("20018702", "20018702")
              val list = ArrayList<Product>()
              list.add(product)
              return KiboProductRequest(KotlinUtils.getDeliveryDetails(false), list)
         }

         fun prepareKiboResponseWithEmptyList(): KiboProductResponse {

              val items = ArrayList<Item>()
              val action = Action(items)
              val actionList = ArrayList<Action>()
              actionList.add(action)
              val actionResponse = ActionResponse(actionList, "1234")
              val actionResponseList = ArrayList<ActionResponse>()
              actionResponseList.add(actionResponse)
              val itemResponse = ItemResponse(actionResponseList)
              val response = Response(
                   "-1", "success"
              )
              return KiboProductResponse(itemResponse, response, 200)
         }

    }
}