package com.canteen.model;

/**
 * Beverage item extending FoodItem.
 * Features an extra boolean attribute indicating cold or hot serving,
 * and custom promotional discount logic.
 */
public class Beverage extends FoodItem {
    private boolean isCold;

    public Beverage() {
        super();
        setItemType("BEVERAGE");
    }

    public Beverage(int id, String name, String description, double price, int categoryId,
                    String categoryName, boolean available, String imageUrl, int preparationTime, boolean isCold) {
        super(id, name, description, price, categoryId, categoryName, "BEVERAGE", available, imageUrl, preparationTime);
        this.isCold = isCold;
    }

    @Override
    public String getItemType() {
        return "BEVERAGE";
    }

    @Override
    public String getItemBadge() {
        return isCold ? "🧊 COLD" : "☕ HOT";
    }

    /**
     * Applies promotional discount + 5% extra for beverages.
     * @param discountPercent base discount percentage
     * @return calculated discounted price
     */
    @Override
    public double calculateDiscountedPrice(double discountPercent) {
        if (discountPercent <= 0) {
            return getPrice();
        }
        double totalDiscount = Math.min(discountPercent + 5.0, 100.0);
        return Math.round(getPrice() * (1.0 - (totalDiscount / 100.0)) * 100.0) / 100.0;
    }

    public boolean isCold() {
        return isCold;
    }

    public void setCold(boolean cold) {
        isCold = cold;
    }
}
