package model;

import java.math.BigDecimal;

public class NgayCong {
    private NhanVien nhanVien;
    private BigDecimal soNgayDaLam = BigDecimal.ZERO;
    private BigDecimal soNgayChuan = BigDecimal.ZERO;

    public NgayCong() {
    }

    public NgayCong(NhanVien nhanVien, double soNgayDaLam, double soNgayChuan) {
        this(nhanVien, BigDecimal.valueOf(soNgayDaLam), BigDecimal.valueOf(soNgayChuan));
    }

    public NgayCong(NhanVien nhanVien, BigDecimal soNgayDaLam, BigDecimal soNgayChuan) {
        this.nhanVien = nhanVien;
        this.soNgayDaLam = soNgayDaLam;
        this.soNgayChuan = soNgayChuan;
    }

    public double tinhPhanTramHoanThanh() {
        if (soNgayChuan.signum() <= 0) return 0;
        return soNgayDaLam.movePointRight(2)
                .divide(soNgayChuan, java.math.MathContext.DECIMAL128).doubleValue();
    }

    public String getPhanLoai() {
        if (soNgayChuan.signum() <= 0) return "Kém";
        BigDecimal percent = soNgayDaLam.movePointRight(2);
        if (percent.compareTo(soNgayChuan.multiply(BigDecimal.valueOf(100))) >= 0) return "Xuất sắc";
        if (percent.compareTo(soNgayChuan.multiply(BigDecimal.valueOf(90))) >= 0) return "Giỏi";
        if (percent.compareTo(soNgayChuan.multiply(BigDecimal.valueOf(75))) >= 0) return "Khá";
        if (percent.compareTo(soNgayChuan.multiply(BigDecimal.valueOf(50))) >= 0) return "Trung bình";
        return "Kém";
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public double getSoNgayDaLam() {
        return soNgayDaLam.doubleValue();
    }

    public void setSoNgayDaLam(double soNgayDaLam) {
        this.soNgayDaLam = BigDecimal.valueOf(soNgayDaLam);
    }

    public double getSoNgayChuan() {
        return soNgayChuan.doubleValue();
    }

    public void setSoNgayChuan(double soNgayChuan) {
        this.soNgayChuan = BigDecimal.valueOf(soNgayChuan);
    }

    public BigDecimal getSoNgayDaLamChinhXac() {
        return soNgayDaLam;
    }

    public BigDecimal getSoNgayChuanChinhXac() {
        return soNgayChuan;
    }
}
