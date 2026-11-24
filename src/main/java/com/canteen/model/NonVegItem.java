package com.canteen.model;

/**
 * Non-Vegetarian food item.
 * Demonstrates Inheritance and Polymorphic method implementation.
 */
public class NonVegItem extends FoodItem {

    public NonVegItem() {
        super();
        setItemType("NON_VEG");
    }

    public NonVegItem(int id, String name, String description, double price, int categoryId,
                      String categoryName, boolean available, String imageUrl, int preparationTime) {
        super(id, name, description, price, categoryId, categoryName, "NON_VEG", available, imageUrl, preparationTime);
    }

    @Override
    public String getItemType() {
        return "NON_VEG";
    }

    @Override
    public String getItemBadge() {
        return "🔴 NON-VEG";
    }

    /**
     * Applies half discount for non-vegetarian items due to higher preparation and sourcing costs.
     * @param discountPercent base discount percentage
     * @return calculated discounted price
     */
    @Override
    public double calculateDiscountedPrice(double discountPercent) {
        if (discountPercent <= 0) {
            return getPrice();
        }
        double halfDiscount = Math.min(discountPercent / 2.0, 100.0);
        return Math.round(getPrice() * (1.0 - (halfDiscount / 100.0)) * 100.0) / 100.0;
    }
}
