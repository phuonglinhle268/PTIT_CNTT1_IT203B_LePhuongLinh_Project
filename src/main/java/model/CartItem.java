package model;

import java.math.BigDecimal;

/**
 * CartItem - Đại diện cho một sản phẩm trong giỏ hàng.
 *
 * unitPrice: giá tại thời điểm thêm vào giỏ.
 * Nếu đang có Flash Sale → unitPrice = giá flash.
 * Nếu không               → unitPrice = giá gốc.
 *
 * Tách biệt với product.getPrice() để tránh bị thay đổi
 * nếu admin cập nhật giá sản phẩm trong lúc customer đang giỏ hàng.
 */
public class CartItem {

    private Product    product;
    private int        quantity;
    private BigDecimal unitPrice;  // Giá thực tế tại lúc thêm vào giỏ

    /**
     * Constructor khi không có flash sale → dùng giá gốc.
     */
    public CartItem(Product product, int quantity) {
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = product.getPrice();
    }

    /**
     * Constructor khi có flash sale → dùng giá flash.
     */
    public CartItem(Product product, int quantity, BigDecimal unitPrice) {
        this.product   = product;
        this.quantity  = quantity;
        this.unitPrice = unitPrice;
    }

    public Product    getProduct()              { return product; }
    public int        getQuantity()             { return quantity; }
    public void       setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice()            { return unitPrice; }

    /** Thành tiền = unitPrice (giá thực tế) × số lượng */
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Có đang được giảm giá không */
    public boolean isDiscounted() {
        return unitPrice.compareTo(product.getPrice()) < 0;
    }

    @Override
    public String toString() {
        return String.format("| %-35s | %-6s | %-12s | %3d | %,15.0f |",
                product.getProductName(),
                product.getStorage(),
                product.getColor(),
                quantity,
                getSubtotal());
    }
}