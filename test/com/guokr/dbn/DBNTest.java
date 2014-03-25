package com.guokr.dbn;

import mikera.vectorz.AVector;
import mikera.vectorz.Vectorz;

import org.junit.Assert;
import org.junit.Test;

public class DBNTest {

    @Test
    public void test() {

        int[] sizes_per_layer = { 6, 3, 3, 2 };
        DBN dbn = new DBN(sizes_per_layer);

        // pretrain

        int k = 1;
        double pretrain_lr = 0.1;
        int pretraining_epochs = 1000;

        double[][] traindata = { { 1, 1, 1, 0, 0, 0 }, { 1, 0, 1, 0, 0, 0 }, { 1, 1, 1, 0, 0, 0 },
                { 0, 0, 1, 1, 1, 0 }, { 0, 0, 1, 1, 0, 0 }, { 0, 0, 1, 1, 1, 0 } };

        for (double[] item : traindata) {
            dbn.pretrain(k, pretraining_epochs, pretrain_lr, Vectorz.create(item));
        }

        // finetune

        double finetune_lr = 0.1;
        int finetune_epochs = 500;

        double[][] tunedata = { { 1, 0 }, { 1, 0 }, { 1, 0 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, };

        for (int i = 0; i < 6; i++) {
            dbn.finetune(finetune_epochs, finetune_lr, Vectorz.create(traindata[i]), Vectorz.create(tunedata[i]));
        }

        // test data
        double[][] testdata = { { 1, 1, 0, 0, 0, 0 }, { 1, 1, 1, 1, 0, 0 }, { 0, 0, 0, 1, 1, 0 }, { 0, 0, 1, 1, 1, 0 }, };
        double[][] results = { { 1, 0 }, { 1, 0 }, { 0, 1 }, { 0, 1 }, };

        for (int i = 0; i < 4; i++) {
            AVector result = dbn.predict(Vectorz.create(testdata[i]));
            System.out.println(result);
            result.scale(-1);
            AVector test = Vectorz.create(results[i]);
            Assert.assertTrue("error is greater than expected!", test.epsilonEquals(result, 0.1));
        }
    }

}
