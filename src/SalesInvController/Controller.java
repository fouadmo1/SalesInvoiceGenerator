package SalesInvController;

import SalesInvModel.Invoice;
import SalesInvModel.InvoicesTableModel;
import SalesInvModel.Line;
import SalesInvModel.LinesTableModel;
import SalesInvView.InvoiceDialog;
import SalesInvView.MainFrame;
import SalesInvView.LineDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Controller implements ActionListener, ListSelectionListener {

    private MainFrame frame;
    private InvoiceDialog invoiceDialog;
    private LineDialog lineDialog;

    public Controller(MainFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        
        switch (actionCommand) {
            case "Load File":
                loadFile();
                break;
            case "Save File":
                saveFile();
                break;
            case "Create New Invoice":
                createNewInvoice();
                break;
            case "Delete Invoice":
                deleteInvoice();
                break;
            case "Create New Item":
                createNewItem();
                break;
            case "Delete Item":
                deleteItem();
                break;
            case "createInvoiceCancel":
                createInvoiceCancel();
                break;
            case "createInvoiceOK":
                createInvoiceOK();
                break;
            case "createLineOK":
                createLineOK();
                break;
            case "createLineCancel":
                createLineCancel();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int selectedIndex = frame.getInvoiceTable().getSelectedRow();
        if(selectedIndex != -1){
            
            Invoice currentInvoice = frame.getInvoices().get(selectedIndex);
            frame.getInvoiceNumLabel().setText("" + currentInvoice.getNumber());
            frame.getInvoiceDateLabel().setText(currentInvoice.getDate());
            frame.getCustomerNameLabel().setText(currentInvoice.getCustomer());
            frame.getInvoiceTotalLabel().setText("" + currentInvoice.getInvoiceTotal());
            LinesTableModel linesTableModel = new LinesTableModel(currentInvoice.getLines());
            frame.getLineTable().setModel(linesTableModel);
            linesTableModel.fireTableDataChanged();
        }
    }

    private void loadFile() {
        JFileChooser fc = new JFileChooser();
        try {
            int result = fc.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File headerFile = fc.getSelectedFile();
                Path headerPath = Paths.get(headerFile.getAbsolutePath());
                List<String> headerLines = Files.readAllLines(headerPath);
                
                ArrayList<Invoice> invoicesArray = new ArrayList<>();
                System.out.println("invoiceHeader contains :");
                for (String headerLine : headerLines) {
                    try {
                        String[] headerParts = headerLine.split(",");
                        int invoiceNum = Integer.parseInt(headerParts[0]);
                        
                        System.out.println("invoiceNum "+invoiceNum +"{");
                        String invoiceDate = headerParts[1];
                        System.out.print(invoiceDate+",");
                        String customerName = headerParts[2];
                        System.out.println(customerName+"}");
                        Invoice invoice = new Invoice(invoiceNum, invoiceDate, customerName);
                        invoicesArray.add(invoice);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error in line format", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                
                result = fc.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File lineFile = fc.getSelectedFile();
                    Path linePath = Paths.get(lineFile.getAbsolutePath());
                    List<String> lineLines = Files.readAllLines(linePath);
                    
                    System.out.println("invoiceline contains :");
                    for (String lineLine : lineLines) {
                        try {
                            String lineParts[] = lineLine.split(",");
                            
                            int invoiceNum = Integer.parseInt(lineParts[0]);
                            
                            System.out.println("lineNum "+lineParts[0]+"{");
                            String itemName = lineParts[1];
                            System.out.print(lineParts[1]+",");
                            double itemPrice = Double.parseDouble(lineParts[2]);
                            System.out.print(lineParts[2]+",");
                            int count = Integer.parseInt(lineParts[3]);
                            System.out.println(lineParts[3]+".");
                            Invoice inv = null;
                            for (Invoice invoice : invoicesArray) {
                                ;
                                if (invoice.getNumber() == invoiceNum) {
                                    System.out.println("}");
                                    inv = invoice;
                                    break;
                                }
                            }

                            Line line = new Line(itemName, itemPrice, count, inv);
                            inv.getLines().add(line);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(frame, "Error in line format", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    
                }
                frame.setInvoices(invoicesArray);
                InvoicesTableModel invoicesTableModel = new InvoicesTableModel(invoicesArray);
                frame.setInvoicesTableModel(invoicesTableModel);
                frame.getInvoiceTable().setModel(invoicesTableModel);
                frame.getInvoicesTableModel().fireTableDataChanged();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Cannot read file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFile() {
        ArrayList<Invoice> invoices = frame.getInvoices();
        String headers = "";
        String lines = "";
        for(Invoice invoice : invoices){
            String invCSV = invoice.getAsCSV();
            headers += invCSV;
            headers += "\n";

            for(Line line : invoice.getLines()){
                String lineCSV = line.getAsCSV();
                lines += lineCSV;
                lines += "\n";
            }
        }
        try {
            JFileChooser fc = new JFileChooser();
            int result = fc.showSaveDialog(frame);
            if(result == JFileChooser.APPROVE_OPTION){
                File headerFile = fc.getSelectedFile();
                FileWriter hf = new FileWriter(headerFile);
                hf.write(headers);
                hf.flush();
                hf.close();
                result = fc.showSaveDialog(frame);
                if(result == JFileChooser.APPROVE_OPTION){
                    File lineFile = fc.getSelectedFile();
                    FileWriter X = new FileWriter(lineFile);
                    X.write(lines);
                    X.flush();
                    X.close();
                }
            }
        } catch(Exception ex){

        }
    }

    private void createNewInvoice(){
        invoiceDialog = new InvoiceDialog(frame);
        invoiceDialog.setVisible(true);
    }

    private void deleteInvoice(){
        int selectedR = frame.getInvoiceTable().getSelectedRow();
        if(selectedR != -1){
            frame.getInvoices().remove(selectedR);
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void createNewItem(){
        lineDialog = new LineDialog(frame);
        lineDialog.setVisible(true);
    }

    private void deleteItem(){
        int selectedR = frame.getLineTable().getSelectedRow();

        if(selectedR != -1){
            LinesTableModel linesTableModel = (LinesTableModel) frame.getLineTable().getModel();
            linesTableModel.getLines().remove(selectedR);
            linesTableModel.fireTableDataChanged();
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
    }

    private void createInvoiceCancel() {
        invoiceDialog.setVisible(false);
        invoiceDialog.dispose();
        invoiceDialog = null;
    }

    private void createInvoiceOK() {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String date = invoiceDialog.getInvDateField().getText();
        String customer = invoiceDialog.getCustNameField().getText();
        int num = frame.getNextInvoiceNum();
        try {
            String[] dateParts = date.split("-");  
            if (dateParts.length < 3) {
                JOptionPane.showMessageDialog(frame, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                int day = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int year = Integer.parseInt(dateParts[2]);
                if (day > 31 || month > 12) {
                    JOptionPane.showMessageDialog(frame, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    Invoice invoice = new Invoice(num, date, customer);
                    frame.getInvoices().add(invoice);
                    frame.getInvoicesTableModel().fireTableDataChanged();
                    invoiceDialog.setVisible(false);
                    invoiceDialog.dispose();
                    invoiceDialog = null;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Wrong date format", "Error", JOptionPane.ERROR_MESSAGE);
        }
       
    }

    private void createLineOK() {
        String item = lineDialog.getItemNameField().getText();
        String countS = lineDialog.getItemCountField().getText();
        String priceS = lineDialog.getItemPriceField().getText();
        int count = Integer.parseInt(countS);
        double price = Double.parseDouble(priceS);
        int selectedInvoice = frame.getInvoiceTable().getSelectedRow();
        if (selectedInvoice != -1) {
            Invoice invoice = frame.getInvoices().get(selectedInvoice);
            Line line = new Line(item, price, count, invoice);
            invoice.getLines().add(line);
            LinesTableModel linesTableModel = (LinesTableModel) frame.getLineTable().getModel();
            linesTableModel.fireTableDataChanged();
            frame.getInvoicesTableModel().fireTableDataChanged();
        }
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

    private void createLineCancel() {
        lineDialog.setVisible(false);
        lineDialog.dispose();
        lineDialog = null;
    }

}
