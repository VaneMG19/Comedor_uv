package mx.uv.comedor.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
  Genera códigos QR usando ZXing.
 */
public class QRService {

    /*
      Genera un QR como bytes PNG.
      @param contenido texto a codificar
      @param tamaño    pixeles (cuadrado)
     */
    public static byte[] generarQR(String contenido, int tamaño)
            throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix matrix = new MultiFormatWriter().encode(
            contenido, BarcodeFormat.QR_CODE, tamaño, tamaño, hints);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        return baos.toByteArray();
    }

    /** Wrapper con tamaño default de 200px */
    public static byte[] generarQR(String contenido)
            throws WriterException, IOException {
        return generarQR(contenido, 200);
    }
}
