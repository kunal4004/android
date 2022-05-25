package za.co.woolworths.financial.services.android.models.dto;


public class LiquorCompliance  {
    private String liquorImageUrl;
    private boolean liquorOrder=false;


    public LiquorCompliance(boolean liquorOrder,String liquorImageUrl){
        this.liquorOrder=liquorOrder;
        this.liquorImageUrl=liquorImageUrl;


    }

    public boolean isLiquorOrder() {
        return liquorOrder;
    }

    public void setLiquorOrder(boolean liquorOrder) {
        this.liquorOrder = liquorOrder;
    }



    public String getLiquorImageUrl() {
        return liquorImageUrl;
    }

    public void setLiquorImageUrl(String liquorImageUrl) {
        this.liquorImageUrl = liquorImageUrl;
    }


}
