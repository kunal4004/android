package za.co.woolworths.financial.services.android.shoppinglist.component

/**
 * Created by Kunal Uttarwar on 12/01/24.
 */
sealed class MyListFlowType {

    object FlowTypeNormal : MyListFlowType()
    object FlowTypeViewOnly : MyListFlowType()
    object FlowTypeEdit : MyListFlowType()

    companion object {
        private var flowTypeState: MyListFlowType = FlowTypeNormal
        fun setFlowType(flowType: MyListFlowType){
            flowTypeState = flowType
        }
        fun getFlowType() : MyListFlowType{
            return flowTypeState
        }
    }
}