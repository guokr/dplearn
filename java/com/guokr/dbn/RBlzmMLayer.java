package com.guokr.dbn;

import static com.guokr.dbn.MathUtils.binomial;
import static com.guokr.dbn.MathUtils.sigmoid;
import static com.guokr.dbn.MatrixUtils.compose22;
import static com.guokr.dbn.MatrixUtils.random;
import static com.guokr.dbn.MatrixUtils.zero;
import mikera.matrixx.IMatrix;
import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;

public class RBlzmMLayer {

    public int     vnum;
    public int     hnum;
    public IMatrix weights;

    public RBlzmMLayer(int vnum, int hnum, IMatrix weights) {
        this.vnum = vnum;
        this.hnum = hnum;

        if (weights != null) {
            this.weights = weights;
        } else {
            double alpha = 1.0 / this.vnum;
            IMatrix rand = random(vnum, hnum, -alpha, alpha);

            this.weights = compose22(zero(1, 1), zero(1, hnum), zero(vnum, 1), rand);
        }
    }

    public double up(AVector vsample, AVector vweight) {
        return sigmoid(vsample.innerProduct(vweight).value);
    }

    public double down(AVector hsample, AVector hweight) {
        return sigmoid(hsample.innerProduct(hweight).value);
    }

    public void hsample_under_v(AVector hsample, AVector hmean, AVector vsample) {
        for (int i = 0; i < hnum; i++) {
            hmean.set(i, up(vsample, weights.getRow(i)));
            hsample.set(i, binomial(1, hmean.get(i)));
        }
    }

    public void vsample_under_h(AVector vsample, AVector vmean, AVector hsample) {
        for (int i = 0; i < vnum; i++) {
            vmean.set(i, down(hsample, weights.getColumn(i)));
            vsample.set(i, binomial(1, vmean.get(i)));
        }
    }

    public void gibbs_hvh(AVector hpsample, AVector nvmeans, AVector nvsamples, AVector nhmeans, AVector nhsamples) {
        vsample_under_h(nvsamples, nvmeans, hpsample);
        hsample_under_v(nhsamples, nhmeans, nvsamples);
    }

    public void contrastive_divergence(int k, double learning_rate, AVector input) {
        AVector phmean = Vectorz.createRepeatedElement(hnum + 1, 0);
        AVector phsample = Vectorz.createRepeatedElement(hnum + 1, 0);

        AVector nvmeans = Vectorz.createRepeatedElement(vnum + 1, 0);
        AVector nvsamples = Vectorz.createRepeatedElement(vnum + 1, 0);

        AVector nhmeans = Vectorz.createRepeatedElement(hnum + 1, 0);
        AVector nhsamples = Vectorz.createRepeatedElement(hnum + 1, 0);

        hsample_under_v(phsample, phmean, input);

        for (int step = 0; step < k; step++) {
            if (step == 0) {
                gibbs_hvh(phsample, nvmeans, nvsamples, nhmeans, nhsamples);
            } else {
                gibbs_hvh(nhsamples, nvmeans, nvsamples, nhmeans, nhsamples);
            }
        }

        IMatrix mp = phmean.outerProduct(input);
        IMatrix mn = nhmeans.outerProduct(nvsamples);

        mp.scale(learning_rate);
        mn.scale(-learning_rate);

        weights.add(mp);
        weights.add(mn);

    }

    public void reconstruct(AVector vrecons, AVector vsample) {
        AVector h = Vectorz.newVector(hnum);

        for (int i = 0; i < hnum; i++) {
            h.set(i, up(vsample, weights.getColumn(i)));
        }

        for (int i = 0; i < vnum; i++) {
            vrecons.set(i, sigmoid(weights.getRow(i).innerProduct(h).value));
        }
    }

}