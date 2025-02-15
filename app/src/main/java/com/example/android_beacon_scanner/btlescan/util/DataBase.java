package com.example.android_beacon_scanner.btlescan.util;

public class DataBase {
    private double[] r00 = {-74.98356164, -71.81375358, -76.55014327};
    private double[] r01 = {-70.64756447, -68.28306878, -62.06849315};
    private double[] r02 = {-68.47432763, -69.24069479, -67.61538462};
    private double[] r03 = {-64.29799427, -67.69970845, -67.11174785};
    private double[] r04 = {-66.64393939, -74.04260652, -68.76677316};
    private double[] r10 = {-66.43421053, -66.14824798, -63.6945245};
    private double[] r11 = {-64.83246073, -57.80589681, -69.35175879};
    private double[] r12 = {-64.18341709, -63.25783133, -57.37980769};
    private double[] r13 = {-63.42447917, -66.41772152, -60.95833333};
    private double[] r14 = {-68.92929293, -75.32413793, -68.16037736};
    private double[] r20 = {-66.43421053, -66.14824798, -63.6945245};
    private double[] r21 = {-64.83246073, -57.80589681, -69.35175879};
    private double[] r22 = {-62.82655827, -59.42592593, -69.30666667};
    private double[] r23 = {-56.3925, -71.7520436, -67.06812652};
    private double[] r24 = {-65.60895522, -66.77325581, -65.36842105};
    private double[] r30 = {-62.83943662, -60.48476454, -71.54742547};
    private double[] r31 = {-59.94428969, -67.79076923, -69.01857585};
    private double[] r32 = {-58.32374101, -59.49635036, -69.95443038};
    private double[] r33 = {-58.56149733, -60.13294798, -65.77410468};
    private double[] r34 = {-63.37396122, -66.69230769, -70.17493473};
    private double[] r40 = {-77.3313783, -74.36170213, -78.74486804};
    private double[] r41 = {-74.98356164, -71.81375358, -76.55014327};
    private double[] r42 = {-69.73293769, -66.33153639, -75.24369748};
    private double[] r43 = {-69.30051813, -73.56216216, -71.97580645};
    private double[] r44 = {-69.84384384, -77.46496815, -71.11014493};

    public double[][] getArray() {
        double[][] temp = {r00, r01, r02, r03, r04,
                r10, r11, r12, r13, r14,
                r20, r21, r22, r23, r24,
                r30, r31, r32, r33, r34,
                r40, r41, r42, r43, r44};
        return temp;
    }
}
