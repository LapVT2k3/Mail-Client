package Interface;

import java.util.LinkedList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class DataInformationModel extends AbstractTableModel {

    private String[] hearders = {"Tiêu Đề", "Người Gửi", "Thời Gian"};
    private List<DataInformation> Data = new LinkedList<>();

    public DataInformationModel(String[] hearders, List<DataInformation> Data) {
        this.hearders = hearders;
        this.Data = Data;
    }

    @Override
    public int getRowCount() {
        if (Data != null) {
            return Data.size();
        }
        return 0;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < getColumnCount()) {
            return hearders[columnIndex];
        }
        return "";
    }

    @Override
    public int getColumnCount() {
        if (hearders != null) {
            return hearders.length;
        }
        return 0;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex > getRowCount() || columnIndex > getColumnCount()) {
            return "";
        }
        DataInformation row = Data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return row.getTitle();
            case 1:
                return row.getSender();
            case 2:
                return row.getDate();
            default:
                return "";
        }
    }
}
