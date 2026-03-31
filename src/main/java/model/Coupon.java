package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Coupon {

    private int       couponId;
    private String    couponCode;
    private int       discountPercent;
    private int       maxUses;
    private int       usedCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean   isActive;
    private LocalDate createdAt;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Coupon() {}

    public Coupon(String couponCode, int discountPercent, int maxUses,
                  LocalDate startDate, LocalDate endDate) {
        this.couponCode      = couponCode;
        this.discountPercent = discountPercent;
        this.maxUses         = maxUses;
        this.startDate       = startDate;
        this.endDate         = endDate;
        this.isActive        = true;
        this.usedCount       = 0;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int       getCouponId()                    { return couponId; }
    public void      setCouponId(int id)              { this.couponId = id; }

    public String    getCouponCode()                  { return couponCode; }
    public void      setCouponCode(String c)          { this.couponCode = c; }

    public int       getDiscountPercent()             { return discountPercent; }
    public void      setDiscountPercent(int d)        { this.discountPercent = d; }

    public int       getMaxUses()                     { return maxUses; }
    public void      setMaxUses(int m)                { this.maxUses = m; }

    public int       getUsedCount()                   { return usedCount; }
    public void      setUsedCount(int u)              { this.usedCount = u; }

    public LocalDate getStartDate()                   { return startDate; }
    public void      setStartDate(LocalDate d)        { this.startDate = d; }

    public LocalDate getEndDate()                     { return endDate; }
    public void      setEndDate(LocalDate d)          { this.endDate = d; }

    public boolean   isActive()                       { return isActive; }
    public void      setActive(boolean b)             { this.isActive = b; }

    public LocalDate getCreatedAt()                   { return createdAt; }
    public void      setCreatedAt(LocalDate d)        { this.createdAt = d; }

    // ── Helper ────────────────────────────────────────────────────────────

    /** Mã còn dùng được không: active + còn hạn + còn lượt */
    public boolean isUsable() {
        LocalDate today = LocalDate.now();
        return isActive
                && !today.isBefore(startDate)
                && !today.isAfter(endDate)
                && usedCount < maxUses;
    }

    public String getStatusLabel() {
        if (!isActive)                            return "Đã tắt";
        if (LocalDate.now().isAfter(endDate))     return "Hết hạn";
        if (usedCount >= maxUses)                 return "Hết lượt";
        if (LocalDate.now().isBefore(startDate))  return "Chưa đến hạn";
        return "Còn dùng được";
    }

    @Override
    public String toString() {
        return String.format("[%d] %-12s | Giảm %3d%% | %d/%d lượt | %s → %s | %s",
                couponId, couponCode, discountPercent,
                usedCount, maxUses,
                startDate.format(FMT), endDate.format(FMT),
                getStatusLabel());
    }
}