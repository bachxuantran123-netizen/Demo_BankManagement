package src.Utils;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Exporter {
    // Hàm này nhận vào JTable và File, nhiệm vụ chỉ là ghi dữ liệu ra file
    public static void exportTable(JTable table, File file) throws IOException {
        TableModel model = table.getModel();

        // Mở file với encoding UTF-8
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            bw.write('\ufeff');
            // 1. Ghi tiêu đề cột
            for (int i = 0; i < model.getColumnCount(); i++) {
                bw.write(model.getColumnName(i) + ",");
            }
            bw.newLine();

            // 2. Ghi dữ liệu dòng
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    // Lấy dữ liệu và chuyển thành chuỗi
                    Object value = model.getValueAt(i, j);
                    String data = (value != null) ? value.toString() : "";

                    // Xử lý dấu phẩy để tránh lỗi cột CSV
                    data = data.replace(",", " ");

                    bw.write(data + ",");
                }
                bw.newLine();
            }
        }
    }
}