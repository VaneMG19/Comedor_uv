package mx.uv.comedor.util;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import mx.uv.comedor.model.DetallePedido;
import mx.uv.comedor.model.Pedido;
import mx.uv.comedor.model.Usuario;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.imageio.ImageIO;

/*
  Genera un PDF de comprobante de pedido usando iText 7.
 */
public class PDFService {

    private static final DeviceRgb COLOR_AZUL_UV   = new DeviceRgb(24, 82, 157);
    private static final DeviceRgb COLOR_VERDE_UV  = new DeviceRgb(40, 173, 86);
    private static final DeviceRgb COLOR_AMARILLO  = new DeviceRgb(245, 184, 0);
    private static final DeviceRgb COLOR_GRIS_300  = new DeviceRgb(220, 220, 220);
    private static final DeviceRgb COLOR_GRIS_700  = new DeviceRgb(80, 80, 80);
    private static final DeviceRgb COLOR_BLANCO    = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb COLOR_AMARILLO_LIGHT = new DeviceRgb(255, 248, 224);

    /*
      Genera un logo UV cuadrado como PNG en memoria.
      Es un fondo blanco con texto "UV" azul.
     */
    private static byte[] generarLogoUV(int tamaño) throws IOException {
        BufferedImage img = new BufferedImage(tamaño, tamaño, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Fondo blanco con bordes redondeados
        g.setColor(Color.WHITE);
        g.fillRoundRect(0, 0, tamaño, tamaño, tamaño / 6, tamaño / 6);

        // Texto "UV" centrado en azul
        g.setColor(new Color(24, 82, 157));
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, (int)(tamaño * 0.5));
        g.setFont(font);
        java.awt.FontMetrics fm = g.getFontMetrics();
        String texto = "UV";
        int textWidth  = fm.stringWidth(texto);
        int textHeight = fm.getAscent();
        int x = (tamaño - textWidth)  / 2;
        int y = (tamaño + textHeight) / 2 - fm.getDescent() / 2;
        g.drawString(texto, x, y);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return baos.toByteArray();
    }

