package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FlashSale - Áp dụng giảm giá TOÀN BỘ cửa hàng trong khoảng thời gian.
 * Không gắn với sản phẩm cụ thể.
 * Giá flash = price * (1 - discountPercent / 100)
 */
public class FlashSale {

    private int           flashSaleId;
    private int           discountPercent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean       isActive;
    private LocalDateTime createdAt;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FlashSale() {}

    public FlashSale(int discountPercent, LocalDateTime startTime, LocalDateTime endTime) {
        this.discountPercent = discountPercent;
        this.startTime       = startTime;
        this.endTime         = endTime;
        this.isActive        = true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int           getFlashSaleId()                    { return flashSaleId; }
    public void          setFlashSaleId(int id)              { this.flashSaleId = id; }

    public int           getDiscountPercent()                { return discountPercent; }
    public void          setDiscountPercent(int d)           { this.discountPercent = d; }

    public LocalDateTime getStartTime()                      { return startTime; }
    public void          setStartTime(LocalDateTime t)       { this.startTime = t; }

    public LocalDateTime getEndTime()                        { return endTime; }
    public void          setEndTime(LocalDateTime t)         { this.endTime = t; }

    public boolean       isActive()                          { return isActive; }
    public void          setActive(boolean b)                { this.isActive = b; }

    public LocalDateTime getCreatedAt()                      { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)       { this.createdAt = t; }

    // ── Helper ────────────────────────────────────────────────────────────

    /** Flash sale đang trong thời gian hiệu lực và được kích hoạt */
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && now.isAfter(startTime) && now.isBefore(endTime);
    }

    public String getStatusLabel() {
        LocalDateTime now = LocalDateTime.now();
        if (!isActive)              return "Đã tắt";
        if (now.isBefore(startTime)) return "Sắp diễn ra";
        if (now.isAfter(endTime))    return "Đã kết thúc";
        return "Đang diễn ra";
    }

    @Override
    public String toString() {
        return String.format("[%d] Giảm %2d%% | %s → %s | %s",
                flashSaleId, discountPercent,
                startTime.format(FMT), endTime.format(FMT),
                getStatusLabel());
    }
}