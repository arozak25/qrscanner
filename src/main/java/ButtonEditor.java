import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class ButtonEditor extends DefaultCellEditor {

    protected JButton button;
    private String label;
    private boolean isPushed;
    private Object id;
    public static JButton confirmButton = new JButton("OK");

    public ButtonEditor(JCheckBox checkBox) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        id = table.getValueAt(row, 0);

        isPushed = true;

        if (column == 3){
            String sql = "DELETE FROM data WHERE id = ?";
            PreparedStatement stmt = null;
            try {
                stmt = DatabaseHelper.getDBconnection().prepareStatement(sql);
                stmt.setString( 1, id.toString());
                stmt.executeUpdate();
                stmt.close();

                DefaultTableModel model = ((DefaultTableModel)table.getModel());
                model.removeRow(row);
                table.setModel(model);

                JOptionPane.showMessageDialog(confirmButton, "Data Deleted");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(confirmButton, "Delete Failed");
                e.printStackTrace();
            }
        }

        return table;
    }

    @Override
    public Object getCellEditorValue() {
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}