/**
 * @author Abdul Rozak
 **/

import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.ByQuadrantReader;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.CvPoint;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.Date;

import static org.bytedeco.opencv.global.opencv_core.cvPoint;
import static org.bytedeco.opencv.global.opencv_core.cvScalar;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgproc.cvFont;
import static org.bytedeco.opencv.global.opencv_imgproc.cvPutText;
import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_PLAIN;

public class QRScanner extends JFrame
{
    // menubar
    static JMenuBar mb;

    // JMenu
    static JMenu x;
    static JMenu y;

    // Menu items
    static JMenuItem m1, m2, m3, m4;

    public static void main(String[] args) throws FrameGrabber.Exception {
        new QRScanner();
    }

    QRScanner() throws FrameGrabber.Exception {
        FrameGrabber grabber = FrameGrabber.createDefault(0);
        grabber.start();

        final java.awt.Dimension size = WebcamResolution.HD.getSize();

        // Frame to capture
        Frame frame = null;
        IplImage img = null;


        final CanvasFrame canvasFrame = new CanvasFrame("Webcam", CanvasFrame.getDefaultGamma() / grabber.getGamma());

        // create a menubar
        mb = new JMenuBar();

        // create a menu
        x = new JMenu("File");
        y = new JMenu("Help");

        // create menuitems
        m1 = new JMenuItem("Manage Data");
        m2 = new JMenuItem("Quit");

        // add menu items to menu
        x.add(m1);
        x.add(m2);

        m3 = new JMenuItem("About");
        m4 = new JMenuItem("User Manual");

        m3.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                final JFrame frame = new JFrame("About");
                frame.setSize(300, 300);
                frame.setLocationRelativeTo(null);

                JLabel label = new JLabel("<html>Dibuat oleh:<br/> 41518110010 DERBY NUGRAHA <br/> " +
                        "41518110017 ERIC SETIAWAN  <br/> 41518110085 ABDUL ROZAK <br/> 41518110089 YOGIE NUR INDIARTO" +
                        "<br/> 41518110039 VIKY NURHIDAYANTI </html>", SwingConstants.CENTER);
                frame.add(label);

                frame.setVisible(true);
            }
        });

        m4.addActionListener(new AbstractAction() {
             public void actionPerformed(ActionEvent e) {

             }
        });

        // add menu items to menu
        y.add(m3);
        y.add(m4);

        // add menu to menu bar
        mb.add(x);
        mb.add(y);

        m1.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String[] columnNames = {"ID", "Data", "Date", "Button"};
                DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                String query = "SELECT * FROM data";

                // create a Statement from the connection
                try {
                    Statement statement = DatabaseHelper.getDBconnection().createStatement();
                    ResultSet rs = statement.executeQuery(query);

                    while (rs.next()) {
                        String data = rs.getString("data");
                        String date = rs.getString("date");
                        String id = rs.getString("id");

                        // create a single array of one row's worth of data
                        String[] row = { id, data, date, "Delete" } ;

                        // and add this row of data into the table model
                        tableModel.addRow(row);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                JTable table = new JTable(tableModel);
                table.getColumn("Button").setCellRenderer(new ButtonRenderer());
                table.getColumn("Button").setCellEditor(new ButtonEditor(new JCheckBox()));

                JScrollPane scroll = new JScrollPane(table);

                table.setPreferredScrollableViewportSize(table.getPreferredSize());

                table.getColumnModel().getColumn(0).setPreferredWidth(100);

                final JFrame frame = new JFrame("Manage Data");

                frame.setSize(size);
                frame.setVisible(true);
                frame.add(scroll);
                frame.setLocationRelativeTo(null);
            }
        });

        m2.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // add menubar to frame
        canvasFrame.setJMenuBar(mb);
        canvasFrame.setSize(size);
        canvasFrame.setLocationRelativeTo(null);
        canvasFrame.setVisible(true);

        String[] buttons = { "Yes", "No"};

        String lastText = "";
        int index = 0;

        while ((frame = grabber.grab()) != null) {

            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bufferedImage;

            bufferedImage = converter.getBufferedImage(frame);

            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            GenericMultipleBarcodeReader barcodeReader = new
                    GenericMultipleBarcodeReader(new ByQuadrantReader(new
                    MultiFormatReader()));
            Result[] results;
            OpenCVFrameConverter.ToIplImage iplImageConverter = new OpenCVFrameConverter.ToIplImage();
            img = iplImageConverter.convert(frame);
            try {
                results = barcodeReader.decodeMultiple(bitmap);
                // setting results.
                lastText = "";
                for (Result oneResult : results) {
                    lastText = oneResult.getText();
                    for (ResultPoint resultPoint : oneResult.getResultPoints()) {
                        CvPoint ptPoit = cvPoint((int) resultPoint.getX(), (int) resultPoint.getY());
                        cvCircle(img, ptPoit, 5, cvScalar(0, 255, 0, 0), 2, 4, 0);
                    }
                }

                if (!lastText.equals("")){
                    int input = JOptionPane.showOptionDialog(new JFrame(), "QR Code Scan Result: " + lastText, "Save Data?",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,null, buttons, buttons[0]);

                    if (input == JOptionPane.OK_OPTION)
                    {
                        // do something
                        Connection conn = DatabaseHelper.getDBconnection();

                        String sql = "INSERT INTO data(data,date)"+"VALUES('"+lastText+"','"+new Date().toString()+"')";

                        // create a Statement from the connection
                        Statement statement = conn.createStatement();

                        statement.executeUpdate(sql);

                        JButton button = new JButton();
                        button.setText("OK");

                        JOptionPane.showMessageDialog(button, "Data Saved");
                    }
                }
            } catch (NotFoundException e) {/* cannot find or decode */} catch (SQLException e) {
                e.printStackTrace();
            }
            cvPutText(img, lastText, cvPoint(0,img.height()-20), cvFont(FONT_HERSHEY_PLAIN), cvScalar(0,255,0,0));
            index++;

            if (canvasFrame.isVisible()) {
                canvasFrame.showImage(frame);
            }
        }

    }


}