package dao;

import model.FlashSale;
import java.util.List;

public interface FlashSaleDAO {
    boolean      createFlashSale(FlashSale flashSale);
    List<FlashSale> getAllFlashSales();
    List<FlashSale> getActiveFlashSales();
    FlashSale    getCurrentActiveFlashSale();   // đang trong thời gian + is_active
    boolean      deleteFlashSale(int flashSaleId);
    boolean      toggleActive(int flashSaleId, boolean active);
}