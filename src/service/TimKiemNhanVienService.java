package service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import model.NhanVien;
import model.PhongBan;
import repository.NhanSuRepository;

/** Logic áp dụng và lọc lại chuyển từ TimKiemNhanVienDialog ở commit 3312331. */
public final class TimKiemNhanVienService {
    private final NhanSuRepository repository;
    private final Clock clock;

    public TimKiemNhanVienService(NhanSuRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    public TimKiemNhanVienService(NhanSuRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<NhanVien> tatCa(PhongBan phongBan) {
        return repository.danhSachNhanVien(phongBan);
    }

    public List<NhanVien> timKiem(PhongBan phongBan, String tieuChi, String giaTri) {
        synchronized (repository) {
            List<NhanVien> employees = repository.danhSachNhanVien(phongBan);
            AtomicReference<List<NhanVien>> result = new AtomicReference<>(List.of());
            apDungBoLoc(tieuChi, giaTri,
                    filter -> result.set(employees.stream().filter(filter).toList()),
                    message -> { throw new IllegalArgumentException(message); });
            return result.get();
        }
    }

    /** Adapter tạo predicate đã có từ giai đoạn 1; timKiem áp dụng đầy đủ fallback TX1. */
    public Predicate<NhanVien> taoBoLoc(String tieuChi, String giaTri) {
        AtomicReference<Predicate<NhanVien>> result = new AtomicReference<>();
        apDungBoLoc(tieuChi, giaTri, result::set,
                message -> { throw new IllegalArgumentException(message); });
        return result.get();
    }

    public void apDungBoLoc(String criterion, String input,
            Consumer<Predicate<NhanVien>> refreshTable, Consumer<String> showError) {
        if (criterion == null || !List.of("Họ tên", "Mã nhân viên", "Trạng thái", "Vị trí",
                "Tuổi", "Thâm niên", "Giới tính").contains(criterion)) {
            showError.accept("Tiêu chí tìm kiếm không hợp lệ.");
            return;
        }
        String value = input == null ? "" : input.trim();
        if (value.isEmpty()) {
            showError.accept("Vui lòng nhập giá trị tìm kiếm.");
            return;
        }
        if ("Giới tính".equals(criterion)) {
            if (!value.equals("Nam") && !value.equals("Nữ")) {
                showError.accept("Giới tính không hợp lệ.");
                return;
            }
            refreshTable.accept(nv -> Boolean.TRUE.equals(nv.getGioiTinh()) == value.equals("Nam"));
            return;
        }
        boolean id = criterion.equals("Mã nhân viên");
        boolean years = criterion.equals("Tuổi") || criterion.equals("Thâm niên");
        if (years || (id && value.matches("[-0-9\\s]+"))) {
            String[] bounds = value.split("-", -1);
            if (bounds.length > 2 || !bounds[0].trim().matches("[0-9]+")
                    || (bounds.length == 2 && !bounds[1].trim().matches("[0-9]+"))) {
                showError.accept("Giá trị tìm kiếm phải là số nguyên không âm hoặc khoảng hợp lệ.");
                return;
            }
            java.math.BigInteger min = new java.math.BigInteger(bounds[0].trim());
            java.math.BigInteger max = new java.math.BigInteger(bounds[bounds.length - 1].trim());
            if (min.compareTo(max) > 0) {
                showError.accept("Khoảng tìm kiếm không hợp lệ.");
                return;
            }
            refreshTable.accept(nv -> {
                String digits = id ? value(nv.getId()).replaceAll("[^0-9]", "")
                        : Integer.toString(calculateYearsSince(criterion.equals("Tuổi")
                                ? nv.getBirthDate() : nv.getNgayVaoLam()));
                if (digits.isEmpty()) return false;
                var number = new java.math.BigInteger(digits);
                return number.compareTo(min) >= 0 && number.compareTo(max) <= 0;
            });
            return;
        }
        String keyword = value.toLowerCase(Locale.ROOT);
        refreshTable.accept(nv -> getTextValue(nv, criterion).toLowerCase(Locale.ROOT).contains(keyword));
    }

    public boolean khopBoLocHienTai(NhanVien employee, String criterion, String input) {
        if (input == null || input.isBlank()) return true;
        try {
            return taoBoLoc(criterion, input).test(employee);
        } catch (IllegalArgumentException invalidFilter) {
            return false;
        }
    }

    private String getTextValue(NhanVien employee, String criterion) {
        if ("Họ tên".equals(criterion)) {
            return value(employee.getFullName());
        }
        if ("Mã nhân viên".equals(criterion)) {
            return value(employee.getId());
        }
        if ("Trạng thái".equals(criterion)) {
            return value(employee.getTrangThai());
        }
        return value(employee.getViTri());
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private int calculateYearsSince(Date date) {
        if (date == null) return -1;
        LocalDate start = date.toInstant().atZone(clock.getZone()).toLocalDate();
        LocalDate today = LocalDate.now(clock);
        return start.isAfter(today) ? -1 : Period.between(start, today).getYears();
    }
}
