package yanchao.bj.ngp.utils.qrcode;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.HashMap;

/**
 * 北京中油瑞飞信息技术有限责任公司 研究院 瑞信项目
 * All Rights Reserved
 * 项目:瑞信项目
 * 类:ngp
 * 描述:
 * 版本信息：
 *
 * @作者: yanchao
 * @日期: 2017-10-08 14:03
 */

public class QRCodeFactory {

    /**
     * @return Bitmap
     * @方法功能说明: 生成二维码图片, 返回Bitmap, 不然会报空指针错误
     * @参数: url 要转换的地址或字符串,可以是中文
     * @参数: width 生成图片宽度
     * @参数: height 生成图片高度
     */
    public static Bitmap createQRImage(String url, int width, int height) {
        try {
            // 判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            HashMap<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter()
                    .encode(url, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
        }
        return null;
    }
}
