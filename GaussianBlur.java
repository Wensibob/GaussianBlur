import android.graphics.Bitmap;

/**
 * Gaussian blur for a bitmap with specified radius.
 */
public class GaussianBlur {

    public static Bitmap gaussianBlur(Bitmap src, int r) {
        int[] sPixels = getPixels(src);
        int[][] scls = pixelsToChannels(sPixels);

        int[][] tcls = new int[4][sPixels.length];

        final int w = src.getWidth();
        final int h = src.getHeight();

        int[] boxs = boxesForGauss(r, 3);

        for (int i = 0; i < 4; i++) {
            tcls[i] = boxBlur(scls[i], tcls[i], w, h, (boxs[0] - 1) / 2);
            scls[i] = boxBlur(tcls[i], scls[i], w, h, (boxs[1] - 1) / 2);
            tcls[i] = boxBlur(scls[i], tcls[i], w, h, (boxs[2] - 1) / 2);
        }

        int[] tPixels = channelsToPixels(tcls[0], tcls[1], tcls[2], tcls[3]);

        return Bitmap.createBitmap(tPixels, w, h, src.getConfig());
    }

    private static int[] boxBlur(int[] scl, int[] tcl, int w, int h, int r) {
        for (int i = 0; i < scl.length; i++) {
            tcl[i] = scl[i];
        }

        if (r >= w / 2) r = w / 2 - 1;
        if (r >= h / 2) r = h / 2 - 1;

        scl = boxBlurH(tcl, scl, w, h, r);
        tcl = boxBlurT(scl, tcl, w, h, r);

        return tcl;
    }

    private static int[] boxBlurH(int[] scl, int[] tcl, int w, int h, int r) {
        float iarr = 1.0f / (r + r + 1);

        for (int i = 0; i < h; i++) {
            int ti = i * w;
            int li = ti;
            int ri = ti + r;
            int fv = scl[ti];
            int lv = scl[ti + w - 1];
            int val = (r + 1) * fv;

            for (int j = 0; j < r; j++) {
                val += scl[ti + j];
            }

            for (int j = 0; j <= r; j++) {
                val += scl[ri++] - fv;
                tcl[ti++] = Math.round(val * iarr);
            }

            for (int j = r + 1; j < w - r; j++) {
                val += scl[ri++] - scl[li++];
                tcl[ti++] = Math.round(val * iarr);
            }

            for (int j = w - r; j < w; j++) {
                val += lv - scl[li++];
                tcl[ti++] = Math.round(val * iarr);
            }
        }

        return tcl;
    }

    private static int[] boxBlurT(int[] scl, int[] tcl, int w, int h, int r) {
        float iarr = 1.0f / (r + r + 1);

        for (int i = 0; i < w; i++) {
            int ti = i;
            int li = ti;
            int ri = ti + r * w;

            int fv = scl[ti];
            int lv = scl[ti + w * (h - 1)];
            int val = (r + 1) * fv;

            for (int j = 0; j < r; j++) {
                val += scl[ti + j * w];
            }

            for (int j = 0; j <= r; j++) {
                val += scl[ri] - fv;
                tcl[ti] = Math.round(val * iarr);
                ri += w;
                ti += w;
            }

            for (int j = r + 1; j < h - r; j++) {
                val += scl[ri] - scl[li];
                tcl[ti] = Math.round(val * iarr);
                li += w;
                ri += w;
                ti += w;
            }

            for (int j = h - r; j < h; j++) {
                val += lv - scl[li];
                tcl[ti] = Math.round(val * iarr);
                li += w;
                ti += w;
            }
        }

        return tcl;
    }

    private static int[] boxesForGauss(float sigma, int n) {
        float wIdeal = (float) Math.sqrt((12 * sigma * sigma / n) + 1);
        int wl = (int) Math.floor(wIdeal);
        if (wl % 2 == 0) wl--;
        int wu = wl + 2;

        float mIdeal = (12 * sigma * sigma - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4);
        int m = Math.round(mIdeal);

        int[] sizes = new int[n];

        for (int i = 0; i < n; i++) {
            sizes[i] = i < m ? wl : wu;
        }

        return sizes;
    }

    private static int[] getPixels(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        int[] pixels = new int[width * height];

        src.getPixels(pixels, 0, width, 0, 0, width, height);

        return pixels;
    }

    private static int[][] pixelsToChannels(int[] pixels) {
        final int num = 4;
        int length = pixels.length;
        int[][] channels = new int[num][length];

        for (int j = 0; j < length; j++) {
            int p = pixels[j];
            channels[0][j] = (p >> 24) & 0xff;  // alpha channel
            channels[1][j] = (p >> 16) & 0xff;  // red channel
            channels[2][j] = (p >> 8) & 0xff;   // green channel
            channels[3][j] = p & 0xff;          // blue channel
        }

        return channels;
    }

    private static int[] channelsToPixels(int[]as, int[] rs, int[] gs, int[] bs) {
        final int length = rs.length;
        int[] pixels = new int[length];

        for (int i = 0; i < length; i++) {
            pixels[i] = ((as[i] & 0xff) << 24)
                    | ((rs[i] & 0xff) << 16)
                    | ((gs[i] & 0xff) << 8)
                    | (bs[i] & 0xff);
        }

        return pixels;
    }
}

