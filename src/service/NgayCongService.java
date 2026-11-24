package service;

import java.math.BigDecimal;
import java.util.Objects;
import model.NgayCong;
import model.NhanVien;
import model.PhongBan;
import repository.NhanSuRepository;

public final class NgayCongService {
    private final NhanSuRepository repository;

    public NgayCongService(NhanSuRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public NgayCong capNhatNgayCong(NhanVien nhanVien, String daLam, String chuan) {
        if (daLam == null || chuan == null) {
            throw new IllegalArgumentException("Ngày công và ngày chuẩn phải là số hợp lệ.");
        }
        try {
            return capNhatNgayCong(nhanVien, new BigDecimal(daLam.trim()), new BigDecimal(chuan.trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Ngày công và ngày chuẩn phải là số hợp lệ.", exception);
        }
    }

    public NgayCong capNhatNgayCong(NhanVien nhanVien, double daLam, double chuan) {
        return capNhatNgayCong(nhanVien, BigDecimal.valueOf(daLam), BigDecimal.valueOf(chuan));
    }

    public NgayCong capNhatNgayCong(NhanVien nhanVien, BigDecimal daLam, BigDecimal chuan) {
        if (daLam == null || chuan == null || daLam.signum() < 0 || chuan.signum() <= 0
                || daLam.compareTo(chuan) > 0 || !Double.isFinite(chuan.doubleValue())
                || daLam.stripTrailingZeros().scale() > 2 || chuan.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Số ngày công không hợp lệ.");
        }
        synchronized (repository) {
            repository.kiemTraNhanVien(nhanVien);
            NgayCong ngayCong = new NgayCong(nhanVien, daLam, chuan);
            nhanVien.setNgayCong(ngayCong);
            return ngayCong;
        }
    }

    public void resetPhongBan(PhongBan phongBan) {
        synchronized (repository) {
            for (NhanVien nv : repository.danhSachNhanVien(phongBan)) {
                nv.setNgayCong(new NgayCong(nv, 0, 26));
            }
        }
    }

}
