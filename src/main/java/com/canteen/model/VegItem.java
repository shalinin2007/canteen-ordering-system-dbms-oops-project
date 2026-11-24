package com.canteen.model;

/**
 * Vegetarian food item.
 * Demonstrates Inheritance and Polymorphic method implementation.
 */
public class VegItem extends FoodItem {

    public VegItem() {
        super();
        setItemType("VEG");
    }

    public VegItem(int id, String name, String description, double price, int categoryId,
                   String categoryName, boolean available, String imageUrl, int preparationTime) {
        super(id, name, description, price, categoryId, categoryName, "VEG", available, imageUrl, preparationTime);
    }

    @Override
    public String getItemType() {
        return "VEG";
    }

    @Override
    public String getItemBadge() {
        return "🟢 VEG";
    }

    /**
     * Applies full discount for vegetarian items.
     * @param discountPercent percentage of discount (e.g. 10.0 for 10%)
     * @return calculated discounted price
     */
    @Override
    public double calculateDiscountedPrice(double discountPercent) {
        if (discountPercent <= 0) {
            return getPrice();
        }
        double discount = Math.min(discountPercent, 100.0);
        return Math.round(getPrice() * (1.0 - (discount / 100.0)) * 100.0) / 100.0;
    }
}