    /*
      Genera el PDF del comprobante.
     */
    public static byte[] generarComprobante(Pedido pedido, Usuario usuario, byte[] qrPng)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter   writer = new PdfWriter(baos);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 40, 40, 40);

        // ── HEADER UV con logo ──
        byte[] logoBytes = generarLogoUV(120);
        Image logo = new Image(ImageDataFactory.create(logoBytes))
                .setWidth(45).setHeight(45);

        Table header = new Table(new float[]{1, 4})
                .setWidth(UnitValue.createPercentValue(100));
        Cell logoCell = new Cell()
                .add(logo)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10);
        Cell titulo = new Cell()
                .add(new Paragraph("Comedor Universitario")
                        .setFontSize(16).setBold().setFontColor(COLOR_BLANCO))
                .add(new Paragraph("Universidad Veracruzana")
                        .setFontSize(10).setFontColor(COLOR_BLANCO))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                .setPaddingLeft(5).setPaddingTop(10).setPaddingBottom(10);
        header.addCell(logoCell);
        header.addCell(titulo);
        header.setBackgroundColor(COLOR_AZUL_UV).setMarginBottom(20);
        doc.add(header);

        // TÍTULO
        doc.add(new Paragraph("COMPROBANTE DE PEDIDO")
                .setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COLOR_AZUL_UV).setMarginBottom(5));

        if (pedido.getFolio() != null) {
            doc.add(new Paragraph("Folio: " + pedido.getFolio())
                    .setFontSize(11).setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15));
        }

        // DATOS DEL PEDIDO
        Table datos = new Table(new float[]{1, 2})
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
        agregarFilaDatos(datos, "Cliente:",
                usuario != null ? usuario.getNombreCompleto() : "—");
        agregarFilaDatos(datos, "Email:",
                usuario != null ? usuario.getEmail() : "—");
        if (pedido.getFechaCreacion() != null) {
            agregarFilaDatos(datos, "Fecha:",
                    pedido.getFechaCreacion().format(
                            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm",
                                    new Locale("es","MX"))));
        }
        agregarFilaDatos(datos, "Tipo de pedido:",
                pedido.getTipo() != null ? pedido.getTipo().name() : "—");
        if (pedido.getEstado() != null) {
            agregarFilaDatos(datos, "Estado:", pedido.getEstado().name());
        }
        if (pedido.getMetodoPagoDisplay() != null) {
            agregarFilaDatos(datos, "Método de pago:",
                    formatearMetodoPago(pedido.getMetodoPagoDisplay()));
        }
        doc.add(datos);

        //  AVISO ANTICIPADO
        if (pedido.getProgramacion() != null) {
            Table aviso = new Table(1).setWidth(UnitValue.createPercentValue(100));
            Cell c = new Cell()
                    .add(new Paragraph("PEDIDO ANTICIPADO").setBold().setFontSize(11))
                    .add(new Paragraph("Recoger el: " +
                            pedido.getProgramacion().getFechaRecogida() + " a las " +
                            pedido.getProgramacion().getHoraRecogida()).setFontSize(10))
                    .add(new Paragraph("Lugar: " + pedido.getProgramacion().getLugarRecogida())
                            .setFontSize(10))
                    .setBackgroundColor(COLOR_AMARILLO_LIGHT)
                    .setBorderLeft(new com.itextpdf.layout.borders.SolidBorder(COLOR_AMARILLO, 3))
                    .setPadding(10);
            aviso.addCell(c);
            doc.add(aviso.setMarginBottom(15));
        }

        // TABLA DE PLATILLOS
        doc.add(new Paragraph("Detalle del pedido").setBold().setFontSize(12)
                .setFontColor(COLOR_AZUL_UV).setMarginBottom(5));

        Table tabla = new Table(new float[]{4, 1, 2, 2})
                .setWidth(UnitValue.createPercentValue(100));
        agregarHeaderTabla(tabla, "Platillo");
        agregarHeaderTabla(tabla, "Cant.");
        agregarHeaderTabla(tabla, "Precio Unit.");
        agregarHeaderTabla(tabla, "Subtotal");

        if (pedido.getDetalles() != null) {
            for (DetallePedido d : pedido.getDetalles()) {
                String nombre = d.getPlatillo() != null && d.getPlatillo().getNombre() != null
                        ? d.getPlatillo().getNombre()
                        : "Platillo #" + d.getIdPlatillo();
                if (d.isCubiertoPorBeca()) nombre += " [BECA]";

                BigDecimal subtotal = d.getPrecioUnitario()
                        .multiply(new BigDecimal(d.getCantidad()));

                agregarCeldaTabla(tabla, nombre, TextAlignment.LEFT);
                agregarCeldaTabla(tabla, String.valueOf(d.getCantidad()),
                        TextAlignment.CENTER);
                agregarCeldaTabla(tabla, "$" + d.getPrecioUnitario().toPlainString(),
                        TextAlignment.RIGHT);
                agregarCeldaTabla(tabla, "$" + subtotal.toPlainString(),
                        TextAlignment.RIGHT);
            }
        }
        doc.add(tabla.setMarginBottom(15));

        //  TOTALES
        Table tot = new Table(new float[]{3, 1})
                .setWidth(UnitValue.createPercentValue(60))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);
        if (pedido.getSubtotal() != null) {
            agregarFilaTotal(tot, "Subtotal:",
                    "$" + pedido.getSubtotal().toPlainString(), false);
        }
        if (pedido.getDescuentoBeca() != null
                && pedido.getDescuentoBeca().compareTo(BigDecimal.ZERO) > 0) {
            agregarFilaTotal(tot, "Descuento beca:",
                    "-$" + pedido.getDescuentoBeca().toPlainString(), false);
        }
        agregarFilaTotal(tot, "TOTAL:",
                "$" + (pedido.getTotal() != null ? pedido.getTotal().toPlainString() : "0.00"),
                true);
        doc.add(tot);

        // QR
        if (qrPng != null && qrPng.length > 0) {
            doc.add(new Paragraph("\n").setMarginTop(20));
            Image qrImg = new Image(ImageDataFactory.create(qrPng))
                    .setWidth(150).setHeight(150)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            doc.add(qrImg);
            doc.add(new Paragraph("Escanea este QR al recoger tu pedido")
                    .setFontSize(9).setItalic().setFontColor(COLOR_GRIS_700)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // FOOTER
        doc.add(new Paragraph("\n\nComedor Universitario UV — " +
                "Gracias por tu preferencia 🎓")
                .setFontSize(9).setFontColor(COLOR_GRIS_700)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(20));

        doc.close();
        return baos.toByteArray();
    }

    /** Formato lindo del método de pago para mostrar. */
    private static String formatearMetodoPago(String metodo) {
        if (metodo == null) return "—";
        switch (metodo) {
            case "EFECTIVO":     return "Efectivo";
            case "TARJETA":      return "Tarjeta de crédito/débito";
            case "BECA":         return "Beca alimentaria";
            case "MIXTO":        return "Pago mixto";
            case "TRANSFERENCIA":return "Transferencia bancaria";
            default:             return metodo;
        }
    }

    private static void agregarFilaDatos(Table t, String etiqueta, String valor) {
        t.addCell(new Cell()
                .add(new Paragraph(etiqueta).setBold().setFontSize(10).setFontColor(COLOR_GRIS_700))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(4));
        t.addCell(new Cell()
                .add(new Paragraph(valor != null ? valor : "—").setFontSize(10))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(4));
    }

    private static void agregarHeaderTabla(Table t, String texto) {
        t.addHeaderCell(new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(10).setFontColor(COLOR_BLANCO))
                .setBackgroundColor(COLOR_AZUL_UV).setPadding(6));
    }

    private static void agregarCeldaTabla(Table t, String texto, TextAlignment align) {
        t.addCell(new Cell()
                .add(new Paragraph(texto != null ? texto : "—").setFontSize(10))
                .setTextAlignment(align).setPadding(5)
                .setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(COLOR_GRIS_300, 0.5f))
                .setBorderLeft(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBorderRight(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBorderTop(com.itextpdf.layout.borders.Border.NO_BORDER));
    }

    private static void agregarFilaTotal(Table t, String etiqueta, String valor, boolean negrita) {
        Cell e = new Cell().add(new Paragraph(etiqueta).setFontSize(negrita ? 12 : 10)
                        .setBold().setFontColor(negrita ? COLOR_AZUL_UV : COLOR_GRIS_700))
                .setTextAlignment(TextAlignment.RIGHT).setPadding(4)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        Cell v = new Cell().add(new Paragraph(valor).setFontSize(negrita ? 12 : 10)
                        .setBold().setFontColor(negrita ? COLOR_AZUL_UV : COLOR_GRIS_700))
                .setTextAlignment(TextAlignment.RIGHT).setPadding(4)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        if (negrita) {
            e.setBorderTop(new com.itextpdf.layout.borders.SolidBorder(COLOR_AZUL_UV, 1));
            v.setBorderTop(new com.itextpdf.layout.borders.SolidBorder(COLOR_AZUL_UV, 1));
        }
        t.addCell(e);
        t.addCell(v);
    }
}
